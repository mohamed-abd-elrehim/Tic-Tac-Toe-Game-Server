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

    static {
        try {
            DriverManager.registerDriver(new ClientDriver());
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/Player", "root", "root");

        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



//    static int update(PlayerDTO p) {
//        try {
//            PreparedStatement pst
//                    = con.prepareStatement("UPDATE PLAYER SET USERNAME = ?, STATUS = ?, PASSWORD = ?, POINTS = ? WHERE ID = ?");
//            pst.setString(1, p.userName);
//            pst.setString(2, p.status.toString());
//            pst.setString(3, p.password);
//            pst.setInt(4, p.points);
//            pst.setInt(5, p.id);
//            result = pst.executeUpdate();
//
//        } catch (SQLException ex) {
//            Logger.getLogger(PlayerDTO.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }

    

}


/*
class GameDTO {

    int id;
    String userNamePlayerOne;
    String userNamePlayerTwo;
    String status;
}
*/

