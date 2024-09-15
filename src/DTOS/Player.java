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

  
    public Player() {
    }

    
    public Player(int id, String userName, Status status, String password, int points) {
        this.id = id;
        this.userName = userName;
        this.status = status;
        this.password = password;
        this.points = points;
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

    @Override
    public String toString() {
        return "PlayerRequest{"
                + "id=" + id
                + ", userName='" + userName + '\''
                + ", status=" + status
                + ", password='" + password + '\''
                + ", points=" + points
                + '}';
    }
}
