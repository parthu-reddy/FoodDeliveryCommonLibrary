package com.fooddelivery.common.event;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Class-level shape rule for a {@link FulfilmentScopedEvent}; each broken field is reported under its own name. */
@Documented
@Constraint(validatedBy = FulfilmentShapeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FulfilmentShape {
    String message() default "event shape does not match its fulfilmentType";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
