package com.saas.Schedulo.dto.request.timetable;

import jakarta.validation.constraints.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkScheduleRequest {

    @NotNull(message = "Timetable ID is required")
    private UUID timetableId;

    @NotEmpty(message = "At least one schedule entry is required")
    @Valid
    private List<CreateScheduleEntryRequest> entries;

    private Boolean skipConflicts = false;
    private Boolean overwriteExisting = false;
}
