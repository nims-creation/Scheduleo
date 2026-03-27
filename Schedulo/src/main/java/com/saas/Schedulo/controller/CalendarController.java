package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.calendar.CreateEventRequest;
import com.saas.Schedulo.dto.request.calendar.CreateHolidayRequest;
import com.saas.Schedulo.dto.request.calendar.UpdateEventRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.calendar.CalendarEventResponse;
import com.saas.Schedulo.dto.response.calendar.HolidayResponse;
import com.saas.Schedulo.entity.calendar.Holiday;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.calendar.HolidayRepository;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.annotation.CurrentUser;
import com.saas.Schedulo.service.calendar.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "Calendar events and holiday management")
@SecurityRequirement(name = "bearerAuth")
public class CalendarController {

    private final CalendarService calendarService;
    private final HolidayRepository holidayRepository;
    private final OrganizationRepository organizationRepository;

    // ── Events ─────────────────────────────────────────────────────────────

    @GetMapping("/events")
    @Operation(summary = "List events by date range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CalendarEventResponse>>> getEvents(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<CalendarEventResponse> events = calendarService.getEventsByDateRange(
                currentUser.getOrganizationId(), start, end);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "Get event by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> getEvent(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(calendarService.getEventById(id)));
    }

    @PostMapping("/events")
    @Operation(summary = "Create calendar event")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> createEvent(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody CreateEventRequest request) {
        CalendarEventResponse response = calendarService.createEvent(
                request, currentUser.getOrganizationId(), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Event created successfully"));
    }

    @PutMapping("/events/{id}")
    @Operation(summary = "Update calendar event")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> updateEvent(
            @PathVariable UUID id,
            @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.updateEvent(id, request), "Event updated successfully"));
    }

    @DeleteMapping("/events/{id}")
    @Operation(summary = "Delete calendar event")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable UUID id) {
        calendarService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Event deleted successfully"));
    }

    @PostMapping("/events/{id}/attendees/{userId}")
    @Operation(summary = "Add attendee to event")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> addAttendee(
            @PathVariable UUID id, @PathVariable UUID userId) {
        calendarService.addAttendee(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendee added"));
    }

    @DeleteMapping("/events/{id}/attendees/{userId}")
    @Operation(summary = "Remove attendee from event")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeAttendee(
            @PathVariable UUID id, @PathVariable UUID userId) {
        calendarService.removeAttendee(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendee removed"));
    }

    // ── Holidays ───────────────────────────────────────────────────────────

    @GetMapping("/holidays")
    @Operation(summary = "List holidays in date range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getHolidays(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<HolidayResponse> holidays = calendarService.getHolidays(
                currentUser.getOrganizationId(), start, end);
        return ResponseEntity.ok(ApiResponse.success(holidays));
    }

    @PostMapping("/holidays")
    @Operation(summary = "Create holiday")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<HolidayResponse>> createHoliday(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody CreateHolidayRequest request) {
        Organization organization = organizationRepository.findById(currentUser.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id",
                        currentUser.getOrganizationId().toString()));

        Holiday holiday = Holiday.builder()
                .name(request.getName())
                .description(request.getDescription())
                .holidayDate(request.getHolidayDate())
                .holidayType(request.getHolidayType())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .isHalfDay(request.getIsHalfDay() != null ? request.getIsHalfDay() : false)
                .applicableTo(request.getApplicableTo())
                .organization(organization)
                .build();

        Holiday saved = holidayRepository.save(holiday);

        HolidayResponse response = HolidayResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .holidayDate(saved.getHolidayDate())
                .holidayType(saved.getHolidayType())
                .isRecurring(saved.getIsRecurring())
                .isHalfDay(saved.getIsHalfDay())
                .applicableTo(saved.getApplicableTo())
                .organizationId(organization.getId())
                .createdAt(saved.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Holiday created successfully"));
    }

    @DeleteMapping("/holidays/{id}")
    @Operation(summary = "Delete holiday")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable UUID id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", "id", id.toString()));
        holiday.setIsDeleted(true);
        holidayRepository.save(holiday);
        return ResponseEntity.ok(ApiResponse.success(null, "Holiday deleted"));
    }
}
