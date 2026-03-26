package com.saas.Schedulo.service.timetable;

import com.saas.Schedulo.dto.request.timetable.BulkScheduleRequest;
import com.saas.Schedulo.dto.request.timetable.CreateScheduleEntryRequest;
import com.saas.Schedulo.dto.response.timetable.ConflictCheckResponse;
import com.saas.Schedulo.dto.response.timetable.ScheduleEntryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScheduleService {
    ScheduleEntryResponse create(CreateScheduleEntryRequest request);
    ScheduleEntryResponse update(UUID id, CreateScheduleEntryRequest request);
    void delete(UUID id);
    List<ScheduleEntryResponse> getByTimetable(UUID timetableId);
    List<ScheduleEntryResponse> getByTimetableAndDay(UUID timetableId, java.time.DayOfWeek day);
    List<ScheduleEntryResponse> getByUserAndDateRange(UUID userId, LocalDate startDate, LocalDate endDate);
    List<ScheduleEntryResponse> getByResourceAndDate(UUID resourceId, LocalDate date);
    List<ScheduleEntryResponse> bulkCreate(BulkScheduleRequest request);
    ConflictCheckResponse checkConflicts(CreateScheduleEntryRequest request);
}
