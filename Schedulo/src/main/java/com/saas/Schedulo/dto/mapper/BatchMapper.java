package com.saas.Schedulo.dto.mapper;

import com.saas.Schedulo.dto.response.organization.BatchResponse;
import com.saas.Schedulo.entity.organization.Batch;
import org.springframework.stereotype.Component;

@Component
public class BatchMapper {

    public BatchResponse toResponse(Batch batch) {
        if (batch == null) {
            return null;
        }

        return BatchResponse.builder()
                .id(batch.getId())
                .name(batch.getName())
                .code(batch.getCode())
                .semester(batch.getSemester())
                .academicYear(batch.getAcademicYear())
                .strength(batch.getStrength())
                .description(batch.getDescription())
                .departmentId(batch.getDepartment() != null ? batch.getDepartment().getId() : null)
                .departmentName(batch.getDepartment() != null ? batch.getDepartment().getName() : null)
                .departmentCode(batch.getDepartment() != null ? batch.getDepartment().getCode() : null)
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }
}
