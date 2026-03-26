package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.timetable.BulkScheduleRequest;
import com.saas.Schedulo.dto.request.timetable.CreateScheduleEntryRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.timetable.ConflictCheckResponse;
import com.saas.Schedulo.dto.response.timetable.ScheduleEntryResponse;
import com.saas.Schedulo.service.timetable.ScheduleService;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedules", description = "Schedule entry management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "Create schedule entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<ScheduleEntryResponse>> create(
            @Valid @RequestBody CreateScheduleEntryRequest request) {
        ScheduleEntryResponse response = scheduleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Schedule entry created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update schedule entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<ScheduleEntryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateScheduleEntryRequest request) {
        ScheduleEntryResponse response = scheduleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule entry updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete schedule entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        scheduleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule entry deleted"));
    }

    @GetMapping("/timetable/{timetableId}")
    @Operation(summary = "Get entries by timetable")
    public ResponseEntity<ApiResponse<List<ScheduleEntryResponse>>> getByTimetable(
            @PathVariable UUID timetableId) {
        List<ScheduleEntryResponse> response = scheduleService.getByTimetable(timetableId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/timetable/{timetableId}/day/{day}")
    @Operation(summary = "Get entries by timetable and day")
    public ResponseEntity<ApiResponse<List<ScheduleEntryResponse>>> getByTimetableAndDay(
            @PathVariable UUID timetableId,
            @PathVariable DayOfWeek day) {
        List<ScheduleEntryResponse> response = scheduleService.getByTimetableAndDay(timetableId, day);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get entries by user and date range")
    public ResponseEntity<ApiResponse<List<ScheduleEntryResponse>>> getByUserAndDateRange(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ScheduleEntryResponse> response =
                scheduleService.getByUserAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/resource/{resourceId}/date/{date}")
    @Operation(summary = "Get entries by resource and date")
    public ResponseEntity<ApiResponse<List<ScheduleEntryResponse>>> getByResourceAndDate(
            @PathVariable UUID resourceId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ScheduleEntryResponse> response = scheduleService.getByResourceAndDate(resourceId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk create schedule entries")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<ScheduleEntryResponse>>> bulkCreate(
            @Valid @RequestBody BulkScheduleRequest request) {
        List<ScheduleEntryResponse> response = scheduleService.bulkCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Bulk schedule entries created"));
    }

    @PostMapping("/check-conflicts")
    @Operation(summary = "Check for conflicts")
    public ResponseEntity<ApiResponse<ConflictCheckResponse>> checkConflicts(
            @Valid @RequestBody CreateScheduleEntryRequest request) {
        ConflictCheckResponse response = scheduleService.checkConflicts(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

