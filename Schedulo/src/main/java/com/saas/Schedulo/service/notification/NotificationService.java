package com.saas.Schedulo.service.notification;

import com.saas.Schedulo.dto.response.notification.NotificationResponse;
import com.saas.Schedulo.entity.notification.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void createNotification(UUID userId, String title, String message, Notification.NotificationType type);
    void createNotificationForOrganization(UUID organizationId, String title, String message, Notification.NotificationType type);
    List<NotificationResponse> getUserNotifications(UUID userId);
    List<NotificationResponse> getUnreadUserNotifications(UUID userId);
    long getUnreadCount(UUID userId);
    void markAsRead(UUID id, UUID userId);
    void markAllAsRead(UUID userId);
}
