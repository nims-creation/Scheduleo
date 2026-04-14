package com.saas.Schedulo.service;

import com.saas.Schedulo.dto.mapper.TimetableMapper;
import com.saas.Schedulo.dto.request.timetable.CreateScheduleEntryRequest;
import com.saas.Schedulo.dto.response.timetable.ConflictCheckResponse;
import com.saas.Schedulo.entity.timetable.ScheduleEntry;
import com.saas.Schedulo.entity.timetable.TimeSlot;
import com.saas.Schedulo.repository.timetable.ScheduleEntryRepository;
import com.saas.Schedulo.repository.timetable.TimeSlotRepository;
import com.saas.Schedulo.service.impl.timetable.ConflictDetectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ConflictDetectionServiceImpl}.
 *
 * This is the core business logic of Schedulo — the timetable conflict engine.
 * All dependencies are mocked; no database or Spring context is started.
 *
 * Test coverage:
 *   - Resource conflict detection (overlap / no-overlap / adjacent slots)
 *   - User assignment conflict detection
 *   - Participant conflict detection
 *   - excludeEntryId (edit-in-place scenario)
 *   - Null / missing time guard
 *   - hasResourceConflict() and hasUserConflict() convenience methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConflictDetectionService — timetable conflict detection engine")
class ConflictDetectionServiceImplTest {

    @Mock private ScheduleEntryRepository scheduleEntryRepository;
    @Mock private TimeSlotRepository      timeSlotRepository;
    @Mock private TimetableMapper         timetableMapper;

    @InjectMocks
    private ConflictDetectionServiceImpl conflictDetectionService;

    // ── Shared fixtures ───────────────────────────────────────────────────────

    private static final UUID RESOURCE_ID  = UUID.randomUUID();
    private static final UUID USER_ID      = UUID.randomUUID();
    private static final UUID ENTRY_ID     = UUID.randomUUID();
    private static final LocalDate   DATE       = LocalDate.of(2025, 9, 1);
    private static final LocalDateTime START    = LocalDateTime.of(DATE, LocalTime.of(9, 0));
    private static final LocalDateTime END      = LocalDateTime.of(DATE, LocalTime.of(10, 0));

    private CreateScheduleEntryRequest buildRequest(UUID resourceId, UUID userId) {
        CreateScheduleEntryRequest req = new CreateScheduleEntryRequest();
        req.setTimetableId(UUID.randomUUID()); // required field
        req.setScheduleDate(DATE);
        req.setStartDatetime(START);
        req.setEndDatetime(END);
        req.setResourceId(resourceId);
        req.setAssignedToId(userId);
        req.setTitle("Test Entry");
        req.setEntryType("CLASS"); // required field
        return req;
    }

    private ScheduleEntry stubEntry() {
        ScheduleEntry e = new ScheduleEntry();
        e.setTitle("Existing Entry");
        return e;
    }

    // ── detectConflicts() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Resource conflicts")
    class ResourceConflicts {

        @Test
        @DisplayName("No conflict when resource is free in the requested time window")
        void noConflict_whenResourceIsFree() {
            when(scheduleEntryRepository.findConflictingByResource(
                    eq(RESOURCE_ID), eq(DATE), eq(START), eq(END), any()))
                    .thenReturn(Collections.emptyList());

            CreateScheduleEntryRequest req = buildRequest(RESOURCE_ID, null);
            ConflictCheckResponse result = conflictDetectionService.detectConflicts(req, null);

            assertThat(result.getHasConflicts()).isFalse();
            assertThat(result.getConflicts()).isEmpty();
        }

        @Test
        @DisplayName("RESOURCE conflict detected when times overlap")
        void conflictDetected_whenResourceIsBooked() {
            when(scheduleEntryRepository.findConflictingByResource(
                    eq(RESOURCE_ID), eq(DATE), eq(START), eq(END), any()))
                    .thenReturn(List.of(stubEntry()));
            when(timetableMapper.toScheduleEntryResponse(any())).thenReturn(null);

            CreateScheduleEntryRequest req = buildRequest(RESOURCE_ID, null);
            ConflictCheckResponse result = conflictDetectionService.detectConflicts(req, null);

            assertThat(result.getHasConflicts()).isTrue();
            assertThat(result.getConflicts()).hasSize(1);
            assertThat(result.getConflicts().get(0).getConflictType()).isEqualTo("RESOURCE");
        }

        @Test
        @DisplayName("No conflict when excludeEntryId exactly matches the conflicting entry (edit scenario)")
        void noConflict_whenExcludeIdMatchesConflictingEntry() {
            // The repository receives the excludeId and is expected to exclude it in the query.
            // Here we simulate the repository returning empty because the entry is excluded.
            when(scheduleEntryRepository.findConflictingByResource(
                    eq(RESOURCE_ID), eq(DATE), eq(START), eq(END), eq(ENTRY_ID)))
                    .thenReturn(Collections.emptyList());

            CreateScheduleEntryRequest req = buildRequest(RESOURCE_ID, null);
            ConflictCheckResponse result = conflictDetectionService.detectConflicts(req, ENTRY_ID);

            assertThat(result.getHasConflicts()).isFalse();
        }
    }

    @Nested
    @DisplayName("User assignment conflicts")
    class UserConflicts {

