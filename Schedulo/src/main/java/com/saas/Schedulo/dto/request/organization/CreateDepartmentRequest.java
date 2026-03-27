package com.saas.Schedulo.dto.request.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String code;

    private String description;

    private UUID parentDepartmentId;

    private UUID headId;

    private String color;

    private Integer sortOrder;
}
