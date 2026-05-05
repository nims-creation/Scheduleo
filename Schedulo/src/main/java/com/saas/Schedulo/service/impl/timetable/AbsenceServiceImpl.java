package com.saas.Schedulo.service.impl.timetable;

import com.saas.Schedulo.dto.request.timetable.MarkAbsentRequest;
import com.saas.Schedulo.dto.response.timetable.AbsenceResponse;
import com.saas.Schedulo.dto.response.timetable.AbsenceResponse.AffectedEntryInfo;
import com.saas.Schedulo.dto.response.user.UserResponse;
import com.saas.Schedulo.entity.notification.Notification;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.timetable.ScheduleEntry;
import com.saas.Schedulo.entity.timetable.ScheduleEntry.EntryStatus;
import com.saas.Schedulo.entity.timetable.TeacherAbsence;
import com.saas.Schedulo.entity.timetable.TeacherAbsence.AbsenceResolution;
import com.saas.Schedulo.entity.timetable.TeacherAbsence.AbsenceType;
import com.saas.Schedulo.entity.user.User;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.repository.timetable.ScheduleEntryRepository;
import com.saas.Schedulo.repository.timetable.TeacherAbsenceRepository;
import com.saas.Schedulo.repository.user.UserRepository;
import com.saas.Schedulo.service.notification.NotificationService;
import com.saas.Schedulo.service.timetable.AbsenceService;
import com.saas.Schedulo.service.timetable.ConflictDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core implementation of teacher absence handling.
 *
 * <h3>Strategy: Substitution-First, Cancellation-as-Fallback</h3>
 * <ol>
 *   <li>Load all ScheduleEntries assigned to the absent teacher on the absence date.</li>
 *   <li>If a substitute is provided → for each entry, run conflict check.
 *       <ul>
 *         <li>Free slot  → reassign to substitute, status = RESCHEDULED</li>
 *         <li>Busy slot  → cancel entry,            status = CANCELLED</li>
 *       </ul>
 *   </li>
 *   <li>If no substitute → cancel all entries (status = CANCELLED).</li>
 *   <li>Persist a {@link TeacherAbsence} record with final resolution.</li>
 *   <li>Push in-app notifications to all affected students, the absent teacher,
 *       and the substitute teacher (if any).</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AbsenceServiceImpl implements AbsenceService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final TeacherAbsenceRepository absenceRepository;
    private final ScheduleEntryRepository  scheduleEntryRepository;
    private final UserRepository           userRepository;
    private final OrganizationRepository   organizationRepository;
    private final ConflictDetectionService conflictDetectionService;
    private final NotificationService      notificationService;

    // ─────────────────────────────────────────────────────────────────────────
    // markAbsent
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public AbsenceResponse markAbsent(MarkAbsentRequest request) {
        log.info("Marking teacher {} absent on {}", request.getTeacherId(), request.getAbsentDate());

        // 1. Resolve entities
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User (teacher)", "id", request.getTeacherId()));

        User substitute = null;
        if (request.getSubstituteTeacherId() != null) {
            substitute = userRepository.findById(request.getSubstituteTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("User (substitute)", "id", request.getSubstituteTeacherId()));
        }

        // 2. Guard: prevent duplicate absence records for the same teacher + date
        if (absenceRepository.existsByTeacherIdAndAbsentDateAndIsDeletedFalse(
                teacher.getId(), request.getAbsentDate())) {
            throw new IllegalStateException(
                    "Teacher " + teacher.getFullName() + " is already marked absent on " + request.getAbsentDate());
        }

        // 3. Fetch all schedule entries for the teacher on that date
        List<ScheduleEntry> entries = scheduleEntryRepository
                .findByAssignedUserAndDateRange(teacher.getId(),
                        request.getAbsentDate(), request.getAbsentDate());

        // For PARTIAL absence, filter only entries that fall within the partial window
        if (request.getAbsenceType() == AbsenceType.PARTIAL
                && request.getPartialFrom() != null && request.getPartialTo() != null) {
            entries = entries.stream()
                    .filter(e -> isWithinPartialWindow(e, request))
                    .collect(Collectors.toList());
        }

        log.info("Found {} schedule entries affected by absence", entries.size());

        // 4. Process each entry
        List<AffectedEntryInfo> affectedInfos = new ArrayList<>();
        boolean atLeastOneSubstituted = false;

        for (ScheduleEntry entry : entries) {
            AffectedEntryInfo info = processEntry(entry, substitute, request.getAbsentDate());
            affectedInfos.add(info);
            if ("RESCHEDULED".equals(info.getNewStatus())) {
                atLeastOneSubstituted = true;
            }
        }

        scheduleEntryRepository.saveAll(entries);

        // 5. Determine overall resolution
        AbsenceResolution resolution;
        if (substitute == null) {
            resolution = AbsenceResolution.CANCELLED;
        } else if (atLeastOneSubstituted) {
            resolution = AbsenceResolution.SUBSTITUTED;
        } else {
            // Substitute was busy for every slot → all cancelled
            resolution = AbsenceResolution.CANCELLED;
        }

        // 6. Save TeacherAbsence record
        TeacherAbsence absence = TeacherAbsence.builder()
                .organization(org)
                .teacher(teacher)
                .absentDate(request.getAbsentDate())
                .absenceType(request.getAbsenceType() != null ? request.getAbsenceType() : AbsenceType.FULL_DAY)
                .partialFrom(request.getPartialFrom())
                .partialTo(request.getPartialTo())
                .reason(request.getReason())
                .substituteTeacher(substitute)
                .resolution(resolution)
                .affectedEntriesCount(entries.size())
                .adminNotes(request.getAdminNotes())
                .isActive(true)
                .isDeleted(false)
                .build();

        absence = absenceRepository.save(absence);
        log.info("TeacherAbsence record saved: {}", absence.getId());

        // 7. Push notifications
        sendAbsenceNotifications(teacher, substitute, entries, request.getAbsentDate(), resolution);

        return buildAbsenceResponse(absence, affectedInfos);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // assignSubstitute
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public AbsenceResponse assignSubstitute(UUID absenceId, UUID substituteId) {
        TeacherAbsence absence = absenceRepository.findById(absenceId)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherAbsence", "id", absenceId));

        User substitute = userRepository.findById(substituteId)
                .orElseThrow(() -> new ResourceNotFoundException("User (substitute)", "id", substituteId));

        // Re-fetch affected entries (still CANCELLED, belonging to original teacher on that date)
        List<ScheduleEntry> entries = scheduleEntryRepository
                .findByAssignedUserAndDateRange(absence.getTeacher().getId(),
                        absence.getAbsentDate(), absence.getAbsentDate())
                .stream()
                .filter(e -> e.getStatus() == EntryStatus.CANCELLED)
                .collect(Collectors.toList());

        List<AffectedEntryInfo> affectedInfos = new ArrayList<>();
        for (ScheduleEntry entry : entries) {
            AffectedEntryInfo info = processEntry(entry, substitute, absence.getAbsentDate());
            affectedInfos.add(info);
        }
        scheduleEntryRepository.saveAll(entries);

        absence.setSubstituteTeacher(substitute);
        absence.setResolution(AbsenceResolution.SUBSTITUTED);
        absence = absenceRepository.save(absence);

        // Notify substitute
        notificationService.createNotification(
                substitute.getId(),
                "New Class Assigned",
                "You have been assigned as substitute for " + absence.getTeacher().getFullName()
                        + " on " + absence.getAbsentDate(),
                Notification.NotificationType.TIMETABLE
        );

        return buildAbsenceResponse(absence, affectedInfos);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // cancelAbsence
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void cancelAbsence(UUID absenceId) {
        TeacherAbsence absence = absenceRepository.findById(absenceId)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherAbsence", "id", absenceId));

        // Restore affected entries to SCHEDULED
        List<ScheduleEntry> entries = scheduleEntryRepository
                .findByAssignedUserAndDateRange(absence.getTeacher().getId(),
                        absence.getAbsentDate(), absence.getAbsentDate());

        for (ScheduleEntry entry : entries) {
            if (entry.getStatus() == EntryStatus.CANCELLED
                    || entry.getStatus() == EntryStatus.RESCHEDULED) {
                entry.setStatus(EntryStatus.SCHEDULED);
                entry.setAssignedTo(absence.getTeacher());
                entry.setNotes(null);
            }
        }
        scheduleEntryRepository.saveAll(entries);

        absence.setIsDeleted(true);
        absenceRepository.save(absence);

        log.info("Absence {} cancelled — {} entries restored to SCHEDULED", absenceId, entries.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findAvailableSubstitutes
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAvailableSubstitutes(UUID organizationId, UUID absentTeacherId, LocalDate date) {
        // Get all entries of the absent teacher on that date to know the busy windows
        List<ScheduleEntry> absentEntries = scheduleEntryRepository
                .findByAssignedUserAndDateRange(absentTeacherId, date, date);

        // Get all teachers in the same org (excluding the absent teacher)
        List<User> allTeachers = userRepository.findByOrganizationIdAndIsDeletedFalse(organizationId)
                .stream()
                .filter(u -> !u.getId().equals(absentTeacherId))
                .collect(Collectors.toList());

        // Filter: keep only those who have NO conflict with ANY of the absent teacher's slots
        return allTeachers.stream()
                .filter(teacher -> isTeacherFreeForAllSlots(teacher, absentEntries, date))
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAbsences
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AbsenceResponse> getAbsences(UUID organizationId, LocalDate from, LocalDate to) {
        return absenceRepository.findByOrganizationAndDateRange(organizationId, from, to)
                .stream()
                .map(a -> buildAbsenceResponse(a, List.of()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbsenceResponse> getAbsencesByTeacher(UUID teacherId, LocalDate from, LocalDate to) {
        return absenceRepository.findByTeacherAndDateRange(teacherId, from, to)
                .stream()
                .map(a -> buildAbsenceResponse(a, List.of()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Process a single ScheduleEntry:
     * - If substitute is free → reassign (RESCHEDULED)
     * - Otherwise → cancel (CANCELLED)
     */
    private AffectedEntryInfo processEntry(ScheduleEntry entry, User substitute, LocalDate date) {
        boolean isSubstituted = false;

        if (substitute != null && entry.getStartDatetime() != null && entry.getEndDatetime() != null) {
            boolean hasConflict = conflictDetectionService.hasUserConflict(
                    substitute.getId(),
                    date,
                    entry.getStartDatetime(),
                    entry.getEndDatetime(),
                    entry.getId()
            );

            if (!hasConflict) {
                // Reassign to substitute
                entry.setAssignedTo(substitute);
                entry.setStatus(EntryStatus.RESCHEDULED);
                entry.setNotes("Substitute: " + substitute.getFullName()
                        + " (original teacher absent on " + date + ")");
                isSubstituted = true;
            }
        }

        if (!isSubstituted) {
            entry.setStatus(EntryStatus.CANCELLED);
            entry.setNotes("Cancelled — teacher absent on " + date);
        }

        // Build summary info
        String timeSlotLabel = buildTimeSlotLabel(entry);
        String batchName = entry.getBatch() != null ? entry.getBatch().getName() : "—";

        return AffectedEntryInfo.builder()
                .scheduleEntryId(entry.getId())
                .entryTitle(entry.getTitle())
                .dayOfWeek(entry.getDayOfWeek() != null ? entry.getDayOfWeek().name() : "—")
                .timeSlot(timeSlotLabel)
                .batchName(batchName)
                .newStatus(isSubstituted ? "RESCHEDULED" : "CANCELLED")
                .substituteTeacher(isSubstituted && substitute != null ? substitute.getFullName() : null)
                .build();
    }

    /**
     * True when the entry's time window falls within the PARTIAL absence window.
     */
    private boolean isWithinPartialWindow(ScheduleEntry entry, MarkAbsentRequest request) {
        if (entry.getTimeSlot() == null) return true; // no slot info → include by default
        var slotStart = entry.getTimeSlot().getStartTime();
        var slotEnd   = entry.getTimeSlot().getEndTime();
        return !slotStart.isBefore(request.getPartialFrom())
                && !slotEnd.isAfter(request.getPartialTo());
    }

    /**
     * Returns true if the given teacher has NO conflict with ANY of the busy entries.
     */
    private boolean isTeacherFreeForAllSlots(User teacher, List<ScheduleEntry> busyEntries, LocalDate date) {
        for (ScheduleEntry entry : busyEntries) {
            if (entry.getStartDatetime() == null || entry.getEndDatetime() == null) continue;
            boolean conflict = conflictDetectionService.hasUserConflict(
                    teacher.getId(), date,
                    entry.getStartDatetime(), entry.getEndDatetime(), null);
            if (conflict) return false;
        }
        return true;
    }

    /**
     * Fire notifications to all affected participants, the absent teacher,
     * and the substitute (if any).
     */
    private void sendAbsenceNotifications(User teacher, User substitute,
                                          List<ScheduleEntry> entries,
                                          LocalDate date, AbsenceResolution resolution) {
        // Notify absent teacher
        notificationService.createNotification(
                teacher.getId(),
                "Your classes on " + date + " — " + resolution.name(),
                resolution == AbsenceResolution.SUBSTITUTED
                        ? "Your " + entries.size() + " class(es) on " + date + " have been covered by a substitute."
                        : "Your " + entries.size() + " class(es) on " + date + " have been cancelled.",
                Notification.NotificationType.TIMETABLE
        );

        // Notify substitute
        if (substitute != null && resolution == AbsenceResolution.SUBSTITUTED) {
            notificationService.createNotification(
                    substitute.getId(),
                    "New Classes Assigned — " + date,
                    "You have been assigned to cover " + teacher.getFullName()
                            + "'s classes on " + date + ".",
                    Notification.NotificationType.TIMETABLE
            );
        }

        // Notify each student participant
        entries.stream()
                .flatMap(e -> e.getParticipants().stream())
                .distinct()
                .forEach(student -> notificationService.createNotification(
                        student.getId(),
                        "Class Update — " + date,
                        resolution == AbsenceResolution.SUBSTITUTED
                                ? teacher.getFullName() + " is absent. Substitute teacher assigned on " + date + "."
                                : teacher.getFullName() + "'s class on " + date + " is CANCELLED.",
                        Notification.NotificationType.TIMETABLE
                ));
    }

    private String buildTimeSlotLabel(ScheduleEntry entry) {
        if (entry.getTimeSlot() != null) {
            return entry.getTimeSlot().getStartTime().format(TIME_FMT)
                    + " – " + entry.getTimeSlot().getEndTime().format(TIME_FMT);
        }
        if (entry.getStartDatetime() != null && entry.getEndDatetime() != null) {
            return entry.getStartDatetime().toLocalTime().format(TIME_FMT)
                    + " – " + entry.getEndDatetime().toLocalTime().format(TIME_FMT);
        }
        return "—";
    }

    private AbsenceResponse buildAbsenceResponse(TeacherAbsence absence, List<AffectedEntryInfo> affectedInfos) {
        return AbsenceResponse.builder()
                .id(absence.getId())
                .organizationId(absence.getOrganization().getId())
                .teacherId(absence.getTeacher().getId())
                .teacherName(absence.getTeacher().getFullName())
                .teacherEmail(absence.getTeacher().getEmail())
                .absentDate(absence.getAbsentDate())
                .absenceType(absence.getAbsenceType())
                .partialFrom(absence.getPartialFrom())
                .partialTo(absence.getPartialTo())
                .reason(absence.getReason())
                .adminNotes(absence.getAdminNotes())
                .resolution(absence.getResolution())
                .substituteTeacherId(absence.getSubstituteTeacher() != null
                        ? absence.getSubstituteTeacher().getId() : null)
                .substituteTeacherName(absence.getSubstituteTeacher() != null
                        ? absence.getSubstituteTeacher().getFullName() : null)
                .totalAffectedEntries(absence.getAffectedEntriesCount())
                .affectedEntries(affectedInfos)
                .createdAt(absence.getCreatedAt())
                .updatedAt(absence.getUpdatedAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .build();
    }
}
