package com.saas.Schedulo.entity.timetable;

import com.timetable.entity.base.BaseEntity;
import com.timetable.entity.organization.Department;
import com.timetable.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timetables", indexes = {
        @Index(name = "idx_timetable_org", columnList = "organization_id"),
        @Index(name = "idx_timetable_dates", columnList = "effective_from, effective_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timetable extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TimetableStatus status = TimetableStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "timetable_type", nullable = false)
    private TimetableType timetableType;

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimeSlot> timeSlots = new ArrayList<>();

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScheduleEntry> scheduleEntries = new ArrayList<>();

    @Column(name = "is_template", nullable = false)
    private Boolean isTemplate = false;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "recurrence_pattern")
    private String recurrencePattern;

    public enum TimetableStatus {
        DRAFT, PENDING_APPROVAL, APPROVED, PUBLISHED, ARCHIVED
    }

    public enum TimetableType {
        WEEKLY, DAILY, CUSTOM, ROTATING
    }
}

