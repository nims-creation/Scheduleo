package com.saas.Schedulo.exception.organization;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class InvalidOrganizationTypeException extends TimetableException {
    public InvalidOrganizationTypeException(String organizationType) {
        super(
                String.format("Invalid organization type: %s", organizationType),
                "ORG_002",
                HttpStatus.BAD_REQUEST
        );
    }
}
