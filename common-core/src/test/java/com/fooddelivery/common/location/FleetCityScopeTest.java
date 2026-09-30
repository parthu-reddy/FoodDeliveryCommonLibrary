package com.fooddelivery.common.location;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleetCityScopeTest {

    @Test
    void acceptsOnlyCanonicalCityIdentifiers() {
        assertThat(CityIdValidator.isCanonical("BLR")).isTrue();
        assertThat(CityIdValidator.isCanonical("NYC_2")).isTrue();
        assertThat(CityIdValidator.isCanonical("blr")).isFalse();
        assertThat(CityIdValidator.isCanonical(" BLR")).isFalse();
        assertThat(CityIdValidator.isCanonical("BLR/redis-key")).isFalse();
    }

    @Test
    void parsesAStableDeduplicatedAllowedList() {
        assertThat(FleetCityScope.parseAllowed("BLR, NYC, BLR"))
                .isEqualTo(List.of("BLR", "NYC"));
    }

    @Test
    void usesTheOnlyConfiguredCityForLegacyCreateRequests() {
        assertThat(FleetCityScope.resolve(null, "BLR")).isEqualTo("BLR");
    }

    @Test
    void multipleCitiesRequireAnExplicitAllowedId() {
        assertThatThrownBy(() -> FleetCityScope.resolve(null, "BLR,NYC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");
        assertThatThrownBy(() -> FleetCityScope.resolve("DEL", "BLR,NYC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not an enabled");
        assertThat(FleetCityScope.resolve("NYC", "BLR,NYC")).isEqualTo("NYC");
    }
}
