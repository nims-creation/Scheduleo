package com.saas.Schedulo.entity.calendar;

import com.timetable.entity.base.BaseEntity;
import com.timetable.entity.organization.Organization;
import com.timetable.entity.timetable.RecurringPattern;
import com.timetable.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "calendar_events", indexes = {
        @Index(name = "idx_event_org", columnList = "organization_id"),
        @Index(name = "idx_event_creator", columnList = "creator_id"),
        @Index(name = "idx_event_dates", columnList = "start_datetime, end_datetime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @Column(name = "all_day", nullable = false)
    private Boolean allDay = false;

    @Column(name = "location")
    private String location;

    @Column(name = "virtual_meeting_url")
    private String virtualMeetingUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private EventVisibility visibility = EventVisibility.ORGANIZATION;

    @Column(name = "color", length = 7)
    private String color;

    @ManyToMany
    @JoinTable(
            name = "calendar_event_attendees",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> attendees = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventReminder> reminders = new ArrayList<>();

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_pattern_id")
    private RecurringPattern recurringPattern;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status = EventStatus.CONFIRMED;

    public enum EventType {
        MEETING, HOLIDAY, EXAMINATION, DEADLINE,
        ANNOUNCEMENT, REMINDER, PERSONAL, OTHER
    }

    public enum EventVisibility {
        PRIVATE, ORGANIZATION, PUBLIC
    }

    public enum EventStatus {
        TENTATIVE, CONFIRMED, CANCELLED
    }
}

