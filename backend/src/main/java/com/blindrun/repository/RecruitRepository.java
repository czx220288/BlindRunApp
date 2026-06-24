package com.blindrun.repository;

import com.blindrun.model.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecruitRepository extends JpaRepository<Recruit, String> {
    List<Recruit> findByStatus(String status);
    
    List<Recruit> findByUserId(String userId);
    
    List<Recruit> findByUserIdAndStatus(String userId, String status);
}
