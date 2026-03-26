package com.saas.Schedulo.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class LiveClockHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionTimezones = new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        sessionTimezones.put(session.getId(), "UTC");
        log.info("Clock WebSocket connected: {}", session.getId());
    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String timezone = message.getPayload();
        if (isValidTimezone(timezone)) {
            sessionTimezones.put(session.getId(), timezone);
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        sessionTimezones.remove(session.getId());
        log.info("Clock WebSocket disconnected: {}", session.getId());
    }

    @Scheduled(fixedRate = 1000)
    public void broadcastTime() {
        sessions.forEach((id, session) -> {
            if (session.isOpen()) {
                try {
                    String timezone = sessionTimezones.getOrDefault(id, "UTC");
                    LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
                    String formattedTime = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    session.sendMessage(new TextMessage(formattedTime));
                } catch (IOException e) {
                    log.error("Error sending clock update: {}", e.getMessage());
                }
            }
        });
    }
    private boolean isValidTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
