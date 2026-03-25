package com.saas.Schedulo.dto.request.timetable;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateScheduleEntryRequest {

    @NotNull(message = "Timetable ID is required")
    private UUID timetableId;

    private UUID timeSlotId;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    private String description;

    private DayOfWeek dayOfWeek;
    private LocalDate scheduleDate;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;

    private UUID resourceId;
    private UUID assignedToId;
    private Set<UUID> participantIds;

    @NotNull(message = "Entry type is required")
    private String entryType;

    private String color;
    private Boolean isRecurring = false;

    @Valid
    private RecurringPatternRequest recurringPattern;

    private String notes;
    private String metadata;
}

