package com.turing.model;

/**
 * Represents a registered player or administrator in the Turing Machine game.
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String role; // "USER" or "ADMIN"
    private int tokens;
    private boolean blocked;

    public User() {}

    public User(int id, String username, String password, String role, int tokens, boolean blocked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.tokens = tokens;
        this.blocked = blocked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public String toString() {
        return username + " (" + role + ") - Tokens: " + tokens + (blocked ? " [BLOCKED]" : "");
    }
}
