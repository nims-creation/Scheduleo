package com.saas.Schedulo.repository.notification;

import com.saas.Schedulo.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUserIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);
    long countByUserIdAndIsReadFalseAndIsDeletedFalse(UUID userId);
}
