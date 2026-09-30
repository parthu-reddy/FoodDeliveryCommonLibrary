package com.fooddelivery.common.location;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A canonical fleet city identifier, for example {@code BLR}.
 *
 * <p>This identifies an operating area. It is deliberately separate from a display address city
 * such as {@code Bengaluru}: fleet, dispatch and administrative map queries need a stable key that
 * can safely be used in database predicates and Redis key suffixes. {@code null} is valid so that
 * callers can combine this annotation with {@code @NotBlank} when a value is required.
 */
@Documented
@Constraint(validatedBy = CityIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CityId {

    String message() default "must be a canonical city id such as BLR";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
