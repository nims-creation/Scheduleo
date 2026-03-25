package com.saas.Schedulo.dto.request.timetable;

import com.timetable.entity.timetable.Timetable;
import com.timetable.validation.annotation.ValidTimeRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
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

    private Boolean isTemplate = false;
    private String templateName;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidTimeRange(startField = "startTime", endField = "endTime")
public class TimeSlotRequest {

    private String slotName;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private DayOfWeek dayOfWeek;
    private String slotType;
    private Boolean breakAfter = false;
    private Integer breakDurationMinutes;
    private Integer sortOrder;
}

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPatternRequest {

    @NotNull(message = "Frequency is required")
    private String frequency;

    @Min(1)
    private Integer intervalValue = 1;

    private Set<DayOfWeek> daysOfWeek;
    private Integer dayOfMonth;
    private Integer weekOfMonth;
    private Integer monthOfYear;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private Integer occurrenceCount;
}



