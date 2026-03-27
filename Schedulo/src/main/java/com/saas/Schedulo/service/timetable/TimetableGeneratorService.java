package com.saas.Schedulo.service.timetable;

import com.saas.Schedulo.dto.request.timetable.GenerateTimetableRequest;
import com.saas.Schedulo.dto.response.timetable.ScheduleEntryResponse;

import java.util.List;

public interface TimetableGeneratorService {
    List<ScheduleEntryResponse> generateTimetable(GenerateTimetableRequest request);
}
