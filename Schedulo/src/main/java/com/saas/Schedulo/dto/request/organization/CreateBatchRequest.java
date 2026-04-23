package com.saas.Schedulo.dto.request.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateBatchRequest {

    @NotBlank(message = "Batch name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 20, message = "Code cannot exceed 20 characters")
    private String code;

    @Positive(message = "Semester must be a positive number")
    private Integer semester;

    @Size(max = 10, message = "Academic year cannot exceed 10 characters")
    private String academicYear;

    @Positive(message = "Strength must be a positive number")
    private Integer strength;

    private String description;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;
}
