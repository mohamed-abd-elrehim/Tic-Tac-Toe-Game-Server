/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xoserver;

/**
 *
 * @author COMPUMARTS
 */
/**
 *
 * @author COMPUMARTS
 */
import DTOS.Player;
import enumstatus.EnumStatus.Status;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.derby.jdbc.ClientDriver;

/**
 *
 * @author COMPUMARTS
 */
public class PlayerDAO {

    static Connection con;
    static int result;
    static ResultSet rs;
    static boolean isDone;

    static {
        try {
            DriverManager.registerDriver(new ClientDriver());
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/Player", "root", "root");

        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static synchronized boolean delete(Player p) {
        try {

            PreparedStatement pst = con.prepareStatement("delete from PLAYER where ID = ?");
            pst.setInt(1, p.id);
            result = pst.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (result <= 0) {
            isDone = false;
        } else {
            isDone = true;
        }
        return isDone;
    }

    static synchronized boolean insert(Player p) {
        try {
            //int result;
            PreparedStatement pst = con.prepareStatement("INSERT INTO PLAYER (userName, status, password, points) VALUES (?, ?, ?, ?)");
            //pst.setInt(1, p.id);
            pst.setString(1, p.userName);
            pst.setString(2, String.valueOf(p.status));
            pst.setString(3, p.password);
            pst.setInt(4, p.points);

            result = pst.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (result <= 0) {
            isDone = false;
        } else {
            isDone = true;
        }
        return isDone;
    }

    static synchronized boolean update(Player p) {
        try {
            PreparedStatement pst = con.prepareStatement("UPDATE PLAYER SET USERNAME = ?, STATUS = ?, PASSWORD = ?, POINTS = ? WHERE ID = ?");
            pst.setString(1, p.userName);
            pst.setString(2, String.valueOf(p.status));
            pst.setString(3, p.password);
            pst.setInt(4, p.points);
            pst.setInt(5, p.id);
            result = pst.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (result <= 0) {
            isDone = false;
        } else {
            isDone = true;
        }
        return isDone;
    }

    static synchronized ResultSet selectInGame() {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER where STATUS = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "INGAME");
            rs = pst.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        //resurlt = pst.executeQuerey();
        return rs;

    }

    static synchronized ResultSet selectOnline() {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER where STATUS = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "ONLINE");
            rs = pst.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        //resurlt = pst.executeQuerey();
        return rs;

    }

    static synchronized ResultSet selectOffline() {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER where STATUS = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "OFFLINE");
            rs = pst.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        //resurlt = pst.executeQuerey();
        return rs;

    }

    static synchronized ResultSet selectAll() {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            rs = pst.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    static synchronized ResultSet select(Player p) {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER where id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setInt(1, p.id);
            rs = pst.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        //resurlt = pst.executeQuerey();
        return rs;
    }

    static synchronized int updateStatus(Player p, Status status) {
        try {
            PreparedStatement pst
                    = con.prepareStatement("UPDATE PLAYER SET STATUS = ? WHERE ID = ?");

            pst.setString(1, String.valueOf(status));
            pst.setInt(2, p.id);
            result = pst.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    static synchronized boolean isStatusUpdated(Player p, Status status) {
        int updateResult = updateStatus(p, status);
        if (updateResult <= 0) {
            return false;
        } else {
            return true;
        }

    }

    static synchronized int updatePoints(Player p) {
        try {
            PreparedStatement pst
                    = con.prepareStatement("UPDATE PLAYER SET POINTS = ? WHERE ID = ?");

            pst.setInt(1, p.points);
            pst.setInt(2, p.id);
            result = pst.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    static synchronized boolean isPointsUpdated(Player p) {
        int updateResult = updatePoints(p);
        if (updateResult <= 0) {
            return false;
        } else {
            return true;
        }

    }

    static synchronized boolean selectLogin(Player p) {
        boolean isLoggedIn = false;
        ResultSet rs = null;
        try {
            //System.out.println("Attempting login with: " + p.toString());
            // String query = "SELECT * FROM PLAYER WHERE USERNAME = ? AND PASSWORD = ?";
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER WHERE USERNAME = ? AND PASSWORD = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            //System.out.println("Query: " + query);
            pst.setString(1, p.userName.trim());
            pst.setString(2, p.password.trim());
            //System.out.println("PreparedStatement: " + pst.toString());
            rs = pst.executeQuery();
            if (!rs.next()) {
                System.out.println("No matching user found.");
            } else {
                //System.out.println("Username: " + rs.getString("USERNAME") + "  Password: " + rs.getString("PASSWORD"));
                p.id = rs.getInt("ID");
                p.points = rs.getInt("POINTS");
                updateStatus(p, Status.ONLINE);
                System.out.println(p.toString());
                isLoggedIn = true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isLoggedIn;
    }

    static synchronized boolean isUserLoggedin(Player p) {
        return selectLogin(p);
    }

    static synchronized boolean isUserNameTaken(Player p) {
        boolean isTaken = true;
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER WHERE USERNAME = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, p.userName.trim());
            rs = pst.executeQuery();
            if (rs.next()) {
                isTaken = true;
            } else {
                isTaken = false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isTaken;
    }

    static synchronized int getIngameNumber() {
        int count = 0;
        try {

            PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM player WHERE status = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "INGAME");
            rs = pst.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = -1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }

    static int getOfflineNumber() {
        int count = 0;
        try {

            PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM player WHERE status = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "OFFLINE");
            rs = pst.executeQuery();
            if (rs.next()) {

                count = rs.getInt(1);

            } else {
                count = -1;

            }
        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }

    static int getOnlineNumber() {
        int count = 0;
        try {

            PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM player WHERE status = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "ONLINE");
            rs = pst.executeQuery();
            if (rs.next()) {

                count = rs.getInt(1);

            } else {
                count = -1;

            }
        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }

    static void logOutAllPlayers() {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM player WHERE status = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "ONLINE");
            rs = pst.executeQuery();
            while (rs.next()) {
                 pst = con.prepareStatement("UPDATE PLAYER SET STATUS = ? WHERE ID = ?");
                pst.setString(1, String.valueOf(Status.OFFLINE));
                pst.setInt(2, rs.getInt("ID"));
                result = pst.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

/*
class GameDTO {

    int id;
    String userNamePlayerOne;
    String userNamePlayerTwo;
    String status;
}
 */
