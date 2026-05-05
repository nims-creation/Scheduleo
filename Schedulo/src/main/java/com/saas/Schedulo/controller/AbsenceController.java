package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.timetable.MarkAbsentRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.timetable.AbsenceResponse;
import com.saas.Schedulo.dto.response.user.UserResponse;
import com.saas.Schedulo.service.timetable.AbsenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.List;
import java.util.UUID;

/**
 * REST API for teacher absence management.
 *
 * <pre>
 * POST   /api/v1/absences                           → Mark teacher absent (cancel/substitute entries)
 * PATCH  /api/v1/absences/{id}/substitute           → Assign or change substitute
 * DELETE /api/v1/absences/{id}                      → Revert absence (restore entries to SCHEDULED)
 * GET    /api/v1/absences?orgId=&from=&to=          → List absences for an org in a date range
 * GET    /api/v1/absences/teacher/{teacherId}       → List absences for a specific teacher
 * GET    /api/v1/absences/substitutes               → Find available substitutes for a date
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/absences")
@RequiredArgsConstructor
@Tag(name = "Teacher Absences", description = "Handle teacher absences — auto-cancel or reassign affected timetable entries")
@SecurityRequirement(name = "bearerAuth")
public class AbsenceController {

    private final AbsenceService absenceService;

    // ─────────────────────────────────────────────────────────────────────────
    // Mark teacher absent
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(
            summary = "Mark a teacher as absent",
            description = """
                    Marks a teacher absent for a specific date.
                    All ScheduleEntries assigned to the teacher on that date are automatically:
                    - **RESCHEDULED** to a substitute teacher (if provided and available)
                    - **CANCELLED** if no substitute is given or the substitute is busy.
                    
                    In-app notifications are pushed to the teacher, substitute (if any),
                    and all affected student participants.
                    """
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AbsenceResponse>> markAbsent(
            @Valid @RequestBody MarkAbsentRequest request) {

        AbsenceResponse response = absenceService.markAbsent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,
                        "Teacher marked absent. " + response.getTotalAffectedEntries()
                                + " entry/entries processed with resolution: " + response.getResolution()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Assign substitute to an existing absence
    // ─────────────────────────────────────────────────────────────────────────

    @PatchMapping("/{absenceId}/substitute")
    @Operation(
            summary = "Assign or replace substitute teacher",
            description = """
                    Assigns a substitute teacher to an already-saved absence record.
                    Useful when a substitute is found *after* the absence was initially
                    saved as CANCELLED. The system will try to reassign the affected entries
                    (still respects conflict detection).
                    """
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AbsenceResponse>> assignSubstitute(
            @PathVariable UUID absenceId,
            @Parameter(description = "UUID of the substitute teacher")
            @RequestParam UUID substituteId) {

        AbsenceResponse response = absenceService.assignSubstitute(absenceId, substituteId);
        return ResponseEntity.ok(ApiResponse.success(response, "Substitute teacher assigned successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Revert / cancel absence
    // ─────────────────────────────────────────────────────────────────────────

    @DeleteMapping("/{absenceId}")
    @Operation(
            summary = "Cancel / revert an absence record",
            description = """
                    Reverts an absence record — restores all affected ScheduleEntries back
                    to SCHEDULED with the original teacher. Use this if the teacher showed
                    up after all, or the absence was entered in error.
                    """
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> cancelAbsence(@PathVariable UUID absenceId) {
        absenceService.cancelAbsence(absenceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Absence reverted — entries restored to SCHEDULED"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // List absences for an organization
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
            summary = "Get all absences for an organization",
            description = "Returns all teacher absence records for the given organization within the specified date range."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<AbsenceResponse>>> getAbsences(
            @RequestParam UUID orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<AbsenceResponse> response = absenceService.getAbsences(orgId, from, to);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // List absences for a specific teacher
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/teacher/{teacherId}")
    @Operation(
            summary = "Get absences for a specific teacher",
            description = "Returns absence history for a teacher — useful for attendance reports."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<AbsenceResponse>>> getAbsencesByTeacher(
            @PathVariable UUID teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<AbsenceResponse> response = absenceService.getAbsencesByTeacher(teacherId, from, to);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Find available substitutes
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/substitutes")
    @Operation(
            summary = "Find available substitute teachers",
            description = """
                    Returns a list of teachers (within the same organization) who are
                    completely free during all the absent teacher's scheduled slots on the given date.
                    Use this before calling markAbsent to pick an appropriate substitute.
                    """
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAvailableSubstitutes(
            @RequestParam UUID orgId,
            @RequestParam UUID absentTeacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<UserResponse> response = absenceService.findAvailableSubstitutes(orgId, absentTeacherId, date);
        return ResponseEntity.ok(ApiResponse.success(response,
                response.size() + " substitute(s) available on " + date));
    }
}
