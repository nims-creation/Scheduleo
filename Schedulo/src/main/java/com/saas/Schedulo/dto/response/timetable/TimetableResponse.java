package com.saas.Schedulo.dto.response.timetable;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimetableResponse {
    private UUID id;
    private String name;
    private String description;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private String timetableType;
    private Boolean isTemplate;
    private String templateName;
    private DepartmentSummary department;
    private List<TimeSlotResponse> timeSlots;
    private List<ScheduleEntryResponse> scheduleEntries;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DepartmentSummary {
        private UUID id;
        private String name;
        private String code;
    }
}

