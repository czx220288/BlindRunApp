package com.blindrun.model;

public class AcceptRequest {
    private String recruitId;
    private String companionId;

    public AcceptRequest() {}

    public String getRecruitId() { return recruitId; }
    public void setRecruitId(String recruitId) { this.recruitId = recruitId; }
    public String getCompanionId() { return companionId; }
    public void setCompanionId(String companionId) { this.companionId = companionId; }
}