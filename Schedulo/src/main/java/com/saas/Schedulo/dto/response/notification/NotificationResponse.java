package com.saas.Schedulo.dto.response.notification;

import com.saas.Schedulo.entity.notification.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private Boolean isRead;
    private Notification.NotificationType type;
    private LocalDateTime createdAt;
}
