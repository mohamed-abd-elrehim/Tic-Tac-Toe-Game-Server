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
import DTOS.Status;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.derby.jdbc.ClientDriver;

/**
 *
 * @author COMPUMARTS
 */
public class PlayerDAO {

    static Connection con;
    static int result;
    static ResultSet rs;

    static {
        try {
            DriverManager.registerDriver(new ClientDriver());
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/Player", "root", "root");

        } catch (SQLException ex) {
                ex.printStackTrace();   
        }
    }

    static boolean selectLogin(Player p) {
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
                p.status = Status.ONLINE;
                updateStatus(p.id,p.status.toString());
                System.out.println(p.toString());
                isLoggedIn = true;
            }

        } catch (SQLException ex) {
        }
        return isLoggedIn;
    }

    static boolean isUserLoggedin(Player p) {
        return selectLogin(p);
    } 
    
    static int delete(Player p) {
        try {

            PreparedStatement pst = con.prepareStatement("delete from PLAYER where ID = ?");
            pst.setInt(1, p.id);
            result = pst.executeUpdate();

        } catch (SQLException ex) {
                   ex.printStackTrace();
        }
        return result;
    }

    static int insert(Player p) {
        try {
            
            PreparedStatement pst = con.prepareStatement("INSERT INTO PLAYER VALUES (?,?,?,?,?)");
            pst.setInt(1, p.id);
            pst.setString(1, p.userName);
            pst.setString(2, p.status.toString());
            pst.setString(3, p.password);
            pst.setInt(4, p.points);

            result = pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    static int update(Player p) {
        try {
            PreparedStatement pst = con.prepareStatement("UPDATE PLAYER SET USERNAME = ?, STATUS = ?, PASSWORD = ?, POINTS = ? WHERE ID = ?");
            pst.setString(1, p.userName);
            pst.setString(2, p.status.toString());
            pst.setString(3, p.password);
            pst.setInt(4, p.points);
            pst.setInt(5, p.id);
            result = pst.executeUpdate();

        } catch (SQLException ex) {
                    ex.printStackTrace(); 
        }
        return result;
    }

    static ResultSet selectInGame() {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER where STATUS = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "INGAME");
            rs = pst.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
       
        return rs;

    }

    static ResultSet selectOnline() {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER where STATUS = ?", 
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "ONLINE");
            rs = pst.executeQuery();
        } catch (SQLException ex) {
                   ex.printStackTrace();
        }
        
        return rs;

    }

    static ResultSet selectOffline() {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER where STATUS = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, "OFFLINE");
            rs = pst.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return rs;

    }

    public static ResultSet selectAll() {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = pst.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return rs;

    }

    
   public static ResultSet select(int id) {
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM PLAYER WHERE ID = ?", 
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setInt(1, id);
            rs = pst.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return rs;
    }

    
   public static int updateStatus(int id,String status) {
        try {
            PreparedStatement pst = con.prepareStatement("UPDATE PLAYER SET  STATUS = ? WHERE ID = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, status);
            pst.setInt(2, id);
            result = pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return result;
    }

    public int updatePoints(int points,int id) {
         try {
            PreparedStatement pst = con.prepareStatement("UPDATE PLAYER SET  POINTS = ? WHERE ID = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setInt(1, points);
            pst.setInt(2, id);
            result = pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return result;
        
    }

}
