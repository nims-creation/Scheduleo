package com.saas.Schedulo.exception.auth;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends TimetableException {
    public TokenExpiredException() {
        super("Token has expired", "AUTH_003", HttpStatus.UNAUTHORIZED);
    }
    public TokenExpiredException(String tokenType) {
        super(tokenType + " token has expired", "AUTH_003", HttpStatus.UNAUTHORIZED);
    }
}
