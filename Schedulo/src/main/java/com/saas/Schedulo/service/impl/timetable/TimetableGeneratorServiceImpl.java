package com.saas.Schedulo.service.impl.timetable;

import com.saas.Schedulo.dto.request.timetable.CreateScheduleEntryRequest;
import com.saas.Schedulo.dto.request.timetable.GenerateTimetableRequest;
import com.saas.Schedulo.dto.request.timetable.RecurringPatternRequest;
import com.saas.Schedulo.dto.response.timetable.ConflictCheckResponse;
import com.saas.Schedulo.dto.response.timetable.ScheduleEntryResponse;
import com.saas.Schedulo.entity.timetable.TimeSlot;
import com.saas.Schedulo.entity.timetable.Timetable;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.timetable.TimetableRepository;
import com.saas.Schedulo.service.timetable.ConflictDetectionService;
import com.saas.Schedulo.service.timetable.ScheduleService;
import com.saas.Schedulo.service.timetable.TimetableGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableGeneratorServiceImpl implements TimetableGeneratorService {

    private final TimetableRepository timetableRepository;
    private final ScheduleService scheduleService;
    private final ConflictDetectionService conflictDetectionService;

    @Override
    @Transactional
    public List<ScheduleEntryResponse> generateTimetable(GenerateTimetableRequest request) {
        log.info("Starting timetable generation for timetable ID: {}", request.getTimetableId());

        Timetable timetable = timetableRepository.findById(request.getTimetableId())
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", request.getTimetableId()));

        List<TimeSlot> availableSlots = new ArrayList<>(timetable.getTimeSlots());

        if (availableSlots.isEmpty()) {
            throw new IllegalStateException("Cannot generate timetable because no timeslots are defined for this timetable.");
        }

        List<ScheduleEntryResponse> generatedEntries = new ArrayList<>();

        // Loop through each class requirement
        for (GenerateTimetableRequest.ClassRequirementRequest requirement : request.getClassRequirements()) {
            int scheduledPeriods = 0;

            // Sort or shuffle timeslots depending on if you want random assignment or sequential
            // Here we just iterate to find a free slot.
            for (TimeSlot slot : availableSlots) {
                if (scheduledPeriods >= requirement.getPeriodsPerWeek()) {
                    break;
                }

                if (slot.getSlotType() == TimeSlot.SlotType.BREAK || slot.getSlotType() == TimeSlot.SlotType.LUNCH) {
                    continue; // Skip breaks
                }

                // Prepare request
                CreateScheduleEntryRequest entryRequest = buildEntryRequest(timetable, requirement, slot);

                // Check for conflicts before committing
                ConflictCheckResponse conflictCheck = conflictDetectionService.detectConflicts(entryRequest, null);

                if (!Boolean.TRUE.equals(conflictCheck.getHasConflicts())) {
                    // We found a non-conflicting slot!
                    ScheduleEntryResponse createdEntry = scheduleService.create(entryRequest);
                    generatedEntries.add(createdEntry);
                    scheduledPeriods++;
                }
            }

            if (scheduledPeriods < requirement.getPeriodsPerWeek()) {
                log.warn("Could not fully schedule {}. Scheduled {}/{} periods.",
                        requirement.getTitle(), scheduledPeriods, requirement.getPeriodsPerWeek());
                // In a robust implementation, this would backtrack or throw an exception.
                // For now, it makes a best-effort allocation.
            }
        }

        log.info("Successfully generated {} schedule entries", generatedEntries.size());
        return generatedEntries;
    }

    private CreateScheduleEntryRequest buildEntryRequest(Timetable timetable,
                                                         GenerateTimetableRequest.ClassRequirementRequest req,
                                                         TimeSlot slot) {

        // Calculate the first occurrence date for the schedule
        LocalDate firstOccurrence = timetable.getEffectiveFrom();
        if (firstOccurrence.getDayOfWeek() != slot.getDayOfWeek()) {
            firstOccurrence = firstOccurrence.with(TemporalAdjusters.next(slot.getDayOfWeek()));
        }

        RecurringPatternRequest recurrence = RecurringPatternRequest.builder()
                .frequency("WEEKLY")
                .intervalValue(1)
                .startDate(firstOccurrence)
                .endDate(timetable.getEffectiveTo())
                .build();

        return CreateScheduleEntryRequest.builder()
                .timetableId(timetable.getId())
                .timeSlotId(slot.getId())
                .title(req.getTitle())
                .dayOfWeek(slot.getDayOfWeek())
                .scheduleDate(firstOccurrence)
                .startDatetime(firstOccurrence.atTime(slot.getStartTime()))
                .endDatetime(firstOccurrence.atTime(slot.getEndTime()))
                .resourceId(req.getResourceId())
                .assignedToId(req.getTeacherId())
                .entryType(req.getEntryType() != null ? req.getEntryType() : "CLASS")
                .isRecurring(true)
                .recurringPattern(recurrence)
                .color("#3498db") // default color
                .build();
    }
}
