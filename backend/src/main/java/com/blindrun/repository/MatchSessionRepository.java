package com.blindrun.repository;

import com.blindrun.model.MatchSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchSessionRepository extends JpaRepository<MatchSession, String> {
    Optional<MatchSession> findBySessionId(String sessionId);
    
    List<MatchSession> findByBlindUserId(String blindUserId);
    
    List<MatchSession> findByCompanionUserId(String companionUserId);
    
    List<MatchSession> findByBlindUserIdAndActive(String blindUserId, boolean active);
    
    List<MatchSession> findByCompanionUserIdAndActive(String companionUserId, boolean active);
    
    Optional<MatchSession> findByRecruitIdAndActive(String recruitId, boolean active);
    
    List<MatchSession> findByRecruitId(String recruitId);
}
