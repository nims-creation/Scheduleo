package com.saas.Schedulo.validation.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrganizationTypeValidator.class)
@Documented
public @interface ValidOrganizationType {

    String message() default "Invalid organization type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
