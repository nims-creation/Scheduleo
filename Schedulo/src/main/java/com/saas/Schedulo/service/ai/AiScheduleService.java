package com.saas.Schedulo.service.ai;

import com.saas.Schedulo.dto.request.timetable.CreateScheduleEntryRequest;
import com.saas.Schedulo.dto.response.timetable.ScheduleEntryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AiScheduleService {
    List<CreateScheduleEntryRequest> suggestOptimalSchedule(
            UUID organizationId,
            UUID timetableId,
            LocalDate startDate,
            LocalDate endDate,
            SchedulePreferences preferences
    );
    List<ScheduleEntryResponse> detectAndResolveConflicts(
            UUID timetableId,
            List<CreateScheduleEntryRequest> entries
    );
    String generateScheduleSummary(UUID timetableId);
    ScheduleAnalysis analyzeScheduleEfficiency(UUID timetableId);
    record SchedulePreferences(
            Integer maxEntriesPerDay,
            Integer minBreakBetweenEntries,
            List<String> preferredTimeSlots,
            List<String> avoidDays,
            Boolean balanceWorkload
    ) {}
    record ScheduleAnalysis(
            double utilizationRate,
            int conflictCount,
            List<String> recommendations,
            Map<String, Double> resourceUtilization
    ) {}
}
