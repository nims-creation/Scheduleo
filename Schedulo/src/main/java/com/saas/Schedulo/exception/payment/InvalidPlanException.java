package com.saas.Schedulo.exception.payment;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class InvalidPlanException extends TimetableException {
    public InvalidPlanException(String message) {
        super(message, "PAY_003", HttpStatus.BAD_REQUEST);
    }
}
