package com.saas.Schedulo.service.calendar;

import com.saas.Schedulo.dto.request.calendar.CreateEventRequest;
import com.saas.Schedulo.dto.request.calendar.UpdateEventRequest;
import com.saas.Schedulo.dto.response.calendar.CalendarEventResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CalendarService {
    CalendarEventResponse createEvent(CreateEventRequest request, UUID organizationId, UUID creatorId);
    CalendarEventResponse updateEvent(UUID id, UpdateEventRequest request);
    void deleteEvent(UUID id);
    CalendarEventResponse getEventById(UUID id);
    List<CalendarEventResponse> getEventsByDateRange(UUID organizationId, LocalDateTime start, LocalDateTime end);
    List<CalendarEventResponse> getUserEvents(UUID userId, LocalDateTime start, LocalDateTime end);
    List<HolidayResponse> getHolidays(UUID organizationId, LocalDate start, LocalDate end);
    boolean isHoliday(UUID organizationId, LocalDate date);
    void addAttendee(UUID eventId, UUID userId);
    void removeAttendee(UUID eventId, UUID userId);
}
