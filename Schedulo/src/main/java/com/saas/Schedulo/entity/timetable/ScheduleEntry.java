package com.saas.Schedulo.entity.timetable;

import com.saas.Schedulo.entity.base.BaseEntity;
import com.saas.Schedulo.entity.organization.Batch;
import com.saas.Schedulo.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "schedule_entries", indexes = {
        @Index(name = "idx_schedule_timetable", columnList = "timetable_id"),
        @Index(name = "idx_schedule_date", columnList = "schedule_date"),
        @Index(name = "idx_schedule_resource", columnList = "resource_id"),
        @Index(name = "idx_schedule_assigned", columnList = "assigned_to_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "schedule_date")
    private LocalDate scheduleDate;

    @Column(name = "start_datetime")
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime")
    private LocalDateTime endDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToMany
    @JoinTable(
            name = "schedule_entry_participants",
            joinColumns = @JoinColumn(name = "schedule_entry_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntryStatus status = EntryStatus.SCHEDULED;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_pattern_id")
    private RecurringPattern recurringPattern;

    /**
     * Optional batch override — used for split-batch entries
     * (e.g. Lab session for CSE-A only while CSE-B has a lecture).
     * If null, inherits the batch from the parent Timetable.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    public enum EntryType {
        CLASS, LECTURE, LAB, MEETING, APPOINTMENT, SHIFT,
        CONSULTATION, EXAMINATION, EVENT, OTHER
    }

    public enum EntryStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, RESCHEDULED
    }
}

