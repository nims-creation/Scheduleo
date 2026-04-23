package com.saas.Schedulo.dto.request.organization;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBatchRequest {

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
}
