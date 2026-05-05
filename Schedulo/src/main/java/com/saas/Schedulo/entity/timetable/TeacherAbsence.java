package com.saas.Schedulo.entity.timetable;

import com.saas.Schedulo.entity.base.BaseEntity;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Tracks teacher absences for a specific date.
 * When a teacher is marked absent, their ScheduleEntries are either
 * CANCELLED or RESCHEDULED with a substitute — based on availability.
 */
@Entity
@Table(name = "teacher_absences", indexes = {
        @Index(name = "idx_absence_teacher", columnList = "teacher_id"),
        @Index(name = "idx_absence_date",    columnList = "absent_date"),
        @Index(name = "idx_absence_org",     columnList = "organization_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAbsence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /** The teacher who is absent */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    /** Date of absence */
    @Column(name = "absent_date", nullable = false)
    private LocalDate absentDate;

    /** FULL_DAY = all slots cancelled; PARTIAL = only a time window */
    @Enumerated(EnumType.STRING)
    @Column(name = "absence_type", nullable = false)
    @Builder.Default
    private AbsenceType absenceType = AbsenceType.FULL_DAY;

    /** Start time — only relevant when absenceType = PARTIAL */
    @Column(name = "partial_from")
    private LocalTime partialFrom;

    /** End time — only relevant when absenceType = PARTIAL */
    @Column(name = "partial_to")
    private LocalTime partialTo;

    /** Human-readable reason (e.g. "Medical leave", "Conference") */
    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * Substitute teacher assigned to cover the slots.
     * Null when resolution = CANCELLED.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_teacher_id")
    private User substituteTeacher;

    /** How the absence was resolved */
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution")
    private AbsenceResolution resolution;

    /** Number of schedule entries affected */
    @Column(name = "affected_entries_count")
    @Builder.Default
    private Integer affectedEntriesCount = 0;

    /** Admin/HOD notes */
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    public enum AbsenceType {
        /** Teacher absent for the full working day */
        FULL_DAY,
        /** Teacher absent only during a specific time window */
        PARTIAL
    }

    public enum AbsenceResolution {
        /** All affected slots are marked CANCELLED */
        CANCELLED,
        /** A substitute teacher has been assigned to cover the slots */
        SUBSTITUTED,
        /** Teacher resolves it themselves (e.g. make-up class later) */
        PENDING
    }
}
