package com.saas.Schedulo.dto.response.organization;

import com.saas.Schedulo.entity.organization.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private OrganizationType organizationType;
    private String logoUrl;
    private String website;
    private String email;
    private String phone;
    private String timezone;
    private String workingDays;
    private String workingHoursStart;
    private String workingHoursEnd;
    private Integer slotDurationMinutes;
    private Integer maxUsers;
    private Integer maxSchedulesPerDay;
    private Map<String, Object> settings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
