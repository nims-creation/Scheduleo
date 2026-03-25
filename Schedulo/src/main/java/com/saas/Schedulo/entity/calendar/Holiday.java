package com.saas.Schedulo.entity.calendar;

import com.timetable.entity.base.BaseEntity;
import com.timetable.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "holidays", indexes = {
        @Index(name = "idx_holiday_org", columnList = "organization_id"),
        @Index(name = "idx_holiday_date", columnList = "holiday_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type", nullable = false)
    private HolidayType holidayType;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    @Column(name = "is_half_day", nullable = false)
    private Boolean isHalfDay = false;

    @Column(name = "applicable_to")
    private String applicableTo;

    public enum HolidayType {
        PUBLIC, ORGANIZATIONAL, RELIGIOUS, OPTIONAL
    }
}

