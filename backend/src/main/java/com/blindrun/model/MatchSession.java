package com.blindrun.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_sessions")
public class MatchSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String sessionId;
    
    @Column(name = "blind_user_id")
    private String blindUserId;
    
    @Column(name = "companion_user_id")
    private String companionUserId;
    
    @Column(name = "recruit_id")
    private String recruitId;
    
    private boolean active;
    
    @Column(name = "blind_confirmed")
    private boolean blindConfirmed;
    
    @Column(name = "companion_confirmed")
    private boolean companionConfirmed;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "start_time")
    private Long startTime;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public MatchSession() {
        this.createdAt = LocalDateTime.now();
        this.active = false;
        this.blindConfirmed = false;
        this.companionConfirmed = false;
        this.status = "pending";
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getBlindUserId() { return blindUserId; }
    public void setBlindUserId(String blindUserId) { this.blindUserId = blindUserId; }
    public String getCompanionUserId() { return companionUserId; }
    public void setCompanionUserId(String companionUserId) { this.companionUserId = companionUserId; }
    public String getRecruitId() { return recruitId; }
    public void setRecruitId(String recruitId) { this.recruitId = recruitId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isBlindConfirmed() { return blindConfirmed; }
    public void setBlindConfirmed(boolean blindConfirmed) { this.blindConfirmed = blindConfirmed; }
    public boolean isCompanionConfirmed() { return companionConfirmed; }
    public void setCompanionConfirmed(boolean companionConfirmed) { this.companionConfirmed = companionConfirmed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getStartTime() { return startTime; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}