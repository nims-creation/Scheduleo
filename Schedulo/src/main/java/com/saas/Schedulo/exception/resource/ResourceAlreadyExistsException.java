package com.saas.Schedulo.exception.resource;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends TimetableException {
    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                "RES_002",
                HttpStatus.CONFLICT
        );
    }
}
