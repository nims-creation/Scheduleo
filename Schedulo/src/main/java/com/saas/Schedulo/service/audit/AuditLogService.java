package com.saas.Schedulo.service.audit;

import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.audit.AuditLogResponse;
import com.saas.Schedulo.entity.base.AuditLog;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AuditLogService {

    void log(UUID organizationId, UUID userId, String userName, String userEmail,
             AuditLog.AuditAction action, String entityType, UUID entityId,
             String entityName, String description);

    void log(UUID organizationId, UUID userId, String userName, String userEmail,
             AuditLog.AuditAction action, String entityType, UUID entityId,
             String entityName, String description, String oldValue, String newValue);

    PagedResponse<AuditLogResponse> getOrganizationAuditLogs(UUID organizationId, Pageable pageable);

    PagedResponse<AuditLogResponse> getUserAuditLogs(UUID userId, Pageable pageable);

    List<AuditLogResponse> getEntityAuditLogs(UUID organizationId, String entityType, UUID entityId);
}
