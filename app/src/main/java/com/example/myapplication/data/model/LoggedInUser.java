package com.example.myapplication.data.model;

import java.io.Serializable;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser implements Serializable {

    private double id;
    private String username;
    private String email;
    private boolean is_administrator;
    private String password;

    public LoggedInUser(double id, String username, String email, boolean is_administrator, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.is_administrator = is_administrator;
        this.password = password;
    }

    public LoggedInUser() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isIs_administrator() {
        return is_administrator;
    }

    public void setIs_administrator(boolean is_administrator) {
        this.is_administrator = is_administrator;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}