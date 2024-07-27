package DTOS;

import enumstatus.EnumStatus.Status;

/**
 * PlayerRequest class to encapsulate player data and requests.
 * 
 * Author: Mohammed
 */
public class Player {

    public int id;
    public String userName;
    public Status status;
    public String password;
    public int points;
    public String action;
    public String message;

    /**
     * Default constructor for Gson and other serialization tools.
     */
    public Player() {
    }

    /**
     * Parameterized constructor to initialize PlayerRequest with given values.
     * 
     * @param id        Player's unique identifier.
     * @param userName  Player's username.
     * @param status    Player's status (e.g., online, offline).
     * @param password  Player's password.
     * @param points    Player's accumulated points.
     * @param action    Action requested by the player.
     * @param message   Additional message or information.
     */
    public Player(int id, String userName, Status status, String password, int points, String action, String message) {
        this.id = id;
        this.userName = userName;
        this.status = status;
        this.password = password;
        this.points = points;
        this.action = action;
        this.message = message;
    }

    // Getter and Setter methods for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter methods for userName
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Getter and Setter methods for status
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Getter and Setter methods for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getter and Setter methods for points
    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    // Getter and Setter methods for action
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    // Getter and Setter methods for message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PlayerRequest{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", status=" + status +
                ", password='" + password + '\'' +
                ", points=" + points +
                ", action='" + action + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
