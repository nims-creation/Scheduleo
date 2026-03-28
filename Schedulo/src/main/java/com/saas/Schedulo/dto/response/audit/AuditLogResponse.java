package com.saas.Schedulo.dto.response.audit;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private String entityName;
    private String description;
    private String ipAddress;
    private LocalDateTime timestamp;
}
