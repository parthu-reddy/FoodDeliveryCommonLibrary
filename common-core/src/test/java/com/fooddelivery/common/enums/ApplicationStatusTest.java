package com.fooddelivery.common.enums;

import com.fooddelivery.common.exception.IllegalStateTransitionException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static com.fooddelivery.common.enums.ApplicationStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationStatusTest {
    // The confirmed product lifecycle, independent of the implementation's branch table.
    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED = Map.of(
            DRAFT, Set.of(SUBMITTED), SUBMITTED, Set.of(IN_REVIEW, REJECTED),
            IN_REVIEW, Set.of(APPROVED, REJECTED), APPROVED, Set.of(SUSPENDED),
            REJECTED, Set.of(SUBMITTED), SUSPENDED, Set.of(APPROVED));

    static Stream<Arguments> pairs() {
        return Stream.of(values()).flatMap(from -> Stream.of(values()).map(to ->
                Arguments.of(from, to, ALLOWED.get(from).contains(to))));
    }

    @ParameterizedTest(name = "{0} -> {1}: allowed={2}")
    @MethodSource("pairs")
    void everyAllowedAndForbiddenPair(ApplicationStatus from, ApplicationStatus to, boolean allowed) {
        if (allowed) assertDoesNotThrow(() -> requireTransition(from, to));
        else assertThrows(IllegalStateTransitionException.class, () -> requireTransition(from, to));
    }

    @Test void missingStatusesCannotBypassTheLifecycle() {
        assertThrows(IllegalStateTransitionException.class, () -> requireTransition(null, DRAFT));
        assertThrows(IllegalStateTransitionException.class, () -> requireTransition(DRAFT, null));
    }
}
