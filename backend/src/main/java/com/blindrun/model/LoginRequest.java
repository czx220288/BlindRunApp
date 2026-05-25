package com.blindrun.model;

public class LoginRequest {
    private String userId;
    private String role;

    // 无参构造器必须存在
    public LoginRequest() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}