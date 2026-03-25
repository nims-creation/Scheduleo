package com.saas.Schedulo.exception.schedule;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class InvalidTimeSlotException extends TimetableException {
    public InvalidTimeSlotException(String message) {
        super(message, "SCH_002", HttpStatus.BAD_REQUEST);
    }
}