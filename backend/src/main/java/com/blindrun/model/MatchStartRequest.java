package com.blindrun.model;

public class MatchStartRequest {
    private String sessionId;
    private String userId;
    private String role;

    public MatchStartRequest() {
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
