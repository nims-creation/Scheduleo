package com.saas.Schedulo.validation.validator;

import com.timetable.validation.annotation.ValidTimeRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.time.LocalTime;
import java.time.temporal.Temporal;

public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, Object> {

    private String startField;
    private String endField;

    @Override
    public void initialize(ValidTimeRange constraintAnnotation) {
        this.startField = constraintAnnotation.startField();
        this.endField = constraintAnnotation.endField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        BeanWrapper beanWrapper = new BeanWrapperImpl(value);
        Object startValue = beanWrapper.getPropertyValue(startField);
        Object endValue = beanWrapper.getPropertyValue(endField);

        if (startValue == null || endValue == null) return true;

        if (startValue instanceof LocalTime start && endValue instanceof LocalTime end) {
            return end.isAfter(start);
        }

        if (startValue instanceof Temporal && endValue instanceof Temporal) {
            return ((Comparable<Temporal>) endValue).compareTo((Temporal) startValue) > 0;
        }

        return true;
    }
}









