package com.saas.Schedulo.dto.response.timetable;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotResponse {
    private UUID id;
    private String slotName;
    private LocalTime startTime;
    private LocalTime endTime;
    private DayOfWeek dayOfWeek;
    private String slotType;
    private Boolean breakAfter;
    private Integer breakDurationMinutes;
    private Integer durationMinutes;
    private Integer sortOrder;
}
