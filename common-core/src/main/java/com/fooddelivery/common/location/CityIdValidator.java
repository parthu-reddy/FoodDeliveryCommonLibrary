package com.fooddelivery.common.location;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/** Validation and parsing support for {@link CityId}. */
public final class CityIdValidator implements ConstraintValidator<CityId, String> {

    /**
     * Uppercase identifiers are intentional. A single canonical spelling keeps values from
     * fragmenting Redis keys and SQL city predicates through case or whitespace differences.
     */
    public static final String REGEX = "^[A-Z][A-Z0-9_-]{0,63}$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || isCanonical(value);
    }

    public static boolean isCanonical(String value) {
        return value != null && PATTERN.matcher(value).matches();
    }

    /**
     * Protects service and tool paths that do not pass through bean validation.
     *
     * @throws IllegalArgumentException when the value is null, blank, mixed-case, or contains an
     *                                  unsafe delimiter.
     */
    public static String requireCanonical(String value) {
        if (!isCanonical(value)) {
            throw new IllegalArgumentException("cityId must be a canonical uppercase identifier such as BLR");
        }
        return value;
    }
}
