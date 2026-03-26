package com.saas.Schedulo.dto.request.timetable;

import com.saas.Schedulo.validation.annotation.ValidTimeRange;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

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
