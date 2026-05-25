package com.blindrun.service;

import com.blindrun.model.MatchSession;
import com.blindrun.model.Recruit;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RecruitService {
    private final Map<String, Recruit> recruitStore = new ConcurrentHashMap<>();
    private final Map<String, MatchSession> sessionStore = new ConcurrentHashMap<>();

    public Recruit saveRecruit(Recruit recruit) {
        recruitStore.put(recruit.getId(), recruit);
        return recruit;
    }
    public Recruit getRecruitById(String id) { return recruitStore.get(id); }
    public void updateRecruit(Recruit recruit) { recruitStore.put(recruit.getId(), recruit); }
    public List<Recruit> getActiveRecruits() {
        return recruitStore.values().stream()
                .filter(r -> "active".equals(r.getStatus()))
                .collect(Collectors.toList());
    }
    public void saveMatchSession(MatchSession session) { sessionStore.put(session.getSessionId(), session); }
    public MatchSession getMatchSession(String sessionId) { return sessionStore.get(sessionId); }
}