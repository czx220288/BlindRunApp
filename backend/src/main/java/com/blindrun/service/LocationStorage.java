package com.blindrun.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocationStorage {
    // 用于位置转发（roomId_userId -> session）
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // 用于发送通知（userId -> session）
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public void put(String key, WebSocketSession session) {
        sessions.put(key, session);
    }
    public WebSocketSession get(String key) {
        return sessions.get(key);
    }
    public void remove(String key) {
        sessions.remove(key);
    }
    public Map<String, WebSocketSession> getAll() {
        return sessions;
    }

    // 用户会话管理
    public void putUserSession(String userId, WebSocketSession session) {
        userSessions.put(userId, session);
    }
    public WebSocketSession getUserSession(String userId) {
        return userSessions.get(userId);
    }
    public void removeUserSession(String userId) {
        userSessions.remove(userId);
    }
    public Map<String, WebSocketSession> getUserSessions() {
        return userSessions;
    }
}