/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xoserver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author COMPUMARTS
 */
public class XOServer extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = new FXMLDocumentBase(stage) ;
        
        Scene scene = new Scene(root);
        stage.setTitle("Tic Tac Toe Server");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
