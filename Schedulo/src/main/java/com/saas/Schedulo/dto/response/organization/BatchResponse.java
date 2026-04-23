package com.saas.Schedulo.dto.response.organization;

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
public class BatchResponse {

    private UUID id;
    private String name;
    private String code;
    private Integer semester;
    private String academicYear;
    private Integer strength;
    private String description;

    // Department summary (avoid full nesting to prevent deep recursion)
    private UUID departmentId;
    private String departmentName;
    private String departmentCode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
