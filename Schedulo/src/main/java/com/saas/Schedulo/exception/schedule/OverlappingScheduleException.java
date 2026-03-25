package com.saas.Schedulo.exception.schedule;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class OverlappingScheduleException extends TimetableException {
    public OverlappingScheduleException(String message) {
        super(message, "SCH_003", HttpStatus.CONFLICT);
    }
}
