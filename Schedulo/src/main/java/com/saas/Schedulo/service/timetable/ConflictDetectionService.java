package com.saas.Schedulo.service.timetable;

import com.saas.Schedulo.dto.request.timetable.CreateScheduleEntryRequest;
import com.saas.Schedulo.dto.response.timetable.ConflictCheckResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface ConflictDetectionService {
    ConflictCheckResponse detectConflicts(CreateScheduleEntryRequest request, UUID excludeEntryId);
    boolean hasResourceConflict(UUID resourceId, LocalDate date,
                                java.time.LocalDateTime startTime,
                                java.time.LocalDateTime endTime,
                                UUID excludeEntryId);
    boolean hasUserConflict(UUID userId, LocalDate date,
                            java.time.LocalDateTime startTime,
                            java.time.LocalDateTime endTime,
                            UUID excludeEntryId);
}
