package com.saas.Schedulo.dto.response.timetable;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConflictCheckResponse {
    private Boolean hasConflicts;
    private List<ConflictDetail> conflicts;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConflictDetail {
        private String conflictType;
        private String message;
        private ScheduleEntryResponse existingEntry;
        private ScheduleEntryResponse conflictingEntry;
    }
}
