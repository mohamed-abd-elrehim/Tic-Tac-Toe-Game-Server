/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DTOS;

/**
 *
<<<<<<< HEAD
 * @author COMPUMARTS
 */

public class Response {
    public boolean isDone;
=======
 * @author Smart
 */
public class Response {
     public boolean isDone;
>>>>>>> e4d8fb212146b3eb0568555f03d84d9b43a38d8f
    public String message;
    public Player player;

    public Response(boolean isDone, String message, Player player) {
        this.isDone = isDone;
        this.message = message;
        this.player = player;
    }

    // Getters and Setters
    public boolean  getStatus() {
        return isDone;
    }

    public void setStatus(boolean isDone) {
        this.isDone = isDone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public String toString() {
        return "Response{" +
                "isDone='" + isDone + '\'' +
                ", message='" + message + '\'' +
                ", player=" + player +
                '}';
    }
}
