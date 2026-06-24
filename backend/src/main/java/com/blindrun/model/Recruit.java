package com.blindrun.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recruits")
public class Recruit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "start_time")
    private long startTime;
    
    @Column(name = "start_location")
    private String startLocation;
    
    @Column(name = "start_lat")
    private double startLat;
    
    @Column(name = "start_lng")
    private double startLng;
    
    @Column(name = "end_location")
    private String endLocation;
    
    @Column(name = "end_lat")
    private double endLat;
    
    @Column(name = "end_lng")
    private double endLng;
    
    private int distance;
    
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Recruit() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public double getStartLat() { return startLat; }
    public void setStartLat(double startLat) { this.startLat = startLat; }
    public double getStartLng() { return startLng; }
    public void setStartLng(double startLng) { this.startLng = startLng; }
    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    public double getEndLat() { return endLat; }
    public void setEndLat(double endLat) { this.endLat = endLat; }
    public double getEndLng() { return endLng; }
    public void setEndLng(double endLng) { this.endLng = endLng; }
    public int getDistance() { return distance; }
    public void setDistance(int distance) { this.distance = distance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}