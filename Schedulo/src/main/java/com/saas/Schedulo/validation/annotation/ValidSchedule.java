package com.saas.Schedulo.validation.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ScheduleValidator.class)
@Documented
public @interface ValidSchedule {

    String message() default "Invalid schedule configuration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
