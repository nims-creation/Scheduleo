package com.saas.Schedulo.dto.response.calendar;

import com.saas.Schedulo.entity.calendar.CalendarEvent;
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
public class CalendarEventResponse {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Boolean allDay;
    private String location;
    private String virtualMeetingUrl;
    private String color;

    private CalendarEvent.EventType eventType;
    private CalendarEvent.EventVisibility visibility;
    private CalendarEvent.EventStatus status;

    private UUID organizationId;
    private UUID creatorId;
    private String creatorName;

    private Boolean isRecurring;
    private List<AttendeeInfo> attendees;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendeeInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
    }
}
