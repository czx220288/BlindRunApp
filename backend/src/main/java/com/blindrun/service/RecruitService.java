package com.blindrun.service;

import com.blindrun.model.LoginRequest;
import com.blindrun.model.MatchSession;
import com.blindrun.model.Recruit;
import com.blindrun.model.User;
import com.blindrun.repository.MatchSessionRepository;
import com.blindrun.repository.RecruitRepository;
import com.blindrun.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RecruitService {
    private final RecruitRepository recruitRepository;
    private final MatchSessionRepository matchSessionRepository;
    private final UserRepository userRepository;

    public RecruitService(RecruitRepository recruitRepository, 
                         MatchSessionRepository matchSessionRepository,
                         UserRepository userRepository) {
        this.recruitRepository = recruitRepository;
        this.matchSessionRepository = matchSessionRepository;
        this.userRepository = userRepository;
    }

    public User login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUserId(request.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(request.getPassword()) && 
                user.getRole().equals(request.getRole())) {
                return user;
            }
        }
        return null;
    }

    public User register(User request) {
        Optional<User> existingUser = userRepository.findByUserId(request.getUserId());
        if (existingUser.isPresent()) {
            return null;
        }
        
        User newUser = new User();
        newUser.setUserId(request.getUserId());
        newUser.setName(request.getName());
        newUser.setRole(request.getRole());
        newUser.setPassword(request.getPassword());
        newUser.setToken("token_" + request.getUserId() + "_" + System.currentTimeMillis());
        
        return userRepository.save(newUser);
    }

    public Recruit saveRecruit(Recruit recruit) {
        return recruitRepository.save(recruit);
    }
    
    public Recruit getRecruitById(String id) { 
        return recruitRepository.findById(id).orElse(null); 
    }
    
    public void updateRecruit(Recruit recruit) { 
        recruitRepository.save(recruit); 
    }
    
    public List<Recruit> getActiveRecruits() {
        return recruitRepository.findByStatus("active");
    }
    
    public List<Recruit> getUserRecruits(String userId) {
        return recruitRepository.findByUserId(userId);
    }
    
    public List<Recruit> getUserActiveRecruits(String userId) {
        return recruitRepository.findByUserIdAndStatus(userId, "active");
    }
    
    public List<MatchSession> getBlindUserSessions(String blindUserId) {
        return matchSessionRepository.findByBlindUserId(blindUserId);
    }
    
    public List<MatchSession> getCompanionUserSessions(String companionUserId) {
        return matchSessionRepository.findByCompanionUserId(companionUserId);
    }
    
    public List<MatchSession> getBlindOrCompanionSessions(String userId, String role) {
        if ("blind".equals(role)) {
            return matchSessionRepository.findByBlindUserId(userId);
        } else {
            return matchSessionRepository.findByCompanionUserId(userId);
        }
    }

    public List<MatchSession> getActiveSessions(String userId, String role) {
        if ("blind".equals(role)) {
            return matchSessionRepository.findByBlindUserIdAndActive(userId, true);
        } else {
            return matchSessionRepository.findByCompanionUserIdAndActive(userId, true);
        }
    }
    
    public void saveMatchSession(MatchSession session) { 
        matchSessionRepository.save(session); 
    }
    
    public void updateMatchSession(MatchSession session) {
        matchSessionRepository.save(session);
    }
    
    public MatchSession getMatchSession(String sessionId) { 
        return matchSessionRepository.findBySessionId(sessionId).orElse(null); 
    }
    
    public MatchSession getMatchSessionByRecruitId(String recruitId) {
        List<MatchSession> sessions = matchSessionRepository.findByRecruitId(recruitId);
        if (sessions.isEmpty()) {
            return null;
        }
        // 返回最新的一条记录（按创建时间排序）
        return sessions.stream()
            .max((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
            .orElse(sessions.get(0));
    }
}