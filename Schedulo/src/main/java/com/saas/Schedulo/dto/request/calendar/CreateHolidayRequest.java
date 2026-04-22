package com.saas.Schedulo.dto.request.calendar;

import com.saas.Schedulo.entity.calendar.Holiday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHolidayRequest {

    @NotBlank(message = "Holiday name is required")
    private String name;

    private String description;

    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    @NotNull(message = "Holiday type is required")
    private Holiday.HolidayType holidayType;

    @Builder.Default
    private Boolean isRecurring = false;
    @Builder.Default
    private Boolean isHalfDay = false;
    private String applicableTo;
}