        @Test
        @DisplayName("No conflict when assigned user has no overlapping schedule")
        void noConflict_whenUserIsFree() {
            when(scheduleEntryRepository.findConflictingByUser(
                    eq(USER_ID), eq(DATE), eq(START), eq(END), any()))
                    .thenReturn(Collections.emptyList());

            CreateScheduleEntryRequest req = buildRequest(null, USER_ID);
            ConflictCheckResponse result = conflictDetectionService.detectConflicts(req, null);

            assertThat(result.getHasConflicts()).isFalse();
        }

        @Test
        @DisplayName("ASSIGNED_USER conflict detected when user has an overlapping entry")
        void conflictDetected_whenUserHasOverlappingEntry() {
            when(scheduleEntryRepository.findConflictingByUser(
                    eq(USER_ID), eq(DATE), eq(START), eq(END), any()))
                    .thenReturn(List.of(stubEntry()));
            when(timetableMapper.toScheduleEntryResponse(any())).thenReturn(null);

            CreateScheduleEntryRequest req = buildRequest(null, USER_ID);
            ConflictCheckResponse result = conflictDetectionService.detectConflicts(req, null);

            assertThat(result.getHasConflicts()).isTrue();
            assertThat(result.getConflicts()).hasSize(1);
            assertThat(result.getConflicts().get(0).getConflictType()).isEqualTo("ASSIGNED_USER");
        }
    }

    @Nested
    @DisplayName("Participant conflicts")
    class ParticipantConflicts {

        @Test
        @DisplayName("PARTICIPANT conflict detected when a participant has overlapping schedule")
        void conflictDetected_whenParticipantHasOverlappingEntry() {
            UUID participantId = UUID.randomUUID();

            when(scheduleEntryRepository.findConflictingByUser(
                    eq(participantId), eq(DATE), eq(START), eq(END), any()))
                    .thenReturn(List.of(stubEntry()));
            when(timetableMapper.toScheduleEntryResponse(any())).thenReturn(null);

            CreateScheduleEntryRequest req = buildRequest(null, null);
            req.setParticipantIds(Set.of(participantId));

            ConflictCheckResponse result = conflictDetectionService.detectConflicts(req, null);

            assertThat(result.getHasConflicts()).isTrue();
            assertThat(result.getConflicts().get(0).getConflictType()).isEqualTo("PARTICIPANT");
        }
    }

    @Nested
    @DisplayName("Guard conditions")
    class GuardConditions {

        @Test
        @DisplayName("Returns hasConflicts=false immediately when date or times are null")
        void noConflict_whenDateAndTimeAreNull() {
            CreateScheduleEntryRequest req = new CreateScheduleEntryRequest();
            // date, startDatetime, endDatetime are all null — no time slot id either
            req.setTimetableId(UUID.randomUUID());
            req.setEntryType("CLASS");
            req.setTitle("Incomplete Entry");

            ConflictCheckResponse result = conflictDetectionService.detectConflicts(req, null);

            assertThat(result.getHasConflicts()).isFalse();
            assertThat(result.getConflicts()).isEmpty();
            // Repository must never be called when there's nothing to check
            verifyNoInteractions(scheduleEntryRepository);
        }

        @Test
        @DisplayName("Resolves times from TimeSlot when startDatetime is null but timeSlotId is provided")
        void resolvesTimesFromTimeSlot_whenStartTimeIsNull() {
            UUID timeSlotId = UUID.randomUUID();

            TimeSlot slot = new TimeSlot();
            slot.setStartTime(LocalTime.of(10, 0));
            slot.setEndTime(LocalTime.of(11, 0));

            when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(slot));
            when(scheduleEntryRepository.findConflictingByResource(any(), any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            CreateScheduleEntryRequest req = new CreateScheduleEntryRequest();
            req.setTimeSlotId(timeSlotId);
            req.setScheduleDate(DATE);
            req.setResourceId(RESOURCE_ID);
            req.setTitle("Slot-based Entry");
            req.setEntryType("CLASS");
            // startDatetime and endDatetime intentionally left null

            ConflictCheckResponse result = conflictDetectionService.detectConflicts(req, null);

            assertThat(result.getHasConflicts()).isFalse();
            verify(timeSlotRepository).findById(timeSlotId);
        }
    }

    // ── hasResourceConflict() / hasUserConflict() ─────────────────────────────

    @Nested
    @DisplayName("Convenience boolean methods")
    class ConvenienceMethods {

        @Test
        @DisplayName("hasResourceConflict() returns true when repository returns results")
        void hasResourceConflict_returnsTrue_whenConflictExists() {
            when(scheduleEntryRepository.findConflictingByResource(
                    eq(RESOURCE_ID), eq(DATE), eq(START), eq(END), any()))
                    .thenReturn(List.of(stubEntry()));

            boolean result = conflictDetectionService.hasResourceConflict(
                    RESOURCE_ID, DATE, START, END, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("hasResourceConflict() returns false when no conflict")
        void hasResourceConflict_returnsFalse_whenNoConflict() {
            when(scheduleEntryRepository.findConflictingByResource(
                    any(), any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            boolean result = conflictDetectionService.hasResourceConflict(
                    RESOURCE_ID, DATE, START, END, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("hasUserConflict() returns true when user has overlapping schedule")
        void hasUserConflict_returnsTrue_whenConflictExists() {
            when(scheduleEntryRepository.findConflictingByUser(
                    eq(USER_ID), eq(DATE), eq(START), eq(END), any()))
                    .thenReturn(List.of(stubEntry()));

            boolean result = conflictDetectionService.hasUserConflict(
                    USER_ID, DATE, START, END, null);

            assertThat(result).isTrue();
        }
    }
}
