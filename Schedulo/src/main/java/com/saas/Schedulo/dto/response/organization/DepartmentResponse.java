package com.saas.Schedulo.dto.response.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private UUID id;
    private String name;
    private String code;
    private String description;
    private String color;
    private Integer sortOrder;

    private UUID organizationId;
    private UUID parentDepartmentId;
    private String parentDepartmentName;

    private UUID headId;
    private String headName;

    private List<DepartmentResponse> subDepartments;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
