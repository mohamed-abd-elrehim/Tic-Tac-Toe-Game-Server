package xoserver;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import DTOS.*;
import enumstatus.EnumPlayerAction.Action;
import enumstatus.EnumStatus.Status;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

public class PlayerHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(PlayerHandler.class.getName());
    private final BufferedReader br;
    private final PrintWriter pw;
    private final Gson gson;
    private final Socket cs;
    private static final Map<String, Socket> onlinePlayers = new ConcurrentHashMap<>();
    private final FXMLDocumentBase fxmlDocumentBase;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public PlayerHandler(Socket cs, FXMLDocumentBase fxmlDocumentBase) throws IOException {
        this.cs = cs;
        this.br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
        this.pw = new PrintWriter(cs.getOutputStream(), true);
        this.gson = new Gson();
        this.fxmlDocumentBase = fxmlDocumentBase;
    }

    @Override
    public void run() {
        try {
            // Start periodic UI updates
            String jsonString;
            // Reading requests from the client
            while ((jsonString = br.readLine()) != null) {
                try {
                    // Deserialize incoming JSON to Request object
                    Request request = gson.fromJson(jsonString, Request.class);
                    LOGGER.info("Received request: " + request.getAction());

                    // Handle the request and generate a response
                    Response response = handleRequest(request);

                    // Serialize the response back to JSON
                    String responseJson = gson.toJson(response);
                    LOGGER.info("Sending response: " + responseJson);

                    // Synchronize when writing the response to avoid race conditions
                    synchronized (pw) {
                        pw.println(responseJson);
                        pw.flush(); // Ensure the data is sent immediately
                    }
                } catch (JsonSyntaxException e) {
                    LOGGER.warning("Malformed JSON received: " + jsonString);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error processing request", e);
                }
            }
        } catch (SocketException e) {
            LOGGER.info("Connection reset by client: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "I/O error occurred during communication", e);
        } finally {
            // Ensure resources are properly closed even in case of an exception
            synchronized (this) {
                closeResources();
            }
        }
    }

    private void closeResources() {
        try {
            if (br != null) {
                br.close();
            }
            if (pw != null) {
                pw.close();
            }
            if (cs != null && !cs.isClosed()) {
                cs.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error closing resources", ex);
        }
    }

    public static void sendJsonToAllPlayers() {
        Gson gson = new Gson();
        LOGGER.info("Sending server down message to all players");

        Map<String, Socket> snapshot = new ConcurrentHashMap<>(onlinePlayers);
        snapshot.forEach((playerName, socket) -> {
            try (PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {
                String jsonResponse = gson.toJson(new Response(false, "Server is down"));
                pw.println(jsonResponse);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error sending message to player " + playerName, ex);
                onlinePlayers.remove(playerName);
            }
        });

        LOGGER.info("Messages sent to all players.");
    }

    private Response handleRequest(Request request) {
        Action action = request.getAction();
        switch (action) {
            case login:
                return loginPlayer(request);
            case logout:
                return logoutPlayer(request);
            case register:
                return registerPlayer(request);
            case gamelobby:
                return getPlayersStatus(request);
            case wanttoplay:
                return sendRequestToPlayer(request);
            case yes:
            case no:
                LOGGER.info("Handling action: " + action);
                return getOtherPlayerResponse(request);
            case move:
                return sendMove(request);
//            case resign:
//                return sendResignRequest(request);
//            case updatescore:
//                return updateScore(request);
            default:
                return new Response(false, "Unknown action");
        }
    }

    private Response registerPlayer(Request request) {
        Player player = request.getPlayer();

        if (player == null || player.getUserName() == null) {
            LOGGER.warning("Player data is missing or username is null.");
            return new Response(false, "Player data is missing");
        }

        String userName = player.getUserName();
        LOGGER.info("Attempting registration for player: " + userName);

        try {
            if (PlayerDAO.isUserNameTaken(player)) {
                LOGGER.info("Registration failed: Player " + userName + " already has an account.");
                return new Response(false, "Player already has an account", player);
            }

            boolean registerSuccess = PlayerDAO.insert(player);
            if (registerSuccess) {
                LOGGER.info("Registration successful for player: " + userName);
                return new Response(true, "Registration successful", player);
            } else {
                LOGGER.severe("Registration failed for player: " + userName + " due to database error.");
                return new Response(false, "Registration failed due to an internal error");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An exception occurred during registration for player: " + userName, e);
            return new Response(false, "An unexpected error occurred");
        }
    }

    private Response loginPlayer(Request request) {
        LOGGER.info("Attempting login for player: " + request.getPlayer().getUserName());
        Player player = request.getPlayer();
        if (player == null || player.getUserName() == null) {
            return new Response(false, "Player data is missing");
        }
        if (isPlayerInMap(player.getUserName())) {
            LOGGER.info("Player " + player.getUserName() + " is already logged in.");
            return new Response(false, "Player is already logged in", player);
        } else {
            boolean loginSuccess = PlayerDAO.isUserLoggedin(player);
            if (loginSuccess) {
                addToMap(player.getUserName(), cs);
                LOGGER.info("Login successful for player: " + player.getUserName());
                return new Response(true, "Login successful", player);
            } else {
                LOGGER.info("Login failed for player: " + player.getUserName());
                return new Response(false, "Login failed", player);
            }
        }
    }

    private Response logoutPlayer(Request request) {
        Logger logger = Logger.getLogger(Request.class.getName());

        // Extract player from request
        Player player = request.getPlayer();

        // Validate player data
        if (player == null || player.getUserName() == null) {
            logger.warning("Player data is missing or incomplete for logout.");
            return new Response(false, "Player data is missing");
        }

        try {
            // Attempt to update player status to OFFLINE
            boolean statusUpdated = PlayerDAO.isStatusUpdated(player, Status.OFFLINE);

            // Check if status update was successful
            if (statusUpdated) {
                // Remove player from the online players list
                boolean playerRemoved = onlinePlayers.remove(player.getUserName()) != null;

                // Prepare response based on removal success
                if (playerRemoved) {
                    logger.info("Player " + player.getUserName() + " has been successfully logged out.");
                    return new Response(true, "Logout successful", player);
                } else {
                    logger.warning("Failed to remove player " + player.getUserName() + " from online list.");
                    return new Response(false, "Logout successful, but failed to remove player from online list", player);
                }
            } else {
                logger.warning("Failed to update status for player " + player.getUserName() + " to OFFLINE.");
                return new Response(false, "Logout failed due to status update failure", player);
            }
        } catch (Exception e) {
            logger.severe("Exception during logout process: " + e.getMessage());
            return new Response(false, "Logout failed due to an internal error", player);
        }
    }

    public Response getPlayersStatus(Request request) {
        // Initialize a mutable list to hold players
        List<Player> playerList = new ArrayList<>();
        // Create a new Response object with the list of players
        Response response = new Response(true, "Player List", playerList);
        // Add online players to the playerList
        addOnlinePlayers(response, request.getPlayer());
        return response;
    }

    private void addOnlinePlayers(Response response, Player currentPlayer) {
        try {
            ResultSet rs = PlayerDAO.selectOnline();
            while (rs.next()) {
                String username = rs.getString("USERNAME");
                if (!username.equals(currentPlayer.getUserName())) {
                    Player player = mapResultSetToPlayer(rs);
                    response.getPlayers().add(player);
                }
            }
            response.setDone(true);
            //  LOGGER.info("Online players retrieved successfully.");
        } catch (SQLException ex) {
            //    LOGGER.log(Level.SEVERE, "Error retrieving online players", ex);
            response.setDone(false);
            response.setMessage("Failed to retrieve players' status");
        }
    }

    private Player mapResultSetToPlayer(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.setId(rs.getInt("ID"));
        player.setUserName(rs.getString("USERNAME"));
        player.setPoints(rs.getInt("POINTS"));
        player.setStatus(Status.valueOf(rs.getString("STATUS")));
        return player;
    }

    private Response sendRequestToPlayer(Request request) {
        System.out.println("------------------------------------2wanttoplay");

        // Player initiating the request
        Player playerWhoSendRequest = request.getPlayer();

        // Validate player data
        if (playerWhoSendRequest == null || playerWhoSendRequest.getUserName() == null) {
            return new Response(false, "Player data is missing");
        }

        String playerWhoSendRequestUserName = playerWhoSendRequest.getUserName();
        String playerIWantPlayWithUserName = request.getMessage();  // Corrected 'Massage' to 'Message'

        // Validate the target player data
        if (playerIWantPlayWithUserName == null || playerIWantPlayWithUserName.trim().isEmpty()) {
            return new Response(false, "Target player's username is missing");
        }

        // Check if the target player is online
        if (!onlinePlayers.containsKey(playerIWantPlayWithUserName)) {
            return new Response(false, "Player " + playerIWantPlayWithUserName + " is not online");
        }

        // Submit task to executor service
        Future<Response> future = executorService.submit(() -> {
            try {
                Socket socket = findSocketByPlayerName(playerIWantPlayWithUserName);
                if (socket != null) {
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    // Create and send the play request
                    String responseJson = gson.toJson(new Response(true, playerWhoSendRequestUserName + " " + "wants to play with you"));
                    System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyy" + responseJson);
                    pw.println(responseJson);

                    // Return success response
                    return new Response(true, playerIWantPlayWithUserName + " received your play request");

                } else {
                    return new Response(false, "Failed to send request: Player is not online anymore");
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to send request to player " + playerIWantPlayWithUserName, ex);
                return new Response(false, "Failed to send request to player");
            }
        });

        try {
            // Get the result of the asynchronous task
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Error while sending request to player", e);
            return new Response(false, "Error occurred while sending request");
        }
    }

    private Socket findSocketByPlayerName(String key) {
        LOGGER.info("Searching for player with name: " + key);

        Socket socket = onlinePlayers.get(key);

        if (socket != null) {
            LOGGER.info("Player found: " + key);
        } else {
            LOGGER.warning("Player not found: " + key);
        }

        return socket;
    }

    
   
    private Response sendMove(Request request) {
        System.out.println("------------------------------------sendMove");

        // Player initiating the request
        Player playerWhoSendRequest = request.getPlayer();

        // Validate player data
        if (playerWhoSendRequest == null || playerWhoSendRequest.getUserName() == null) {
            System.out.println("Player data is missing");
            return new Response(false, "Player data is missing");
        }

        String recipientPlayerName = request.getMessage();  // Player to play with

        // Validate the target player's username
        if (recipientPlayerName == null || recipientPlayerName.trim().isEmpty()) {
            System.out.println("Target player's username is missing");

            return new Response(false, "Target player's username is missing");
        }

        // Check if the target player is online
        if (!onlinePlayers.containsKey(recipientPlayerName)) {
            System.out.println("playerIWillPlayWithUserName is not online");
            return new Response(false, "Player " + recipientPlayerName + " is not online");
        }

        // Retrieve the player object for the target player
        Player recipientPlayer = PlayerDAO.selectPlayerByUserName(recipientPlayerName);
        if (recipientPlayer == null) {
            System.out.println("playerIWillPlayWithUserName is not found");
            return new Response(false, "Player " + recipientPlayerName + " is not found");
        }

        // Asynchronously handle the game request
        Future<Response> future = executorService.submit(() -> {
            try {
                // Find the target player's socket to send the response
                Socket socket = findSocketByPlayerName(recipientPlayerName);
                if (socket != null) {
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    String responseJson;
                    responseJson = gson.toJson(new Response(true, "move" + request.getMove()));

                   
                    System.out.println("Response JSON===========================================: " + responseJson);
                    pw.println(responseJson);

                return new Response(true, "Player received your move");


                } else {
                    return new Response(false, "Failed to send request: Player is no longer online");
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to send request to player " + recipientPlayerName, ex);
                return new Response(false, "Failed to send request to player");
            }
        });

        try {
            // Get the result of the asynchronous task
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Error while processing request to player", e);
            return new Response(false, "An error occurred while processing the request");
        }

    } 

    private Response getOtherPlayerResponse(Request request) {
        System.out.println("------------------------------------getOtherPlayerResponse");

        // Player initiating the request
        Player playerWhoSendRequest = request.getPlayer();
        List<Player> players= new ArrayList() ;
      
        // Validate player data
        if (playerWhoSendRequest == null || playerWhoSendRequest.getUserName() == null) {
            System.out.println("Player data is missing1111111111111111111");
            return new Response(false, "Player data is missing");
        }

        String playerIWillPlayWithUserName = request.getMessage();  // Player to play with

        // Validate the target player's username
        if (playerIWillPlayWithUserName == null || playerIWillPlayWithUserName.trim().isEmpty()) {
            System.out.println("Target player's username is missing");

            return new Response(false, "Target player's username is missing");
        }

        // Check if the target player is online
        if (!onlinePlayers.containsKey(playerIWillPlayWithUserName)) {
            System.out.println("playerIWillPlayWithUserName is not online");
            return new Response(false, "Player " + playerIWillPlayWithUserName + " is not online");
        }

        // Retrieve the player object for the target player
        Player playerIWillPlayWith = PlayerDAO.selectPlayerByUserName(playerIWillPlayWithUserName);
        if (playerIWillPlayWith == null) {
            System.out.println("playerIWillPlayWithUserName is not found");
            return new Response(false, "Player " + playerIWillPlayWithUserName + " is not found");
        }

        // Asynchronously handle the game request
        Future<Response> future = executorService.submit(() -> {
            try {
                // Find the target player's socket to send the response
                Socket socket = findSocketByPlayerName(playerIWillPlayWithUserName);
                if (socket != null) {
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    String responseJson;

                    if (request.getAction() == Action.yes) {
                        // Update both players' statuses to "in-game"
                        PlayerDAO.updateStatus(playerWhoSendRequest, Status.INGAME);
                        PlayerDAO.updateStatus(playerIWillPlayWith, Status.INGAME);
                        players.add(playerIWillPlayWith);
                        players.add(playerWhoSendRequest);
                        
                        responseJson = gson.toJson(new Response(true, "has started the game", players));
                    } else {
                        responseJson = gson.toJson(new Response(true, "declined the game request", players));
                    }

                    System.out.println("Response JSON===========================================: " + responseJson);
                    pw.println(responseJson);

                    // Return the response based on the action
                    if (request.getAction() == Action.yes) {
                        return new Response(true, "has started the game", players);
                    } else {
                        return new Response(true, "declined the game request", players);
                    }

                } else {
                    return new Response(false, "Failed to send request: Player is no longer online");
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to send request to player " + playerIWillPlayWithUserName, ex);
                return new Response(false, "Failed to send request to player");
            }
        });

        try {
            // Get the result of the asynchronous task
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Error while processing request to player", e);
            return new Response(false, "An error occurred while processing the request");
        }

    }

//    
//    public static void sendJsonToAllPlayers() {
//        Gson gson = new Gson();
//        LOGGER.info("Sending server down message to all players");
//
//        // Take a snapshot of online players to avoid ConcurrentModificationException
//        Map<String, Socket> snapshot = new ConcurrentHashMap<>(onlinePlayers);
//
//        for (Map.Entry<String, Socket> entry : snapshot.entrySet()) {
//            String playerName = entry.getKey();
//            Socket socket = entry.getValue();
//            try (PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {
//                String jsonResponse = gson.toJson(new Response(false, "ServerisDown"));
//                pw.println(jsonResponse);  // PrintWriter automatically adds a newline
//            } catch (IOException ex) {
//                LOGGER.log(Level.SEVERE, "Error sending message to player " + playerName, ex);
//                // Remove player from the online list if an error occurs
//                onlinePlayers.remove(playerName);
//            }
//        }
//
//        // Optionally log the number of successful sends or other metrics
//        LOGGER.info("Messages sent to all players.");
//    }
//    private Response sendResignRequest(Player request) {
//        BufferedWriter bwToPlayer = null;
//        String player2;
//        try {
//            player2 = request.getUserName();
//            bwToPlayer = new BufferedWriter(new OutputStreamWriter(onlinePlayers.get(player2).getOutputStream()));
//            String responseJson = gson.toJson(new Response(true, player2 + "vresigned", null));
//            bwToPlayer.write(responseJson);
//            bwToPlayer.newLine();
//            bwToPlayer.flush();
//        } catch (IOException ex) {
//            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
//            return new Response(false, "Failed to send resign request", null);
//        }
//        return new Response(true, player2 + " has won", null);
//
//    }
//
//    private Response sendMove(Player request) {
//        BufferedWriter bwToPlayer = null;
//        try {
//            String player2 = request.getUserName();
//            bwToPlayer = new BufferedWriter(new OutputStreamWriter(onlinePlayers.get(player2).getOutputStream()));
//            System.out.println(player2);
//            Response response = new Response(true, "move" + request.getMessage(), null);
//            String responseJson = gson.toJson(new Response(true, "move" + request.getMessage(), null));
//            System.out.println(response.toString());
//            System.out.println("Player 1 played " + request.getMessage() + " to " + request.getUserName());
//            bwToPlayer.write(responseJson);
//            bwToPlayer.newLine();
//            bwToPlayer.flush();
//        } catch (IOException ex) {
//            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
//            return new Response(false, "Failed to send move", null);
//        }
//        return new Response(true, "Player2 received your move", null);
//    }
//
//    private Response updateScore(Player request) {
//        if (PlayerDAO.isPointsUpdated(request)) {
//            PlayerDAO.updateStatus(request, Status.ONLINE);
//            return new Response(true, "Your points have been updated", null);
//        } else {
//            return new Response(false, "Points update failed", null);
//        }
//    }
//
//   
//
//    private void addInGamePlayers(InOnlineResponse onlineResponse, Player player) {
//        try {
//            ResultSet rs = PlayerDAO.selectInGame();
//            while (rs.next()) {
//                Player p = new Player();
//                p.setId(rs.getInt("ID"));
//                p.setUserName(rs.getString("USERNAME"));
//                p.setPoints(rs.getInt("POINTS"));
//                p.setStatus(Status.valueOf(rs.getString("STATUS")));
//                onlineResponse.getPlayers().add(p);
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
//            onlineResponse.setStatus(false);
//            onlineResponse.setMessage("Failed to retrieve players status");
//        }
//    }
    private void addToMap(String username, Socket socket) {
        onlinePlayers.put(username, socket);
    }

    public static void clearOnlinePlayers() {
        onlinePlayers.clear();
    }

    public boolean isPlayerInMap(String username) {
        return onlinePlayers.containsKey(username);
    }

//    public static void printOnlinePlayers() {
//        for (Map.Entry<String, Socket> entry : onlinePlayers.entrySet()) {
//            System.out.println("Player: " + entry.getKey() + " " + entry.getValue());
//        }
//    }
//    public void close() {
//        running = false;
//        try {
//            if (br != null) {
//                br.close();
//            }
//            if (pw != null) {
//                pw.close();
//            }
//            if (cs != null && !cs.isClosed()) {
//                cs.close();
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    private void addOnlinePlayers(InOnlineResponse onlineResponse, Player player) {
//        try {
//            System.out.println("Requesting player: " + player.userName);
//            ResultSet rs = PlayerDAO.selectOnline();
//            while (rs.next()) {
//                String currentUsername = rs.getString("USERNAME").trim();
//                System.out.println("Checking player: " + currentUsername);
//
//                if (currentUsername.equalsIgnoreCase(player.userName.trim())) {
//                    System.out.println("Skipping player: " + player.userName);
//                    continue;
//                }
//
//                Player p = new Player();
//                p.setId(rs.getInt("ID"));
//                p.setUserName(currentUsername);
//                p.setPoints(rs.getInt("POINTS"));
//                p.setStatus(Status.valueOf(rs.getString("STATUS")));
//                onlineResponse.getPlayers().add(p);
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
//            onlineResponse.setStatus(false);
//            onlineResponse.setMessage("Failed to retrieve players status");
//        }
//    }
}
