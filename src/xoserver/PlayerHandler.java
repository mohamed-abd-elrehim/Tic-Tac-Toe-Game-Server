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
    private final FXMLDocumentBase fxmlDocumentBase; // Add this line

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
            // This handles cases where the socket connection is forcibly closed
            LOGGER.log(Level.WARNING, "Connection reset by client", e);
        } catch (IOException e) {
            // This handles general I/O exceptions
            LOGGER.log(Level.SEVERE, "I/O error in player handler", e);
        } catch (JsonSyntaxException e) {
            // This handles JSON parsing errors
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
            case "accept":
                return getOtherPlayerResponse(request);
            case "move":
                return sendMove(request);
            case "updatescore":
                return updateScore(request);
            default:
                return new Response(false, "Unknown action", null);
        }
    }

    private Response sendMove(Player request) {
        PrintWriter pwToplayer2 = null;
        try {
            String player2 = request.getUserName();
            pwToplayer2 = new PrintWriter(onlinePlayers.get(player2).getOutputStream(), true);
            pwToplayer2.write(request.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
            return new Response(false, "Failed to send move", null);
        } finally {
            if (pwToplayer2 != null) {
                pwToplayer2.close();
            }
        }
        return new Response(true, "Player2 received your move", null);
    }

    private Response updateScore(Player request) {
        if (PlayerDAO.isPointsUpdated(request)) {
            return new Response(true, "Your points have been updated", null);
        } else {
            return new Response(false, "Points update failed", null);
        }
    }

    private Response sendRequestToPlayer(Player request) {
        
        System.out.println("1");
        PrintWriter pwToplayer2 = null;
        System.out.println("Inside getplayer");
        try {
            Player p = new Player();
            String player1 = request.getUserName();
            p.setUserName(player1);
            String player2 = request.getMessage();
            pwToplayer2 = new PrintWriter(onlinePlayers.get(player2).getOutputStream(), true);
            String responseJson = gson.toJson(new Response(true, player2, p));
            pwToplayer2.write(responseJson);
        } catch (IOException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
            return new Response(false, "Failed to get player response", null);
        }
        return new Response(true, "Player2 received your message", null);
    }

    private Response getOtherPlayerResponse(Player request) {
        PrintWriter pwToplayer2 = null;
        //System.out.println("Inside getplayer");
        try {
            Player p = new Player();
            String player1 = request.getUserName();
            p.setUserName(player1);
            String player2 = request.getMessage();
            pwToplayer2 = new PrintWriter(onlinePlayers.get(player2).getOutputStream(), true);
            String responseJson = gson.toJson(new Response(true, "Start Game", p));
            pwToplayer2.write(responseJson);
        } catch (IOException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
            return new Response(false, "Failed to get player response", null);
        }
        return new Response(true, "Start Game", null);

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
        printOnlinePlayers();
        // Check if the player is already logged in
        if (isPlayerInMap(player.getUserName())) {
            System.out.println("Player " + player.getUserName() + " is already logged in.");
            return new Response(false, "Player is already logged in", player);
        } else {

            boolean loginSuccess = PlayerDAO.isUserLoggedin(player);
            System.out.println(player.toString());
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
//    private Response loginPlayer(Player player) {
//        boolean loginSuccess = PlayerDAO.isUserLoggedin(player);
//        System.out.println(player.toString());
//        if (loginSuccess) {
//            addToMap(player.getUserName(), cs);
//            System.out.println("Login Successful");
//            return new Response(true, "Login successful", player);
//        } else {
//            System.out.println("Login failed");
//            return new Response(false, "Login failed", player);
//        }
//    }

    private Response logoutPlayer(Player player) {
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
        System.out.println("Received request for 'gamelobby' action.");

        InOnlineResponse onlineResponse = new InOnlineResponse(true, "Players Status sent successfully", new ArrayList<>());

        // Debug statement to check if players are being added correctly
        addOnlinePlayers(onlineResponse, player);

        // Check if `addOnlinePlayers` correctly populates the response
        System.out.println("Online players response: " + onlineResponse);

        return onlineResponse;
    }

//    private InOnlineResponse getPlayersStatus(Player player) {
//        InOnlineResponse onlineResponse = new InOnlineResponse(true, "Players Status sent successfully", new ArrayList<>());
//        addOnlinePlayers(onlineResponse, player);
//        return onlineResponse;
//    }
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

    public boolean isPlayerInMap(String username) {
        // Corrected to check if the player is in the map
        return onlinePlayers.containsKey(username);
    }

    public static void sendJsonToAllPlayers() {
        Gson gson = new Gson();
        System.out.println("Server down");

        // Create a copy of the entries to avoid concurrent modification issues
        Map<String, Socket> snapshot = new ConcurrentHashMap<>(onlinePlayers);

        for (Map.Entry<String, Socket> entry : snapshot.entrySet()) {
            Socket socket = entry.getValue();
            try (
                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                System.out.println("Sending JSON to player: " + entry.getKey());
                String jsonResponse = gson.toJson(new Response(false, "Server is Down", null));
                pw.println(jsonResponse);
                bw.write(jsonResponse);
                bw.newLine(); // Ensure the message is properly terminated
                bw.flush();
            } catch (IOException ex) {
                System.err.println("Error sending message to player " + entry.getKey() + ": " + ex.getMessage());
                // Optionally remove the socket from the map if it fails to send
                onlinePlayers.remove(entry.getKey());
            }
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
    public static void printOnlinePlayers() {
        for (Map.Entry<String, Socket> entry : onlinePlayers.entrySet()) {
            System.out.println("Player: " + entry.getKey() + " " + entry.getValue());
        }
    }

    public static void clearOnlinePlayers() {
        onlinePlayers.clear();
    }
}
