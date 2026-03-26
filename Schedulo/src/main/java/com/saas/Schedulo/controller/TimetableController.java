package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.timetable.CreateTimetableRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.timetable.TimetableResponse;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.service.timetable.TimetableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/timetables")
@RequiredArgsConstructor
@Tag(name = "Timetables", description = "Timetable management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping
    @Operation(summary = "Create timetable", description = "Create a new timetable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TimetableResponse>> create(
            @Valid @RequestBody CreateTimetableRequest request,
            @CurrentUser CustomUserDetails currentUser) {
        TimetableResponse response = timetableService.create(request, currentUser.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Timetable created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get timetable", description = "Get timetable by ID")
    public ResponseEntity<ApiResponse<TimetableResponse>> getById(@PathVariable UUID id) {
        TimetableResponse response = timetableService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update timetable", description = "Update an existing timetable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TimetableResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTimetableRequest request) {
        TimetableResponse response = timetableService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Timetable updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete timetable", description = "Delete a timetable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        timetableService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Timetable deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "List timetables", description = "Get paginated list of timetables")
    public ResponseEntity<ApiResponse<PagedResponse<TimetableResponse>>> list(
            @CurrentUser CustomUserDetails currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<TimetableResponse> response =
                timetableService.getByOrganization(currentUser.getOrganizationId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active timetables", description = "Get timetables active on a specific date")
    public ResponseEntity<ApiResponse<List<TimetableResponse>>> getActive(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam(required = false) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<TimetableResponse> response =
                timetableService.getActiveByDate(currentUser.getOrganizationId(), targetDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish timetable", description = "Publish a timetable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TimetableResponse>> publish(@PathVariable UUID id) {
        TimetableResponse response = timetableService.publish(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Timetable published successfully"));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive timetable", description = "Archive a timetable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TimetableResponse>> archive(@PathVariable UUID id) {
        TimetableResponse response = timetableService.archive(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Timetable archived successfully"));
    }

    @PostMapping("/{id}/duplicate-template")
    @Operation(summary = "Create template", description = "Create a template from existing timetable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TimetableResponse>> duplicateAsTemplate(
            @PathVariable UUID id,
            @RequestParam String templateName) {
        TimetableResponse response = timetableService.duplicateAsTemplate(id, templateName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Template created successfully"));
    }

    @PostMapping("/from-template/{templateId}")
    @Operation(summary = "Create from template", description = "Create new timetable from template")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TimetableResponse>> createFromTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody CreateTimetableRequest request) {
        TimetableResponse response = timetableService.createFromTemplate(templateId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Timetable created from template"));
    }
}

