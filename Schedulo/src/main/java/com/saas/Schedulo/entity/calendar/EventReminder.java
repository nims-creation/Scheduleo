package com.saas.Schedulo.entity.calendar;

import com.saas.Schedulo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventReminder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEvent event;

    @Column(name = "minutes_before", nullable = false)
    private Integer minutesBefore;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false)
    private ReminderType reminderType;

    @Column(name = "is_sent", nullable = false)
    private Boolean isSent = false;

    public enum ReminderType {
        EMAIL, PUSH_NOTIFICATION, SMS, IN_APP
    }
}

