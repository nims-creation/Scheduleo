package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.audit.AuditLogResponse;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.annotation.CurrentUser;
import com.saas.Schedulo.service.audit.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Activity and audit trail endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get organization audit logs", description = "Get paginated activity log for the organization")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getOrganizationLogs(
            @CurrentUser CustomUserDetails currentUser,
            @PageableDefault(size = 30) Pageable pageable) {
        PagedResponse<AuditLogResponse> response = auditLogService.getOrganizationAuditLogs(
                currentUser.getOrganizationId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user audit logs", description = "Get activity log for a specific user")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getUserLogs(
            @PathVariable UUID userId,
            @PageableDefault(size = 30) Pageable pageable) {
        PagedResponse<AuditLogResponse> response = auditLogService.getUserAuditLogs(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get entity audit logs", description = "Get change history for a specific entity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getEntityLogs(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable String entityType,
            @PathVariable UUID entityId) {
        List<AuditLogResponse> response = auditLogService.getEntityAuditLogs(
                currentUser.getOrganizationId(), entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
