package com.blindrun.model;

public class MatchCancelRequest {
    private String sessionId;
    private String userId;

    public MatchCancelRequest() {
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
