package com.saas.Schedulo.exception.schedule;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ScheduleConflictException extends TimetableException {
    public ScheduleConflictException(String message) {
        super(message, "SCH_001", HttpStatus.CONFLICT);
    }
    public ScheduleConflictException(String message, List<?> conflicts) {
        super(message, "SCH_001", HttpStatus.CONFLICT, conflicts);
    }
}
