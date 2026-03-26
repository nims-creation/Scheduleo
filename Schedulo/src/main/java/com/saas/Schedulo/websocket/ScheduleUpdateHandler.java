package com.saas.Schedulo.websocket;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleUpdateHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;

    // Map of organizationId -> sessions
    private final Map<String, Set<WebSocketSession>> organizationSessions = new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Schedule WebSocket connected: {}", session.getId());
    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // Expect message format: {"action": "subscribe", "organizationId": "..."}
            Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);

            if ("subscribe".equals(payload.get("action"))) {
                String orgId = payload.get("organizationId");
                organizationSessions.computeIfAbsent(orgId, k -> ConcurrentHashMap.newKeySet())
                        .add(session);
                session.getAttributes().put("organizationId", orgId);
            }
        } catch (Exception e) {
            log.error("Error handling schedule message: {}", e.getMessage());
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String orgId = (String) session.getAttributes().get("organizationId");
        if (orgId != null) {
            Set<WebSocketSession> sessions = organizationSessions.get(orgId);
            if (sessions != null) {
                sessions.remove(session);
            }
        }
        log.info("Schedule WebSocket disconnected: {}", session.getId());
    }
    public void broadcastScheduleUpdate(String organizationId, String action, ScheduleEntryResponse entry) {
        Set<WebSocketSession> sessions = organizationSessions.get(organizationId);
        if (sessions == null || sessions.isEmpty()) return;
        try {
            Map<String, Object> message = Map.of(
                    "action", action,
                    "data", entry,
                    "timestamp", System.currentTimeMillis()
            );
            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);
            sessions.forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Error broadcasting schedule update: {}", e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error creating schedule update message: {}", e.getMessage());
        }
    }
}
