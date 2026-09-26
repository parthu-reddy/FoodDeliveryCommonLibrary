package com.fooddelivery.common.time;

import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZoneIdConverterTest {

    private final ZoneIdConverter converter = new ZoneIdConverter();

    @Test
    void roundTripsTheIanaId() {
        ZoneId zone = ZoneId.of("America/St_Johns");
        assertThat(converter.convertToDatabaseColumn(zone)).isEqualTo("America/St_Johns");
        assertThat(converter.convertToEntityAttribute("America/St_Johns")).isEqualTo(zone);
    }

    @Test
    void nullStaysNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void aCorruptStoredValueFailsLoudly() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("Mars/Olympus")).isInstanceOf(DateTimeException.class);
    }
}
