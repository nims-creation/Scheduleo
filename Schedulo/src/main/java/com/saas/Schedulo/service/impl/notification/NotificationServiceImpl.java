package com.saas.Schedulo.service.impl.notification;

import com.saas.Schedulo.dto.response.notification.NotificationResponse;
import com.saas.Schedulo.entity.notification.Notification;
import com.saas.Schedulo.entity.user.User;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.notification.NotificationRepository;
import com.saas.Schedulo.repository.user.UserRepository;
import com.saas.Schedulo.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createNotification(UUID userId, String title, String message, Notification.NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
                
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
                
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createNotificationForOrganization(UUID organizationId, String title, String message, Notification.NotificationType type) {
        List<User> orgUsers = userRepository.findByOrganizationIdAndIsDeletedFalse(organizationId);
        
        List<Notification> notifications = orgUsers.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title(title)
                        .message(message)
                        .type(type)
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());
                
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(UUID id, UUID userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id.toString()));
                
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to notification");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
