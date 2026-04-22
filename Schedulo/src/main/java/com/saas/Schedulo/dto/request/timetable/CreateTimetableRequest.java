package com.saas.Schedulo.dto.request.timetable;

import com.saas.Schedulo.entity.timetable.Timetable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTimetableRequest {

    @NotBlank(message = "Timetable name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Effective from date is required")
    @FutureOrPresent(message = "Effective from date must be today or in the future")
    private LocalDate effectiveFrom;

    @Future(message = "Effective to date must be in the future")
    private LocalDate effectiveTo;

    @NotNull(message = "Timetable type is required")
    private Timetable.TimetableType timetableType;

    private UUID departmentId;

    @Valid
    private List<TimeSlotRequest> timeSlots;

    @Builder.Default
    private Boolean isTemplate = false;
    private String templateName;
}








