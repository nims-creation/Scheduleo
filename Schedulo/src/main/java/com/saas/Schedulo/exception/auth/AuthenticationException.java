package com.saas.Schedulo.exception.auth;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;
public class AuthenticationException extends TimetableException {
    public AuthenticationException(String message) {
        super(message, "AUTH_001", HttpStatus.UNAUTHORIZED);
    }
}
