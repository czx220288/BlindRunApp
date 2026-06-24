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
        User user = recruitService.login(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User request) {
        User user = recruitService.register(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
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
        List<Recruit> recruits = recruitService.getActiveRecruits();
        
        // 过滤掉已取消和已完成的订单
        List<Recruit> filteredRecruits = recruits.stream()
            .filter(r -> "active".equals(r.getStatus()))
            .collect(java.util.stream.Collectors.toList());
        
        System.out.println("附近招募数量: " + filteredRecruits.size());
        return ResponseEntity.ok(filteredRecruits);
    }

    @PostMapping("/recruit/accept")
    public ResponseEntity<MatchSession> acceptRecruit(@RequestBody AcceptRequest request) {
        System.out.println("=== 接单请求 ===");
        System.out.println("RecruitId: " + request.getRecruitId());
        System.out.println("CompanionId: " + request.getCompanionId());
        
        Recruit recruit = recruitService.getRecruitById(request.getRecruitId());
        if (recruit == null || !"active".equals(recruit.getStatus())) {
            System.out.println("错误：招募不存在或状态不对");
            return ResponseEntity.badRequest().build();
        }
        
        recruit.setStatus("accepted");
        recruitService.updateRecruit(recruit);

        MatchSession session = new MatchSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setBlindUserId(recruit.getUserId());
        session.setCompanionUserId(request.getCompanionId());
        session.setRecruitId(recruit.getId());
        session.setStartTime(recruit.getStartTime());
        session.setActive(false);
        session.setCompanionConfirmed(true);
        session.setStatus("pending");
        
        System.out.println("创建订单会话:");
        System.out.println("  SessionId: " + session.getSessionId());
        System.out.println("  BlindUserId: " + session.getBlindUserId());
        System.out.println("  CompanionUserId: " + session.getCompanionUserId());
        System.out.println("  Status: " + session.getStatus());
        System.out.println("  Active: " + session.isActive());
        
        recruitService.saveMatchSession(session);

        webSocketController.notifyMatchAccepted(recruit.getUserId(), session.getSessionId());

        System.out.println("=== 接单完成 ===");
        return ResponseEntity.ok(session);
    }

    @PostMapping("/match/start")
    public ResponseEntity<MatchSession> startMatch(@RequestBody MatchStartRequest request) {
        MatchSession session = recruitService.getMatchSession(request.getSessionId());
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        String userId = request.getUserId();
        String role = request.getRole();

        if ("blind".equals(role)) {
            session.setBlindConfirmed(true);
        } else if ("companion".equals(role)) {
            session.setCompanionConfirmed(true);
        }

        if (session.isBlindConfirmed() && session.isCompanionConfirmed()) {
            session.setActive(true);
            session.setStatus("active");
            
            webSocketController.notifyStartRun(session.getBlindUserId(), session.getRecruitId());
        }

        recruitService.updateMatchSession(session);
        
        // 通知对方用户状态已更新
        if ("blind".equals(role)) {
            // 盲人点击开始，通知陪跑者
            webSocketController.notifyMatchStatusUpdated(session.getCompanionUserId(), session.getSessionId());
            System.out.println("盲人已确认，通知陪跑者: " + session.getCompanionUserId());
        } else if ("companion".equals(role)) {
            // 陪跑者点击开始，通知盲人
            webSocketController.notifyMatchStatusUpdated(session.getBlindUserId(), session.getSessionId());
            System.out.println("陪跑者已确认，通知盲人: " + session.getBlindUserId());
        }
        
        return ResponseEntity.ok(session);
    }

    @PostMapping("/match/cancel")
    public ResponseEntity<Void> cancelMatch(@RequestBody MatchCancelRequest request) {
        MatchSession session = recruitService.getMatchSession(request.getSessionId());
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        // 检查当前状态，如果是终态则不允许修改
        if (com.blindrun.model.OrderStatus.isFinalState(session.getStatus())) {
            System.out.println("订单已是终态，无法取消 - Status: " + session.getStatus());
            return ResponseEntity.badRequest().build();
        }

        String oldStatus = session.getStatus();
        session.setStatus("cancelled");
        session.setActive(false);
        recruitService.updateMatchSession(session);

        Recruit recruit = recruitService.getRecruitById(session.getRecruitId());
        if (recruit != null) {
            recruit.setStatus("active");
            recruitService.updateRecruit(recruit);
        }

        // 通知双方订单已取消
        webSocketController.notifyMatchCancelled(session.getBlindUserId(), session.getSessionId());
        webSocketController.notifyMatchCancelled(session.getCompanionUserId(), session.getSessionId());

        System.out.println("订单已取消 - SessionId: " + session.getSessionId() + ", 原状态: " + oldStatus);
        System.out.println("已通知双方订单取消 - Blind: " + session.getBlindUserId() + ", Companion: " + session.getCompanionUserId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/match/finish")
    public ResponseEntity<MatchSession> finishMatch(@RequestBody MatchFinishRequest request) {
        System.out.println("=== 结束陪跑 ===");
        System.out.println("接收到的 sessionId (实际是recruitId): " + request.getSessionId());
        
        MatchSession session = recruitService.getMatchSessionByRecruitId(request.getSessionId());
        if (session == null) {
            System.out.println("未找到会话记录，recruitId: " + request.getSessionId());
            return ResponseEntity.notFound().build();
        }

        // 检查当前状态，如果是终态则不允许修改
        if (com.blindrun.model.OrderStatus.isFinalState(session.getStatus())) {
            System.out.println("订单已是终态，无法结束 - Status: " + session.getStatus());
            return ResponseEntity.badRequest().build();
        }

        System.out.println("找到会话 - SessionId: " + session.getSessionId() + ", Status: " + session.getStatus());
        
        String oldStatus = session.getStatus();
        session.setStatus("completed");
        session.setActive(false);
        recruitService.updateMatchSession(session);

        Recruit recruit = recruitService.getRecruitById(session.getRecruitId());
        if (recruit != null) {
            recruit.setStatus("completed");
            recruitService.updateRecruit(recruit);
            System.out.println("已更新招募状态为 completed");
        }

        // 通知双方订单已完成
        webSocketController.notifyMatchFinished(session.getBlindUserId(), session.getSessionId());
        webSocketController.notifyMatchFinished(session.getCompanionUserId(), session.getSessionId());

        System.out.println("订单已结束 - SessionId: " + session.getSessionId() + ", 原状态: " + oldStatus);
        System.out.println("已通知双方订单完成 - Blind: " + session.getBlindUserId() + ", Companion: " + session.getCompanionUserId());
        System.out.println("陪跑结束成功");
        return ResponseEntity.ok(session);
    }

    @GetMapping("/match/session/{recruitId}")
    public ResponseEntity<MatchSession> getMatchSessionByRecruitId(@PathVariable String recruitId) {
        MatchSession session = recruitService.getMatchSessionByRecruitId(recruitId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sos/trigger")
    public ResponseEntity<Void> triggerSos(@RequestBody SosRequest request) {
        System.out.println("=== SOS 紧急求助信号 ===");
        System.out.println("time: " + new java.util.Date());
        System.out.println("userID: " + request.getUserId());
        System.out.println("location: (" + request.getLatitude() + ", " + request.getLongitude() + ")");
        System.out.println("=========================");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recruit/{id}")
    public ResponseEntity<Recruit> getRecruitById(@PathVariable String id) {
        Recruit recruit = recruitService.getRecruitById(id);
        if (recruit == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(recruit);
    }

    @GetMapping("/recruit/history")
    public ResponseEntity<List<Recruit>> getRecruitHistory(@RequestParam String userId) {
        return ResponseEntity.ok(recruitService.getUserRecruits(userId));
    }

    @GetMapping("/match/history")
    public ResponseEntity<List<MatchSession>> getMatchHistory(
            @RequestParam String userId,
            @RequestParam String role) {
        System.out.println("=== 查询匹配历史 ===");
        System.out.println("UserId: " + userId);
        System.out.println("Role: " + role);
        
        List<MatchSession> sessions = recruitService.getBlindOrCompanionSessions(userId, role);
        
        System.out.println("查询到 " + sessions.size() + " 个订单");
        for (MatchSession session : sessions) {
            System.out.println("  - SessionId: " + session.getSessionId() + 
                             ", Status: " + session.getStatus() + 
                             ", Active: " + session.isActive());
        }
        
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/match/active")
    public ResponseEntity<List<MatchSession>> getActiveMatches(
            @RequestParam String userId,
            @RequestParam String role) {
        return ResponseEntity.ok(recruitService.getActiveSessions(userId, role));
    }
}