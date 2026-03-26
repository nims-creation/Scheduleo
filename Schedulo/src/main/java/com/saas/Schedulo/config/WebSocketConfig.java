package com.saas.Schedulo.config;

import com.saas.Schedulo.websocket.LiveClockHandler;
import com.saas.Schedulo.websocket.ScheduleUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final LiveClockHandler liveClockHandler;
    private final ScheduleUpdateHandler scheduleUpdateHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(liveClockHandler, "/ws/clock")
                .setAllowedOrigins("*");

        registry.addHandler(scheduleUpdateHandler, "/ws/schedule")
                .setAllowedOrigins("*");
    }
}
