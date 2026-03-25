package com.saas.Schedulo.entity.timetable;

import com.timetable.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "recurring_patterns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPattern extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private RecurrenceFrequency frequency;

    @Column(name = "interval_value", nullable = false)
    private Integer intervalValue = 1;

    @ElementCollection
    @CollectionTable(name = "recurring_pattern_days",
            joinColumns = @JoinColumn(name = "pattern_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysOfWeek;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "week_of_month")
    private Integer weekOfMonth;

    @Column(name = "month_of_year")
    private Integer monthOfYear;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "occurrence_count")
    private Integer occurrenceCount;

    @Column(name = "exceptions", columnDefinition = "JSON")
    private String exceptions;

    public enum RecurrenceFrequency {
        DAILY, WEEKLY, BIWEEKLY, MONTHLY, YEARLY
    }
}

