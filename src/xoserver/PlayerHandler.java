package xoserver;

<<<<<<< HEAD
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
=======
/**
 *
 * @author COMPUMARTS
 */
import DTOS.Player;
import DTOS.Response;
import java.io.DataInputStream;
>>>>>>> e4d8fb212146b3eb0568555f03d84d9b43a38d8f
import java.io.IOException;
import java.net.Socket;
import DTOS.*;
import enumstatus.EnumStatus;
import enumstatus.EnumStatus.Status;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerHandler extends Thread {

    private final BufferedReader br;
    private final PrintWriter pw;
    private final Gson gson;
    private final Socket cs;

    public PlayerHandler(Socket cs) throws IOException {
        this.cs = cs;
        this.br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
        this.pw = new PrintWriter(cs.getOutputStream(), true);
        this.gson = new Gson();
        start();
    }
    
      private Response loginPlayer(Player player) {

        boolean loginSuccess = PlayerDAO.isUserLoggedin(player);
        System.out.println(player.toString());
        if (loginSuccess) {
            System.out.println("Login Successful");
            return new Response(true, "Login successful", player);

        } else {
            System.out.println("Login failed");
            return new Response(false, "Login failed", player);
        }
    }
    @Override
    public void run() {
        try {
            while (true) {
                String jsonString = br.readLine();
                if (jsonString == null) {
                    break;
                }
                Player player = gson.fromJson(jsonString, Player.class);
                Object response = handlePlayer(player);
                String responseJson = gson.toJson(response);
                pw.println(responseJson);
            }
        } catch (IOException | JsonSyntaxException ex) {
            ex.printStackTrace();
        } finally {
            try {
                br.close();
                pw.close();
                cs.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Object handlePlayer(Player player) {
        switch (player.getAction().toLowerCase()) {
            case "login":
                return loginPlayer(player);
            case "register":
                return registerPlayer(player);
            case "onlinemode":
                return getPlayersStatus();
            default:
                return new Response(false, "Unknown action", player);
        }
    }

    private Response loginPlayer(Player player) {

        boolean loginSuccess = KareemAshraf.isUserLoggedin(player);
        System.out.println(player.toString());
        if (loginSuccess) {
            System.out.println("Login Successful");
            return new Response(true, "Login successful", player);

        } else {
            System.out.println("Login failed");
            return new Response(false, "Login failed", player);
        }
    }

    private Response registerPlayer(Player player) {

        boolean isDuplicate = KareemAshraf.isUserNameTaken(player);
        if (!isDuplicate) {
            boolean registerSuccess = KareemAshraf.insert(player);
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

    private InOnlineResponse getPlayersStatus() {
        InOnlineResponse onlineResponse = new InOnlineResponse(true, "Players Status sent successfully", new ArrayList<>());
        addOnlinePlayers(onlineResponse);
        addInGamePlayers(onlineResponse);
        return onlineResponse;
    }
    
    
    private void addOnlinePlayers(InOnlineResponse onlineResponse) {
        try {
            ResultSet rs = null;

            rs = KareemAshraf.selectOnline();
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

    private void addInGamePlayers(InOnlineResponse onlineResponse) {
        try {
            ResultSet rs = null;

            rs = KareemAshraf.selectOnline();
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

    

    
}


