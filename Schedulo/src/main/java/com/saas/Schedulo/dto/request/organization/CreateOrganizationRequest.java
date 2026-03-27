package com.saas.Schedulo.dto.request.organization;

import com.saas.Schedulo.entity.organization.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    private String description;

    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;

    private String email;
    private String phone;
    private String website;
    
    private String timezone;
    private String workingDays;
    private String workingHoursStart;
    private String workingHoursEnd;
    private Integer slotDurationMinutes;

    private Map<String, Object> settings;
}
