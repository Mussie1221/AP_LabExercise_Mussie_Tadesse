package com.chatapp.model;

/**
 * Model class representing a user account.
 * Implemented using pure standard Java to ensure compatibility and compilation 
 * in all environments without requiring external IDE plugins.
 */
public class User {
    
    private String username;
    private String password;

    /**
     * Default constructor required for serialization/deserialization.
     */
    public User() {}

    /**
     * All-arguments constructor to initialize credentials.
     *
     * @param username the username
     * @param password the password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
