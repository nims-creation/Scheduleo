package com.saas.Schedulo.service.impl.timetable;


import com.saas.Schedulo.dto.mapper.TimetableMapper;
import com.saas.Schedulo.dto.request.timetable.CreateScheduleEntryRequest;
import com.saas.Schedulo.dto.response.timetable.ConflictCheckResponse;
import com.saas.Schedulo.entity.timetable.ScheduleEntry;
import com.saas.Schedulo.entity.timetable.TimeSlot;
import com.saas.Schedulo.repository.timetable.ScheduleEntryRepository;
import com.saas.Schedulo.repository.timetable.TimeSlotRepository;
import com.saas.Schedulo.service.timetable.ConflictDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConflictDetectionServiceImpl implements ConflictDetectionService {

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimetableMapper timetableMapper;

    @Override
    public ConflictCheckResponse detectConflicts(CreateScheduleEntryRequest request, UUID excludeEntryId) {
        List<ConflictCheckResponse.ConflictDetail> conflicts = new ArrayList<>();

        LocalDate date = request.getScheduleDate();
        LocalDateTime startTime = request.getStartDatetime();
        LocalDateTime endTime = request.getEndDatetime();

        // If using time slot, resolve times
        if (request.getTimeSlotId() != null && (startTime == null || endTime == null)) {
            TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId()).orElse(null);
            if (timeSlot != null) {
                if (date == null) {
                    date = LocalDate.now(); // Or derive from day of week
                }
                startTime = LocalDateTime.of(date, timeSlot.getStartTime());
                endTime = LocalDateTime.of(date, timeSlot.getEndTime());
            }
        }

        if (date == null || startTime == null || endTime == null) {
            return ConflictCheckResponse.builder()
                    .hasConflicts(false)
                    .conflicts(conflicts)
                    .build();
        }

        UUID excludeId = excludeEntryId != null ? excludeEntryId : UUID.randomUUID();

        // Check resource conflicts
        if (request.getResourceId() != null) {
            List<ScheduleEntry> resourceConflicts = scheduleEntryRepository.findConflictingByResource(
                    request.getResourceId(), date, startTime, endTime, excludeId
            );
            for (ScheduleEntry conflict : resourceConflicts) {
                conflicts.add(ConflictCheckResponse.ConflictDetail.builder()
                        .conflictType("RESOURCE")
                        .message("Resource is already booked for this time slot")
                        .existingEntry(timetableMapper.toScheduleEntryResponse(conflict))
                        .build());
            }
        }

        // Check assigned user conflicts
        if (request.getAssignedToId() != null) {
            List<ScheduleEntry> userConflicts = scheduleEntryRepository.findConflictingByUser(
                    request.getAssignedToId(), date, startTime, endTime, excludeId
            );
            for (ScheduleEntry conflict : userConflicts) {
                conflicts.add(ConflictCheckResponse.ConflictDetail.builder()
                        .conflictType("ASSIGNED_USER")
                        .message("Assigned user has another schedule at this time")
                        .existingEntry(timetableMapper.toScheduleEntryResponse(conflict))
                        .build());
            }
        }

        // Check participant conflicts
        if (request.getParticipantIds() != null) {
            for (UUID participantId : request.getParticipantIds()) {
                List<ScheduleEntry> participantConflicts = scheduleEntryRepository.findConflictingByUser(
                        participantId, date, startTime, endTime, excludeId
                );
                for (ScheduleEntry conflict : participantConflicts) {
                    conflicts.add(ConflictCheckResponse.ConflictDetail.builder()
                            .conflictType("PARTICIPANT")
                            .message("Participant has another schedule at this time")
                            .existingEntry(timetableMapper.toScheduleEntryResponse(conflict))
                            .build());
                }
            }
        }

        return ConflictCheckResponse.builder()
                .hasConflicts(!conflicts.isEmpty())
                .conflicts(conflicts)
                .build();
    }

    @Override
    public boolean hasResourceConflict(UUID resourceId, LocalDate date,
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       UUID excludeEntryId) {
        UUID excludeId = excludeEntryId != null ? excludeEntryId : UUID.randomUUID();
        return !scheduleEntryRepository.findConflictingByResource(
                resourceId, date, startTime, endTime, excludeId
        ).isEmpty();
    }

    @Override
    public boolean hasUserConflict(UUID userId, LocalDate date,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   UUID excludeEntryId) {
        UUID excludeId = excludeEntryId != null ? excludeEntryId : UUID.randomUUID();
        return !scheduleEntryRepository.findConflictingByUser(
                userId, date, startTime, endTime, excludeId
        ).isEmpty();
    }
}

