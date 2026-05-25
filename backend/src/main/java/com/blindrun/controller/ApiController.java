package com.blindrun.controller;

import com.blindrun.model.*;
import com.blindrun.service.RecruitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private RecruitService recruitService;

    @Autowired
    private WebSocketController webSocketController;

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        User user = new User();
        user.setUserId(request.getUserId());
        user.setRole(request.getRole());
        user.setName(request.getRole().equals("blind") ? "盲人用户" : "陪跑员");
        user.setToken("token_" + request.getUserId());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/recruit/publish")
    public ResponseEntity<Recruit> publishRecruit(@RequestBody Recruit recruit) {
        recruit.setId(UUID.randomUUID().toString());
        recruit.setStatus("active");
        return ResponseEntity.ok(recruitService.saveRecruit(recruit));
    }

    @GetMapping("/recruit/nearby")
    public ResponseEntity<List<Recruit>> getNearbyRecruits(
            @RequestParam double lat,
            @RequestParam double lng) {
        return ResponseEntity.ok(recruitService.getActiveRecruits());
    }

    @PostMapping("/recruit/accept")
    public ResponseEntity<MatchSession> acceptRecruit(@RequestBody AcceptRequest request) {
        Recruit recruit = recruitService.getRecruitById(request.getRecruitId());
        if (recruit == null || !"active".equals(recruit.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        recruit.setStatus("accepted");
        recruitService.updateRecruit(recruit);

        MatchSession session = new MatchSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setBlindUserId(recruit.getUserId());
        session.setCompanionUserId(request.getCompanionId());
        session.setActive(true);
        recruitService.saveMatchSession(session);

        // 关键：通知盲人端开始跑步
        webSocketController.notifyStartRun(recruit.getUserId(), recruit.getId());

        return ResponseEntity.ok(session);
    }

    @PostMapping("/sos/trigger")
    public ResponseEntity<Void> triggerSos(@RequestBody SosRequest request) {
        System.out.println("SOS from " + request.getUserId() +
                " at (" + request.getLatitude() + ", " + request.getLongitude() + ")");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recruit/{id}")
    public ResponseEntity<Recruit> getRecruitById(@PathVariable String id) {
        Recruit recruit = recruitService.getRecruitById(id);
        if (recruit == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(recruit);
    }
}