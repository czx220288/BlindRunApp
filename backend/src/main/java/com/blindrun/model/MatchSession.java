package com.blindrun.model;

public class MatchSession {
    private String sessionId;
    private String blindUserId;
    private String companionUserId;
    private boolean active;

    public MatchSession() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getBlindUserId() { return blindUserId; }
    public void setBlindUserId(String blindUserId) { this.blindUserId = blindUserId; }
    public String getCompanionUserId() { return companionUserId; }
    public void setCompanionUserId(String companionUserId) { this.companionUserId = companionUserId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}