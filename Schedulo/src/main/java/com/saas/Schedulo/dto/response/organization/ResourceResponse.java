package com.saas.Schedulo.dto.response.organization;

import com.saas.Schedulo.entity.organization.Resource;
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
public class ResourceResponse {
    private UUID id;
    private String name;
    private Resource.ResourceType type;
    private Integer capacity;
    private Boolean isAvailableForBooking;
    private UUID organizationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
