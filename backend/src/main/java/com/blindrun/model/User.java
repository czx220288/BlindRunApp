package com.blindrun.model;

public class User {
    private String userId;
    private String name;
    private String role;
    private String token;

    public User() {}

    public User(String userId, String name, String role, String token) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.token = token;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}