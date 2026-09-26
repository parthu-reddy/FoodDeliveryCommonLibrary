package com.fooddelivery.common.time;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An IANA time zone id, such as {@code Asia/Kolkata} or {@code America/New_York}.
 *
 * <p>Offsets ({@code +05:30}) and abbreviations ({@code IST}, which also names Irish and Israel
 * Standard Time) are rejected. An offset has no daylight-saving rules, so a restaurant or
 * advertiser stored as {@code -05:00} would be an hour wrong for half of every year. {@code null} is
 * valid, so combine with {@code @NotBlank} where the zone is required.
 */
@Documented
@Constraint(validatedBy = IanaTimeZoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface IanaTimeZone {

    String message() default "must be an IANA time zone id such as Asia/Kolkata";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
