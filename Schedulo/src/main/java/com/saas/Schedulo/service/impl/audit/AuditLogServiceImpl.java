package com.saas.Schedulo.service.impl.audit;

import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.audit.AuditLogResponse;
import com.saas.Schedulo.entity.base.AuditLog;
import com.saas.Schedulo.repository.audit.AuditLogRepository;
import com.saas.Schedulo.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    public void log(UUID organizationId, UUID userId, String userName, String userEmail,
                    AuditLog.AuditAction action, String entityType, UUID entityId,
                    String entityName, String description) {
        log(organizationId, userId, userName, userEmail, action, entityType,
                entityId, entityName, description, null, null);
    }

    @Override
    @Async
    public void log(UUID organizationId, UUID userId, String userName, String userEmail,
                    AuditLog.AuditAction action, String entityType, UUID entityId,
                    String entityName, String description, String oldValue, String newValue) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .organizationId(organizationId)
                    .userId(userId)
                    .userName(userName)
                    .userEmail(userEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .description(description)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} {} {} by {}", action, entityType, entityId, userName);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    @Override
    public PagedResponse<AuditLogResponse> getOrganizationAuditLogs(UUID organizationId, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByOrganizationIdOrderByTimestampDesc(organizationId, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    public PagedResponse<AuditLogResponse> getUserAuditLogs(UUID userId, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    public List<AuditLogResponse> getEntityAuditLogs(UUID organizationId, String entityType, UUID entityId) {
        return auditLogRepository.findByEntity(organizationId, entityType, entityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PagedResponse<AuditLogResponse> mapToPagedResponse(Page<AuditLog> page) {
        List<AuditLogResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<AuditLogResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private AuditLogResponse mapToResponse(AuditLog a) {
        return AuditLogResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .userName(a.getUserName())
                .userEmail(a.getUserEmail())
                .action(a.getAction().name())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .entityName(a.getEntityName())
                .description(a.getDescription())
                .ipAddress(a.getIpAddress())
                .timestamp(a.getTimestamp())
                .build();
    }
}
