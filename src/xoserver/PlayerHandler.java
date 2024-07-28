package xoserver;

import DTOS.InOnlineResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import DTOS.*;
import enumstatus.EnumStatus.Status;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(PlayerHandler.class.getName());
    private final BufferedReader br;
    private final PrintWriter pw;
    private final Gson gson;
    private final Socket cs;
    static Map<String, Socket> onlinePlayers = new ConcurrentHashMap<>();
    private final FXMLDocumentBase fxmlDocumentBase;

    public PlayerHandler(Socket cs, FXMLDocumentBase fxmlDocumentBase) throws IOException {
        this.cs = cs;
        this.br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
        this.pw = new PrintWriter(cs.getOutputStream(), true);
        this.gson = new Gson();
        this.fxmlDocumentBase = fxmlDocumentBase;
        fxmlDocumentBase.updateUI();
    }

    @Override
    public void run() {
        try {
            String jsonString;
            while ((jsonString = br.readLine()) != null) {
                Player request = gson.fromJson(jsonString, Player.class);
                Object response = handleRequest(request);
                String responseJson = gson.toJson(response);
                pw.println(responseJson);
            }
        } catch (SocketException e) {

            System.out.println("connection reset by client");
        } catch (IOException e) {

            System.out.println("I/O error in player handler");
        } catch (JsonSyntaxException e) {

            LOGGER.log(Level.SEVERE, "Error parsing JSON", e);
        } finally {
            closeResources();
            fxmlDocumentBase.updateUI();
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

    private Object handleRequest(Player request) {
        if (request == null || request.getAction() == null) {
            return new Response(false, "Invalid request", null);
        }

        switch (request.getAction().toLowerCase()) {
            case "login":
                return loginPlayer(request);
            case "logout":
                return logoutPlayer(request);
            case "register":
                return registerPlayer(request);
            case "gamelobby":
                return getPlayersStatus(request);
            case "wanttoplay":
                return sendRequestToPlayer(request);
            case "yes":
            case "no":
                return getOtherPlayerResponse(request);
            case "move":
                return sendMove(request);
            case "resign":
                return sendResignRequest(request);
            case "updatescore":
                return updateScore(request);
            default:
                return new Response(false, "Unknown action", null);
        }
    }

    private Response sendResignRequest(Player request) {
        BufferedWriter bwToPlayer = null;
        String player2;
        try {
            player2 = request.getUserName();
            bwToPlayer = new BufferedWriter(new OutputStreamWriter(onlinePlayers.get(player2).getOutputStream()));
            String responseJson = gson.toJson(new Response(true, player2 + "vresigned", null));
            bwToPlayer.write(responseJson);
            bwToPlayer.newLine();
            bwToPlayer.flush();
        } catch (IOException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
            return new Response(false, "Failed to send resign request", null);
        }
        return new Response(true, player2+" has won", null);

    }

    private Response sendMove(Player request) {
        BufferedWriter bwToPlayer = null;
        try {
            String player2 = request.getUserName();
            bwToPlayer = new BufferedWriter(new OutputStreamWriter(onlinePlayers.get(player2).getOutputStream()));
            System.out.println(player2);
            Response response = new Response(true, "move" + request.getMessage(), null);
            String responseJson = gson.toJson(new Response(true, "move" + request.getMessage(), null));
            System.out.println(response.toString());
            System.out.println("Player 1 played "+request.getMessage()+" to "+request.getUserName());
            bwToPlayer.write(responseJson);
            bwToPlayer.newLine();
            bwToPlayer.flush();
        } catch (IOException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
            return new Response(false, "Failed to send move", null);
        }
        return new Response(true, "Player2 received your move", null);
    }

    private Response updateScore(Player request) {
        if (PlayerDAO.isPointsUpdated(request)) {
            PlayerDAO.updateStatus(request, Status.ONLINE);
            return new Response(true, "Your points have been updated", null);
        } else {
            return new Response(false, "Points update failed", null);
        }
    }

    private Response sendRequestToPlayer(Player request) {
        BufferedWriter bwToPlayer2 = null;
        System.out.println("Inside send request method ");
        String player2;
        try {
            Player p = new Player();
            String player1 = request.getUserName();
            p.setUserName(player1);
            player2 = request.getMessage();
            bwToPlayer2 = new BufferedWriter(new OutputStreamWriter(onlinePlayers.get(player2).getOutputStream()));
            //System.out.println("P2 Socket " + String.valueOf(onlinePlayers.get(player2)) + "P1 Socket " + String.valueOf(onlinePlayers.get(player1)));
            String responseJson = gson.toJson(new Response(true, player1 + " wants to play with you", null));
            //System.out.println(responseJson);
            bwToPlayer2.write(responseJson);
            bwToPlayer2.newLine();
            bwToPlayer2.flush();
        } catch (IOException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
            return new Response(false, "Failed to get player response", null);
        }
        //System.out.println("This socket = " + onlinePlayers.get(player2));
        return new Response(true, "Player2 received your message", null);
    }

    private Response getOtherPlayerResponse(Player request) {
        BufferedWriter bwToPlayer = null;
        String player2;
        String player1;
        System.out.println("in getOtherPlayerResponse method");
        try {
            Player p1 = new Player();
            Player p2 = new Player();
            player1 = request.getUserName();
            p1.setUserName(player1);
            player2 = request.getMessage();
            p2.setUserName(player2);
            bwToPlayer = new BufferedWriter(new OutputStreamWriter(onlinePlayers.get(player2).getOutputStream()));

            String responseJson;
            if (request.getAction().equals("yes")) {
                PlayerDAO.updateStatus(p1, Status.INGAME);
                PlayerDAO.updateStatus(p2, Status.INGAME);
                responseJson = gson.toJson(new Response(true, player1 + ": start game", p1));
            } else {
                responseJson = gson.toJson(new Response(false, player1 + " is busy", p1));
            }
            System.out.println(responseJson);
            bwToPlayer.write(responseJson);
            bwToPlayer.newLine();
            bwToPlayer.flush();
        } catch (IOException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
            return new Response(false, "Failed to get player response", null);
        }

        if (request.getAction().equals("yes")) {
            //System.out.println("response is no");
            return new Response(true, player2 + ": start game", null);

        } else {
            return new Response(false, "Game request decliened", null);
        }
    }

    private Response registerPlayer(Player player) {
        boolean isDuplicate = PlayerDAO.isUserNameTaken(player);
        if (!isDuplicate) {
            boolean registerSuccess = PlayerDAO.insert(player);
            if (registerSuccess) {
                System.out.println("Register successful");
                return new Response(true, "Registration successful", player);
            } else {
                return new Response(false, "Registration failed", player);
            }
        } else {
            return new Response(false, "Player already has an account", player);
        }
    }

    private Response loginPlayer(Player player) {
        //printOnlinePlayers();
        fxmlDocumentBase.updateUI();

        if (isPlayerInMap(player.getUserName())) {
            System.out.println("Player " + player.getUserName() + " is already logged in.");
            return new Response(false, "Player is already logged in", player);
        } else {

            boolean loginSuccess = PlayerDAO.isUserLoggedin(player);
            //System.out.println(player.toString());
            if (loginSuccess) {
                addToMap(player.getUserName(), cs);
                System.out.println("Login Successful");
                return new Response(true, "Login successful", player);
            } else {
                System.out.println("Login failed");
                return new Response(false, "Login failed", player);
            }
        }
    }

    private Response logoutPlayer(Player player) {
        fxmlDocumentBase.updateUI();
        boolean logoutSuccess = PlayerDAO.isStatusUpdated(player, Status.OFFLINE);
        if (logoutSuccess) {
            Response temp = new Response(true, "Logout successful", player);
            onlinePlayers.remove(player.getUserName());
            return temp;
        } else {
            return new Response(false, "Logout failed", player);
        }
    }

    private InOnlineResponse getPlayersStatus(Player player) {
        InOnlineResponse onlineResponse = new InOnlineResponse(true, "player List", new ArrayList<>());
        addOnlinePlayers(onlineResponse, player);
        return onlineResponse;
    }

    private void addOnlinePlayers(InOnlineResponse onlineResponse, Player player) {
        try {
            ResultSet rs = PlayerDAO.selectOnline();
            while (rs.next()) {
                if (rs.getString("USERNAME").equals(player.userName)) {
                    continue;
                }
                Player p = new Player();
                p.setId(rs.getInt("ID"));
                p.setUserName(rs.getString("USERNAME"));
                p.setPoints(rs.getInt("POINTS"));
                p.setStatus(Status.valueOf(rs.getString("STATUS")));
                onlineResponse.getPlayers().add(p);
                onlineResponse.toString();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
            onlineResponse.setStatus(false);
            onlineResponse.setMessage("Failed to retrieve players status");
        }
    }

    private void addInGamePlayers(InOnlineResponse onlineResponse, Player player) {
        try {
            ResultSet rs = PlayerDAO.selectInGame();
            while (rs.next()) {
                Player p = new Player();
                p.setId(rs.getInt("ID"));
                p.setUserName(rs.getString("USERNAME"));
                p.setPoints(rs.getInt("POINTS"));
                p.setStatus(Status.valueOf(rs.getString("STATUS")));
                onlineResponse.getPlayers().add(p);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
            onlineResponse.setStatus(false);
            onlineResponse.setMessage("Failed to retrieve players status");
        }
    }

    private void addToMap(String username, Socket socket) {
        onlinePlayers.put(username, socket);
    }

    public static void sendJsonToAllPlayers() {
        Gson gson = new Gson();
        System.out.println("Server down");

        Map<String, Socket> snapshot = new ConcurrentHashMap<>(onlinePlayers);

        for (Map.Entry<String, Socket> entry : snapshot.entrySet()) {
            Socket socket = entry.getValue();
            try (
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                //System.out.println("Sending JSON to player: " + entry.getKey());
                String jsonResponse = gson.toJson(new Response(false, "Server is Down", null));
                //pw.println(jsonResponse);
                bw.write(jsonResponse);
                bw.newLine();
                bw.flush();
            } catch (IOException ex) {
                System.err.println("Error sending message to player " + entry.getKey() + ": " + ex.getMessage());

                onlinePlayers.remove(entry.getKey());
            }
        }
    }

//    public static void printOnlinePlayers() {
//        for (Map.Entry<String, Socket> entry : onlinePlayers.entrySet()) {
//            System.out.println("Player: " + entry.getKey() + " " + entry.getValue());
//        }
//    }
    public static void clearOnlinePlayers() {
        onlinePlayers.clear();
    }

    public boolean isPlayerInMap(String username) {
        // Corrected to check if the player is in the map
        return onlinePlayers.containsKey(username);
    }

}
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
