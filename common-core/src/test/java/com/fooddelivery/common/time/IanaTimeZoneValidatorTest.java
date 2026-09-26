package com.fooddelivery.common.time;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class IanaTimeZoneValidatorTest {

    private final IanaTimeZoneValidator validator = new IanaTimeZoneValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Asia/Kolkata", "America/New_York", "America/St_Johns", "Pacific/Chatham", "Europe/London", "UTC"})
    void acceptsRegionIds(String zone) {
        assertThat(validator.isValid(zone, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"+05:30", "IST", "GMT+5", "UTC+05:30", "asia/kolkata", "Asia/Kolkata ", "", " ", "Mars/Olympus"})
    void rejectsOffsetsAbbreviationsAndTypos(String zone) {
        assertThat(validator.isValid(zone, null)).isFalse();
    }

    @org.junit.jupiter.api.Test
    void nullIsLeftToNotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
