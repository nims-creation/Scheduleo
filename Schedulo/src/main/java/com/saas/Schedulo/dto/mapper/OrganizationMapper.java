package com.saas.Schedulo.dto.mapper;

import com.saas.Schedulo.dto.response.organization.OrganizationResponse;
import com.saas.Schedulo.entity.organization.Organization;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class OrganizationMapper {

    public OrganizationResponse toOrganizationResponse(Organization entity) {
        if (entity == null) {
            return null;
        }

        return OrganizationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .organizationType(entity.getOrganizationType())
                .logoUrl(entity.getLogoUrl())
                .website(entity.getWebsite())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .timezone(entity.getTimezone())
                .workingDays(entity.getWorkingDays())
                .workingHoursStart(entity.getWorkingHoursStart())
                .workingHoursEnd(entity.getWorkingHoursEnd())
                .slotDurationMinutes(entity.getSlotDurationMinutes())
                .maxUsers(entity.getMaxUsers())
                .maxSchedulesPerDay(entity.getMaxSchedulesPerDay())
                .settings(entity.getSettings() != null ? new HashMap<>(entity.getSettings()) : new HashMap<>())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
