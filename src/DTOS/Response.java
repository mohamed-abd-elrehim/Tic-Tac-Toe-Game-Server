/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DTOS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author COMPUMARTS
 */

public class Response implements Serializable{

    private boolean isDone;
    private String message;
    private List<Player> players; 
    private Player player; 


    public Response(boolean isDone, String message) {
        this(isDone, message, null, null);
    }

  
    public Response(boolean isDone, String message, List<Player> players) {
        this(isDone, message, players, null);
    }
   
    public Response(boolean isDone, String message, Player player) {
        this(isDone, message, null, player);
    }
    
    public Response(boolean isDone, String message, List<Player> players, Player player) {
        this.isDone = isDone;
        this.message = message;
        this.players = players != null ? players : Collections.emptyList();
        this.player = player;
    }

    // Getters and Setters
    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        this.isDone = done;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players =  players != null ? players : Collections.emptyList();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public String toString() {
        return "ServerResponse{" +
                "isDone=" + isDone +
                ", message='" + message + '\'' +
                ", players=" + players +
                ", player=" + player +
                '}';
    }
}
