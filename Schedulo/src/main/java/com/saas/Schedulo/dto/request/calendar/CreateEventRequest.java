package com.saas.Schedulo.dto.request.calendar;

import jakarta.validation.constraints.*;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;
    private String description;
    @NotNull(message = "Start datetime is required")
    private LocalDateTime startDatetime;
    @NotNull(message = "End datetime is required")
    private LocalDateTime endDatetime;
    @Builder.Default
    private Boolean allDay = false;
    private String location;
    private String virtualMeetingUrl;
    @NotNull(message = "Event type is required")
    private String eventType;
    @Builder.Default
    private String visibility = "ORGANIZATION";
    private String color;
    private Set<UUID> attendeeIds;
    @Builder.Default
    private Boolean isRecurring = false;
    private com.saas.Schedulo.dto.request.timetable.RecurringPatternRequest recurringPattern;
    @AssertTrue(message = "End datetime must be after start datetime")
    private boolean isValidDateRange() {
        if (startDatetime == null || endDatetime == null) return true;
        return endDatetime.isAfter(startDatetime);
    }
}
