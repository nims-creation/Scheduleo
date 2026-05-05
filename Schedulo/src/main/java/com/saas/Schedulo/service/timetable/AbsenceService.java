package com.saas.Schedulo.service.timetable;

import com.saas.Schedulo.dto.request.timetable.MarkAbsentRequest;
import com.saas.Schedulo.dto.response.timetable.AbsenceResponse;
import com.saas.Schedulo.dto.response.user.UserResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing teacher absences and their impact on the timetable.
 *
 * <h3>Core Flow:</h3>
 * <pre>
 *  markAbsent(request)
 *    │
 *    ├── Fetch all ScheduleEntries assigned to teacher on absentDate
 *    │
 *    ├── IF substituteTeacherId is provided:
 *    │     └── For each entry: check ConflictDetectionService.hasUserConflict()
 *    │           ├── No conflict  → reassign entry (assignedTo = substitute, status = RESCHEDULED)
 *    │           └── Has conflict → cancel entry (status = CANCELLED) + note conflict reason
 *    │
 *    ├── IF no substitute:
 *    │     └── Cancel all entries (status = CANCELLED)
 *    │
 *    ├── Save TeacherAbsence record
 *    │
 *    └── Push notifications → affected students + original teacher + substitute (if any)
 * </pre>
 */
public interface AbsenceService {

    /**
     * Mark a teacher as absent for a date, cancel or reassign their ScheduleEntries,
     * and fire notifications to affected students and teachers.
     *
     * @param request the absence details including optional substitute
     * @return summary of the absence record and all affected entries
     */
    AbsenceResponse markAbsent(MarkAbsentRequest request);

    /**
     * Assign (or re-assign) a substitute teacher to a previously created absence record.
     * Useful when a substitute is found after the absence was already saved as CANCELLED.
     *
     * @param absenceId         existing absence record
     * @param substituteId      teacher to assign as substitute
     * @return updated absence response
     */
    AbsenceResponse assignSubstitute(UUID absenceId, UUID substituteId);

    /**
     * Revert an absence: restore all affected ScheduleEntries to SCHEDULED
     * and delete the absence record (e.g. teacher showed up after all).
     *
     * @param absenceId absence record to cancel
     */
    void cancelAbsence(UUID absenceId);

    /**
     * Find teachers who are free (no conflicting ScheduleEntry) during
     * all the slots of the absent teacher on the given date.
     * Scoped to the same organization.
     *
     * @param organizationId org scope for multi-tenancy
     * @param absentTeacherId teacher whose slots need to be covered
     * @param date            the absence date
     * @return list of teachers available to substitute
     */
    List<UserResponse> findAvailableSubstitutes(UUID organizationId, UUID absentTeacherId, LocalDate date);

    /**
     * Get all absence records for an organization within a date range.
     *
     * @param organizationId org scope
     * @param from           start of range (inclusive)
     * @param to             end of range (inclusive)
     * @return list of absences
     */
    List<AbsenceResponse> getAbsences(UUID organizationId, LocalDate from, LocalDate to);

    /**
     * Get all absence records for a specific teacher.
     *
     * @param teacherId teacher's user ID
     * @param from      start of range
     * @param to        end of range
     * @return list of absences
     */
    List<AbsenceResponse> getAbsencesByTeacher(UUID teacherId, LocalDate from, LocalDate to);
}
