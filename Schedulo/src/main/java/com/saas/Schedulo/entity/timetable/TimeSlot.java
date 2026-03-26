package com.saas.Schedulo.entity.timetable;

import com.saas.Schedulo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots", indexes = {
        @Index(name = "idx_timeslot_timetable", columnList = "timetable_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @Column(name = "slot_name", length = 100)
    private String slotName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false)
    private SlotType slotType = SlotType.REGULAR;

    @Column(name = "break_after", nullable = false)
    private Boolean breakAfter = false;

    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public enum SlotType {
        REGULAR, BREAK, LUNCH, ASSEMBLY, SPECIAL
    }

    public int getDurationMinutes() {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }
}

