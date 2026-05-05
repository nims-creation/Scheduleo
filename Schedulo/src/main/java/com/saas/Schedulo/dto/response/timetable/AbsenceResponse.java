package com.saas.Schedulo.dto.response.timetable;

import com.saas.Schedulo.entity.timetable.TeacherAbsence.AbsenceResolution;
import com.saas.Schedulo.entity.timetable.TeacherAbsence.AbsenceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Response payload returned after marking a teacher absent.
 * Includes the absence record details plus a summary of what happened
 * to each affected ScheduleEntry (cancelled or reassigned).
 */
@Data
@Builder
public class AbsenceResponse {

    private UUID id;
    private UUID organizationId;

    // ── Absent teacher info ───────────────────────────────────────────────
    private UUID teacherId;
    private String teacherName;
    private String teacherEmail;

    // ── Absence details ───────────────────────────────────────────────────
    private LocalDate absentDate;
    private AbsenceType absenceType;
    private LocalTime partialFrom;
    private LocalTime partialTo;
    private String reason;
    private String adminNotes;

    // ── Resolution ────────────────────────────────────────────────────────
    private AbsenceResolution resolution;

    /** Present only when resolution = SUBSTITUTED */
    private UUID substituteTeacherId;
    private String substituteTeacherName;

    // ── Affected entries summary ──────────────────────────────────────────
    private int totalAffectedEntries;

    /**
     * Per-entry outcome — what happened to each class slot.
     */
    private List<AffectedEntryInfo> affectedEntries;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Inner summary record ──────────────────────────────────────────────
    @Data
    @Builder
    public static class AffectedEntryInfo {
        private UUID scheduleEntryId;
        private String entryTitle;
        private String dayOfWeek;
        private String timeSlot;            // e.g. "09:00 - 10:00"
        private String batchName;
        private String newStatus;           // "CANCELLED" or "RESCHEDULED"
        private String substituteTeacher;   // name, if reassigned
    }
}
