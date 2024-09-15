package DTOS;


import DTOS.Player;
import enumstatus.EnumPlayerAction;

public class Request {

    private EnumPlayerAction.Action action;
    private Player player; // New field
    private String message; // New field
    private String move; // New field

    // Default Constructor
    public Request() {
    }

    // Parameterized Constructor
    public Request(EnumPlayerAction.Action action, Player player) {
        this.action = action;
        this.player = player; // Initialize new field
    }

    // Parameterized Constructor
    public Request(EnumPlayerAction.Action action, Player player, String message) {
        this.action = action;
        this.player = player; // Initialize new field
        this.message = message;
    }
    // Parameterized Constructor

    public Request(EnumPlayerAction.Action action, Player player, String message, String move) {
        this.action = action;
        this.player = player; // Initialize new field
        this.message = message;
        this.move = move;

    }

    // Getter and Setter for action
    public EnumPlayerAction.Action getAction() {
        return action;
    }

    public void setAction(EnumPlayerAction.Action action) {
        this.action = action;
    }

    // Getter and Setter for massage
    public String getMessage() {
        return message;
    }

    public void setMessage(String massage) {
        this.message = massage;
    }

    // Getter and Setter for massage
    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    // Getter and Setter for player
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public String toString() {
        return "Request{"
                + ", action=" + action
                + ", massage=" + message
                + ", move=" + move
                + ", player=" + player
                + // Include new field in toString()
                '}';
    }
}
