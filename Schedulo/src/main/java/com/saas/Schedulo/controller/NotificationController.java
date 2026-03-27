package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.notification.NotificationResponse;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.annotation.CurrentUser;
import com.saas.Schedulo.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing user notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications for the current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            @CurrentUser CustomUserDetails currentUser) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications for the current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @CurrentUser CustomUserDetails currentUser) {
        List<NotificationResponse> notifications = notificationService.getUnreadUserNotifications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @CurrentUser CustomUserDetails currentUser) {
        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID id,
            @CurrentUser CustomUserDetails currentUser) {
        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @CurrentUser CustomUserDetails currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }
}
