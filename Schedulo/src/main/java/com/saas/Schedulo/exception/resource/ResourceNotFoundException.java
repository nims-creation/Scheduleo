package com.saas.Schedulo.exception.resource;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends TimetableException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                "RES_001",
                HttpStatus.NOT_FOUND
        );
    }
    public ResourceNotFoundException(String message) {
        super(message, "RES_001", HttpStatus.NOT_FOUND);
    }
}
