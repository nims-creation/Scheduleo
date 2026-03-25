package com.saas.Schedulo.exception.auth;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends TimetableException {
    public InvalidTokenException(String message) {
        super(message, "AUTH_002", HttpStatus.UNAUTHORIZED);
    }
    public InvalidTokenException() {
        super("Invalid or malformed token", "AUTH_002", HttpStatus.UNAUTHORIZED);
    }
}
