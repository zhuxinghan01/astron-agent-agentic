package com.iflytek.astron.console.hub.validation.point;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ResourceTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidResourceType {
    String message() default "{resource.type.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
