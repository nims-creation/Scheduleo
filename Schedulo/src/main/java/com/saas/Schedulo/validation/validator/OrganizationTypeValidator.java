package com.saas.Schedulo.validation.validator;

import com.timetable.entity.organization.OrganizationType;
import com.timetable.validation.annotation.ValidOrganizationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class OrganizationTypeValidator implements ConstraintValidator<ValidOrganizationType, String> {

    private static final Set<String> VALID_TYPES = Arrays.stream(OrganizationType.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        return VALID_TYPES.contains(value.toUpperCase());
    }
}
