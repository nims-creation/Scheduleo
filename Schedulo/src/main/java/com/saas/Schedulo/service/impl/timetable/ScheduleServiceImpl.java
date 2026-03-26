package com.saas.Schedulo.service.impl.timetable;

import com.saas.Schedulo.dto.mapper.TimetableMapper;
import com.saas.Schedulo.dto.request.timetable.BulkScheduleRequest;
import com.saas.Schedulo.dto.request.timetable.CreateScheduleEntryRequest;
import com.saas.Schedulo.dto.response.timetable.ConflictCheckResponse;
import com.saas.Schedulo.dto.response.timetable.ScheduleEntryResponse;
import com.saas.Schedulo.entity.timetable.ScheduleEntry;
import com.saas.Schedulo.entity.timetable.Timetable;
import com.saas.Schedulo.entity.timetable.TimeSlot;
import com.saas.Schedulo.exception.resource.ResourceConflictException;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.timetable.ScheduleEntryRepository;
import com.saas.Schedulo.repository.timetable.TimeSlotRepository;
import com.saas.Schedulo.repository.timetable.TimetableRepository;
import com.saas.Schedulo.service.timetable.ConflictDetectionService;
import com.saas.Schedulo.service.timetable.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final TimetableRepository timetableRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ConflictDetectionService conflictDetectionService;
    private final TimetableMapper timetableMapper;

    @Override
    public ScheduleEntryResponse create(CreateScheduleEntryRequest request) {
        log.info("Creating schedule entry for timetable: {}", request.getTimetableId());

        Timetable timetable = timetableRepository.findById(request.getTimetableId())
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", request.getTimetableId()));

        ConflictCheckResponse conflicts = conflictDetectionService.detectConflicts(request, null);
        if (conflicts.isHasConflicts()) {
            throw new ResourceConflictException("Cannot create schedule entry due to conflicts");
        }

        ScheduleEntry entry = mapToEntity(request, timetable);
        entry = scheduleEntryRepository.save(entry);
        
        return timetableMapper.toScheduleEntryResponse(entry);
    }

    @Override
    public ScheduleEntryResponse update(UUID id, CreateScheduleEntryRequest request) {
        ScheduleEntry entry = scheduleEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleEntry", "id", id));
        
        Timetable timetable = timetableRepository.findById(request.getTimetableId())
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", request.getTimetableId()));

        ConflictCheckResponse conflicts = conflictDetectionService.detectConflicts(request, id);
        if (conflicts.isHasConflicts()) {
            throw new ResourceConflictException("Cannot update schedule entry due to conflicts");
        }

        updateEntityFromRequest(entry, request, timetable);
        entry = scheduleEntryRepository.save(entry);
        
        return timetableMapper.toScheduleEntryResponse(entry);
    }

    @Override
    public void delete(UUID id) {
        ScheduleEntry entry = scheduleEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleEntry", "id", id));
        
        entry.setIsDeleted(true);
        scheduleEntryRepository.save(entry);
        log.info("Schedule entry soft deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleEntryResponse> getByTimetable(UUID timetableId) {
        return scheduleEntryRepository.findByTimetableId(timetableId).stream()
                .map(timetableMapper::toScheduleEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleEntryResponse> getByTimetableAndDay(UUID timetableId, java.time.DayOfWeek day) {
        return scheduleEntryRepository.findByTimetableAndDay(timetableId, day).stream()
                .map(timetableMapper::toScheduleEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleEntryResponse> getByUserAndDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        return scheduleEntryRepository.findByAssignedUserAndDateRange(userId, startDate, endDate).stream()
                .map(timetableMapper::toScheduleEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleEntryResponse> getByResourceAndDate(UUID resourceId, LocalDate date) {
        return scheduleEntryRepository.findByResourceAndDate(resourceId, date).stream()
                .map(timetableMapper::toScheduleEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleEntryResponse> bulkCreate(BulkScheduleRequest request) {
        Timetable timetable = timetableRepository.findById(request.getTimetableId())
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", request.getTimetableId()));

        List<ScheduleEntry> entriesToSave = new ArrayList<>();
        
        for (CreateScheduleEntryRequest entryRequest : request.getEntries()) {
            entryRequest.setTimetableId(request.getTimetableId());
            if (!request.getSkipConflicts()) {
                ConflictCheckResponse conflicts = conflictDetectionService.detectConflicts(entryRequest, null);
                if (conflicts.isHasConflicts()) {
                    if (!request.getOverwriteExisting()) {
                        throw new ResourceConflictException("Conflict detected in bulk creation");
                    }
                    // If overwrite existing, we could potentially find and delete those schedules,
                    // but for simplicity currently we won't implement the full overwrite cascade here.
                }
            }
            entriesToSave.add(mapToEntity(entryRequest, timetable));
        }

        entriesToSave = scheduleEntryRepository.saveAll(entriesToSave);
        return entriesToSave.stream()
                .map(timetableMapper::toScheduleEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ConflictCheckResponse checkConflicts(CreateScheduleEntryRequest request) {
        return conflictDetectionService.detectConflicts(request, null);
    }

    private ScheduleEntry mapToEntity(CreateScheduleEntryRequest request, Timetable timetable) {
        ScheduleEntry entry = ScheduleEntry.builder()
                .timetable(timetable)
                .title(request.getTitle())
                .description(request.getDescription())
                .dayOfWeek(request.getDayOfWeek())
                .scheduleDate(request.getScheduleDate())
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .entryType(request.getEntryType() != null ? ScheduleEntry.EntryType.valueOf(request.getEntryType()) : ScheduleEntry.EntryType.REGULAR)
                .color(request.getColor())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .notes(request.getNotes())
                .metadata(request.getMetadata())
                .build();
        
        if (request.getTimeSlotId() != null) {
            TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", request.getTimeSlotId()));
            entry.setTimeSlot(timeSlot);
        }
        
        return entry;
    }

    private void updateEntityFromRequest(ScheduleEntry entry, CreateScheduleEntryRequest request, Timetable timetable) {
        entry.setTimetable(timetable);
        entry.setTitle(request.getTitle());
        entry.setDescription(request.getDescription());
        entry.setDayOfWeek(request.getDayOfWeek());
        entry.setScheduleDate(request.getScheduleDate());
        entry.setStartDatetime(request.getStartDatetime());
        entry.setEndDatetime(request.getEndDatetime());
        if (request.getEntryType() != null) {
            entry.setEntryType(ScheduleEntry.EntryType.valueOf(request.getEntryType()));
        }
        entry.setColor(request.getColor());
        entry.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);
        entry.setNotes(request.getNotes());
        entry.setMetadata(request.getMetadata());
        
        if (request.getTimeSlotId() != null) {
            TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", request.getTimeSlotId()));
            entry.setTimeSlot(timeSlot);
        } else {
            entry.setTimeSlot(null);
        }
    }
}
