package com.saas.Schedulo.service.impl.calendar;

import com.saas.Schedulo.dto.request.calendar.CreateEventRequest;
import com.saas.Schedulo.dto.request.calendar.UpdateEventRequest;
import com.saas.Schedulo.dto.response.calendar.CalendarEventResponse;
import com.saas.Schedulo.dto.response.calendar.HolidayResponse;
import com.saas.Schedulo.entity.calendar.CalendarEvent;
import com.saas.Schedulo.entity.calendar.Holiday;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.user.User;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.calendar.CalendarEventRepository;
import com.saas.Schedulo.repository.calendar.HolidayRepository;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.repository.user.UserRepository;
import com.saas.Schedulo.service.calendar.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarServiceImpl implements CalendarService {

    private final CalendarEventRepository calendarEventRepository;
    private final HolidayRepository holidayRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CalendarEventResponse createEvent(CreateEventRequest request, UUID organizationId, UUID creatorId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId.toString()));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorId.toString()));

        CalendarEvent event = CalendarEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .allDay(request.getAllDay() != null ? request.getAllDay() : false)
                .location(request.getLocation())
                .virtualMeetingUrl(request.getVirtualMeetingUrl())
                .eventType(CalendarEvent.EventType.valueOf(request.getEventType()))
                .visibility(request.getVisibility() != null
                        ? CalendarEvent.EventVisibility.valueOf(request.getVisibility())
                        : CalendarEvent.EventVisibility.ORGANIZATION)
                .color(request.getColor())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .organization(organization)
                .creator(creator)
                .status(CalendarEvent.EventStatus.CONFIRMED)
                .build();

        // Link attendees
        if (request.getAttendeeIds() != null && !request.getAttendeeIds().isEmpty()) {
            for (UUID attendeeId : request.getAttendeeIds()) {
                userRepository.findById(attendeeId).ifPresent(u -> event.getAttendees().add(u));
            }
        }

        CalendarEvent saved = calendarEventRepository.save(event);
        log.info("Created calendar event '{}' in org {}", saved.getTitle(), organizationId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CalendarEventResponse updateEvent(UUID id, UpdateEventRequest request) {
        CalendarEvent event = calendarEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", id.toString()));

        if (request.getTitle() != null && !request.getTitle().isBlank()) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getStartDatetime() != null) event.setStartDatetime(request.getStartDatetime());
        if (request.getEndDatetime() != null) event.setEndDatetime(request.getEndDatetime());
        if (request.getAllDay() != null) event.setAllDay(request.getAllDay());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getVirtualMeetingUrl() != null) event.setVirtualMeetingUrl(request.getVirtualMeetingUrl());
        if (request.getColor() != null) event.setColor(request.getColor());
        if (request.getEventType() != null) event.setEventType(CalendarEvent.EventType.valueOf(request.getEventType()));
        if (request.getVisibility() != null) event.setVisibility(CalendarEvent.EventVisibility.valueOf(request.getVisibility()));
        if (request.getStatus() != null) event.setStatus(CalendarEvent.EventStatus.valueOf(request.getStatus()));

        if (request.getAttendeeIds() != null) {
            event.getAttendees().clear();
            for (UUID attendeeId : request.getAttendeeIds()) {
                userRepository.findById(attendeeId).ifPresent(u -> event.getAttendees().add(u));
            }
        }

        CalendarEvent updated = calendarEventRepository.save(event);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteEvent(UUID id) {
        CalendarEvent event = calendarEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", id.toString()));
        event.setIsDeleted(true);
        calendarEventRepository.save(event);
        log.info("Soft deleted calendar event '{}'", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarEventResponse getEventById(UUID id) {
        CalendarEvent event = calendarEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", id.toString()));
        return toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getEventsByDateRange(UUID organizationId, LocalDateTime start, LocalDateTime end) {
        return calendarEventRepository
                .findByOrganizationAndDateRange(organizationId, start, end)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getUserEvents(UUID userId, LocalDateTime start, LocalDateTime end) {
        return calendarEventRepository
                .findByCreatorAndDateRange(userId, start, end)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidays(UUID organizationId, LocalDate start, LocalDate end) {
        return holidayRepository
                .findByOrganizationAndDateRange(organizationId, start, end)
                .stream().map(this::toHolidayResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHoliday(UUID organizationId, LocalDate date) {
        return holidayRepository.isHoliday(organizationId, date);
    }

    @Override
    @Transactional
    public void addAttendee(UUID eventId, UUID userId) {
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", eventId.toString()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
        event.getAttendees().add(user);
        calendarEventRepository.save(event);
    }

    @Override
    @Transactional
    public void removeAttendee(UUID eventId, UUID userId) {
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", "id", eventId.toString()));
        event.getAttendees().removeIf(u -> u.getId().equals(userId));
        calendarEventRepository.save(event);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private CalendarEventResponse toResponse(CalendarEvent event) {
        List<CalendarEventResponse.AttendeeInfo> attendees = event.getAttendees() != null
                ? event.getAttendees().stream().map(u -> CalendarEventResponse.AttendeeInfo.builder()
                        .id(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .email(u.getEmail())
                        .build()).collect(Collectors.toList())
                : Collections.emptyList();

        return CalendarEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDatetime(event.getStartDatetime())
                .endDatetime(event.getEndDatetime())
                .allDay(event.getAllDay())
                .location(event.getLocation())
                .virtualMeetingUrl(event.getVirtualMeetingUrl())
                .color(event.getColor())
                .eventType(event.getEventType())
                .visibility(event.getVisibility())
                .status(event.getStatus())
                .isRecurring(event.getIsRecurring())
                .organizationId(event.getOrganization() != null ? event.getOrganization().getId() : null)
                .creatorId(event.getCreator() != null ? event.getCreator().getId() : null)
                .creatorName(event.getCreator() != null
                        ? event.getCreator().getFirstName() + " " + event.getCreator().getLastName() : null)
                .attendees(attendees)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    private HolidayResponse toHolidayResponse(Holiday holiday) {
        return HolidayResponse.builder()
                .id(holiday.getId())
                .name(holiday.getName())
                .description(holiday.getDescription())
                .holidayDate(holiday.getHolidayDate())
                .holidayType(holiday.getHolidayType())
                .isRecurring(holiday.getIsRecurring())
                .isHalfDay(holiday.getIsHalfDay())
                .applicableTo(holiday.getApplicableTo())
                .organizationId(holiday.getOrganization() != null ? holiday.getOrganization().getId() : null)
                .createdAt(holiday.getCreatedAt())
                .updatedAt(holiday.getUpdatedAt())
                .build();
    }
}
