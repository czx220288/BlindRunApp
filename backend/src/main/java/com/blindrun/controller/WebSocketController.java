package com.blindrun.controller;

import com.blindrun.model.UserLocation;
import com.blindrun.service.LocationStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketController extends TextWebSocketHandler {

    private final LocationStorage locationStorage;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketController(LocationStorage locationStorage) {
        this.locationStorage = locationStorage;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        String userId = null;
        String sessionId = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    if ("userId".equals(pair[0])) userId = pair[1];
                    if ("sessionId".equals(pair[0])) sessionId = pair[1];
                }
            }
        }
        if (userId != null) {
            // 存储用户会话（用于发送 start_run）
            locationStorage.putUserSession(userId, session);
            if (sessionId != null) {
                locationStorage.put(sessionId + "_" + userId, session);
            }
            System.out.println("WebSocket connected: userId=" + userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        // 尝试解析为位置消息
        try {
            UserLocation location = objectMapper.readValue(payload, UserLocation.class);
            // 找到同房间的另一个用户并转发（位置同步）
            for (Map.Entry<String, WebSocketSession> entry : locationStorage.getAll().entrySet()) {
                if (entry.getValue().equals(session)) {
                    String key = entry.getKey();
                    String roomId = key.split("_")[0];
                    for (Map.Entry<String, WebSocketSession> other : locationStorage.getAll().entrySet()) {
                        if (other.getKey().startsWith(roomId + "_") && !other.getValue().equals(session)) {
                            other.getValue().sendMessage(new TextMessage(payload));
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            // 不是位置消息，忽略
        }
    }

    /**
     * 向盲人端发送开始跑步通知
     * @param blindUserId 盲人用户ID
     * @param recruitId 招募ID
     */
    public void notifyStartRun(String blindUserId, String recruitId) {
        WebSocketSession blindSession = locationStorage.getUserSession(blindUserId);
        if (blindSession != null && blindSession.isOpen()) {
            try {
                Map<String, String> message = new HashMap<>();
                message.put("type", "start_run");
                message.put("recruitId", recruitId);
                String json = objectMapper.writeValueAsString(message);
                blindSession.sendMessage(new TextMessage(json));
                System.out.println("Sent start_run to blind user: " + blindUserId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No WebSocket session found for blind user: " + blindUserId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从 sessions 中移除
        locationStorage.getAll().values().remove(session);
        // 从 userSessions 中移除
        for (Map.Entry<String, WebSocketSession> entry : locationStorage.getUserSessions().entrySet()) {
            if (entry.getValue().equals(session)) {
                locationStorage.removeUserSession(entry.getKey());
                break;
            }
        }
        System.out.println("WebSocket closed");
    }
}