package com.saas.Schedulo.dto.request.timetable;

import jakarta.validation.constraints.*;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPatternRequest {

    @NotNull(message = "Frequency is required")
    private String frequency;

    @Min(1)
    @Builder.Default
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
