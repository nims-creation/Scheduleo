package com.saas.Schedulo.validation.validator;

import com.saas.Schedulo.dto.request.timetable.CreateScheduleEntryRequest;
import com.saas.Schedulo.validation.annotation.ValidSchedule;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ScheduleValidator implements ConstraintValidator<ValidSchedule, CreateScheduleEntryRequest> {

    @Override
    public boolean isValid(CreateScheduleEntryRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        boolean hasTimeSlot = request.getTimeSlotId() != null;
        boolean hasExplicitTime = request.getStartDatetime() != null && request.getEndDatetime() != null;

        if (!hasTimeSlot && !hasExplicitTime) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Either time slot or explicit start/end datetime must be provided"
            ).addConstraintViolation();
            return false;
        }

        if (request.getIsRecurring() && request.getRecurringPattern() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Recurring pattern is required when isRecurring is true"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
