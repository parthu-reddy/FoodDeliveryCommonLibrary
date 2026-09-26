package com.fooddelivery.common.time;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;

/** See {@link IanaTimeZone}. */
public class IanaTimeZoneValidator implements ConstraintValidator<IanaTimeZone, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || isRegionId(value);
    }

    /**
     * The rule itself, for input that never passes through bean validation (a body read by hand, an
     * MCP tool). Region ids only: {@code ZoneId.of} would also accept "+05:30" and "GMT+5", which carry
     * no daylight-saving rules.
     */
    public static boolean isRegionId(String value) {
        return value != null && ZoneId.getAvailableZoneIds().contains(value);
    }
}
