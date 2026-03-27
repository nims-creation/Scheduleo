package com.saas.Schedulo.dto.request.timetable;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateTimetableRequest {

    @NotNull(message = "Timetable ID is required")
    private UUID timetableId;

    @NotEmpty(message = "At least one class requirement must be provided")
    private List<ClassRequirementRequest> classRequirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassRequirementRequest {
        @NotNull(message = "Title is required")
        private String title;

        private UUID teacherId;
        private UUID resourceId;

        @NotNull(message = "Periods per week must be specified")
        private Integer periodsPerWeek;

        @NotNull(message = "Duration in minutes must be specified")
        private Integer durationMinutes;

        private String entryType; // e.g., CLASS, LECTURE, LAB
    }
}
