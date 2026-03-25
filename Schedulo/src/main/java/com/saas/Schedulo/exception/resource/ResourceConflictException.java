package com.saas.Schedulo.exception.resource;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class ResourceConflictException extends TimetableException {
    public ResourceConflictException(String message) {
        super(message, "RES_003", HttpStatus.CONFLICT);
    }
    public ResourceConflictException(String message, Object details) {
        super(message, "RES_003", HttpStatus.CONFLICT, details);
    }
}
