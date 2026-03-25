package com.saas.Schedulo.dto.response.timetable;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleEntryResponse {
    private UUID id;
    private String title;
    private String description;
    private DayOfWeek dayOfWeek;
    private LocalDate scheduleDate;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private TimeSlotResponse timeSlot;
    private ResourceSummary resource;
    private UserSummary assignedTo;
    private Set<UserSummary> participants;
    private String entryType;
    private String status;
    private String color;
    private Boolean isRecurring;
    private String notes;
    private LocalDateTime createdAt;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResourceSummary {
        private UUID id;
        private String name;
        private String code;
        private String resourceType;
        private String location;
        private Integer capacity;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSummary {
        private UUID id;
        private String fullName;
        private String email;
        private String profileImageUrl;
    }
}
