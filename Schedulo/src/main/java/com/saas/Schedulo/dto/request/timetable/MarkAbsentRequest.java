package com.saas.Schedulo.dto.request.timetable;

import com.saas.Schedulo.entity.timetable.TeacherAbsence.AbsenceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request payload to mark a teacher as absent for a specific date.
 *
 * <p>Flow:
 * <ol>
 *   <li>Admin/HOD submits this request.</li>
 *   <li>System fetches all ScheduleEntries assigned to the teacher on {@code absentDate}.</li>
 *   <li>If {@code substituteTeacherId} is provided → system checks availability via
 *       ConflictDetectionService and assigns the substitute (status = RESCHEDULED).</li>
 *   <li>If no substitute → entries are marked CANCELLED.</li>
 *   <li>Notifications are pushed to affected students and both teachers.</li>
 * </ol>
 */
@Data
public class MarkAbsentRequest {

    /** Organization scope — enforced for multi-tenancy */
    @NotNull(message = "organizationId is required")
    private UUID organizationId;

    /** The teacher being marked absent */
    @NotNull(message = "teacherId is required")
    private UUID teacherId;

    /** Date of the absence */
    @NotNull(message = "absentDate is required")
    private LocalDate absentDate;

    /** FULL_DAY (default) or PARTIAL */
    private AbsenceType absenceType = AbsenceType.FULL_DAY;

    /** Required when absenceType = PARTIAL */
    private LocalTime partialFrom;

    /** Required when absenceType = PARTIAL */
    private LocalTime partialTo;

    /** Optional reason shown in notifications and records */
    private String reason;

    /**
     * Optional substitute teacher.
     * If provided, the system will attempt to reassign all affected entries.
     * If null, all affected entries are CANCELLED.
     */
    private UUID substituteTeacherId;

    /** Optional admin notes (internal only, not shown to students) */
    private String adminNotes;
}
