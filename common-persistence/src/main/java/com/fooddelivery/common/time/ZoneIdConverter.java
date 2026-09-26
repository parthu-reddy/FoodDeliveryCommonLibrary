package com.fooddelivery.common.time;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.ZoneId;

/**
 * Stores a {@link ZoneId} as its IANA id ({@code VARCHAR}), so that an entity carries a type that
 * cannot hold a malformed zone and callers don't re-parse a string at every use.
 *
 * <p>Applied explicitly with {@code @Convert(converter = ZoneIdConverter.class)}, not auto-applied.
 */
@Converter
public class ZoneIdConverter implements AttributeConverter<ZoneId, String> {

    @Override
    public String convertToDatabaseColumn(ZoneId zone) {
        return zone == null ? null : zone.getId();
    }

    @Override
    public ZoneId convertToEntityAttribute(String id) {
        return id == null ? null : ZoneId.of(id);
    }
}
