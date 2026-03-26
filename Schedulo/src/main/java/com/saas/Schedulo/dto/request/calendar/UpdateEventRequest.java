package com.saas.Schedulo.dto.request.calendar;

import jakarta.validation.constraints.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventRequest {
    @Size(max = 255)
    private String title;
    private String description;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Boolean allDay;
    private String location;
    private String virtualMeetingUrl;
    private String eventType;
    private String visibility;
    private String color;
    private Set<UUID> attendeeIds;
    private String status;
}
