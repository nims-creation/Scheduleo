package com.saas.Schedulo.exception.organization;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class OrganizationLimitExceededException extends TimetableException {
    public OrganizationLimitExceededException(String limitType, int currentCount, int maxAllowed) {
        super(
                String.format("Organization %s limit exceeded. Current: %d, Maximum: %d",
                        limitType, currentCount, maxAllowed),
                "ORG_001",
                HttpStatus.FORBIDDEN
        );
    }
}
