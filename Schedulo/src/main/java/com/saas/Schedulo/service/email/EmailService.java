package com.saas.Schedulo.service.email;

import com.saas.Schedulo.entity.timetable.Timetable;

public interface EmailService {
    void sendTimetablePublishedEmail(Timetable timetable);
}
