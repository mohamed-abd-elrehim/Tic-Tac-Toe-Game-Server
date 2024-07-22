/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DTOS;

import enumstatus.EnumStatus.Status;

/**
 *
 * @author Mohammed
 */


    public class Player {
        public int id;
        public String userName;
        public Status status;
        public String password;
        public int points;
        public String action;
        public String message; // For chat messages

        // Default constructor for Gson
        public Player() {
        }

        // Parameterized constructor
        public Player(int id, String userName, Status status, String password, int points, String action, String message) {
            this.id = id;
            this.userName = userName;
            this.status = status;
            this.password = password;
            this.points = points;
            this.action = action;
            this.message = message;
        }

        // Getter and Setter methods
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Player{" +
                    "id=" + id +
                    ", userName='" + userName + '\'' +
                    ", status='" + status + '\'' +
                    ", password='" + password + '\'' +
                    ", points=" + points +
                    ", action='" + action + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

