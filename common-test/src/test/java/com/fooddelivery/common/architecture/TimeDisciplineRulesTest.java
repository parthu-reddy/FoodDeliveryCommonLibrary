package com.fooddelivery.common.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Each time rule must fire on a class that breaks it and stay quiet on one that follows the rules.
 * A rule that never fires can't guard anything, and a rule that fires on correct code gets
 * switched off.
 */
class TimeDisciplineRulesTest {

    // ---- a class that follows every rule ------------------------------------------------------
    @SuppressWarnings("unused")
    static class Compliant {
        private Instant placedAt;
        private LocalDate businessDay;     // calendar value; the zone travels with it
        private LocalTime opensAt;
        private ZoneId zone;

        Instant closesAt(Instant from) {
            return from.plusSeconds(60);
        }

        Instant startOf(LocalDate day, ZoneId z) {
            return day.atStartOfDay(z).toInstant(); // touches ZonedDateTime only in passing
        }

        LocalDate today(Clock clock) {
            return LocalDate.now(clock);
        }

        LocalTime wallClock(ZoneId z) {
            return LocalTime.now(z);
        }

        @Scheduled(cron = "0 0 2 * * *", zone = "${platform.accounting-zone}")
        void nightly() {}

        @Scheduled(fixedDelay = 5000)
        void poll() {}
    }

    interface CompliantQueries {
        @Query("SELECT SUM(e.amount) FROM LedgerEntry e WHERE e.createdAt >= :from AND e.createdAt < :to")
        Object window();

        @Query(value = "SELECT * FROM outlets o WHERE (CURRENT_TIMESTAMP AT TIME ZONE o.time_zone)::time >= t.opening_time",
                nativeQuery = true)
        Object openNow();
    }

    // ---- one violation per class ---------------------------------------------------------------
    @SuppressWarnings("unused")
    static class StoresOffsetDateTime { private OffsetDateTime at; }

    @SuppressWarnings("unused")
    static class StoresLegacyDate { private Date at; }

    static class ReturnsZonedDateTime { ZonedDateTime at() { return null; } }

    static class AcceptsLegacyTimestamp { void at(Instant ok, java.sql.Timestamp notOk) {} }

    static class UsesLocalDateTime { Object f() { return LocalDateTime.of(2026, 1, 1, 0, 0).toString(); } }

    static class CallsLocalDateNow { Object f() { return LocalDate.now(); } }

    static class CallsSystemDefault { Object f() { return ZoneId.systemDefault(); } }

    static class CallsTimeZoneDefault { Object f() { return TimeZone.getDefault(); } }

    static class ConstructsDate { Object f() { return new Date(); } }

    static class StartsDayInJvmZone { Object f(LocalDate d) { return d.atStartOfDay(); } }

    static class CallsSystemDefaultClock { Object f() { return Clock.systemDefaultZone(); } }

    static class CronWithoutZone { @Scheduled(cron = "0 0 2 * * *") void nightly() {} }

    interface CastsToDate {
        @Query("SELECT 1 FROM LedgerEntry e WHERE CAST(e.createdAt AS date) = :date")
        Object byDay();
    }

    interface LiteralZone {
        @Query(value = "SELECT (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Kolkata')::time", nativeQuery = true)
        Object now();
    }

    interface CurrentDate {
        @Query(value = "SELECT * FROM campaign_performance WHERE date >= CURRENT_DATE - 7", nativeQuery = true)
        Object recent();
    }

    // ---------------------------------------------------------------------------------------------

    private static JavaClasses classes(Class<?>... c) {
        return new ClassFileImporter().importClasses(c);
    }

    private static boolean fires(ArchRule rule, Class<?> fixture) {
        return rule.evaluate(classes(fixture)).hasViolation();
    }

    @Test
    void everyRulePassesCompliantCode() throws Exception {
        for (ArchRule rule : allRules()) {
            assertThat(fires(rule, Compliant.class)).as(rule.getDescription()).isFalse();
            assertThat(fires(rule, CompliantQueries.class)).as(rule.getDescription()).isFalse();
        }
    }

    @Test
    void storedMoments() {
        assertThat(fires(TimeDisciplineRules.moments_are_not_stored_as_zone_ambiguous_types, StoresOffsetDateTime.class)).isTrue();
        assertThat(fires(TimeDisciplineRules.moments_are_not_stored_as_zone_ambiguous_types, StoresLegacyDate.class)).isTrue();
    }

    @Test
    void returnedAndAcceptedMoments() {
        assertThat(fires(TimeDisciplineRules.moments_are_not_returned_as_zone_ambiguous_types, ReturnsZonedDateTime.class)).isTrue();
        assertThat(fires(TimeDisciplineRules.moments_are_not_accepted_as_zone_ambiguous_types, AcceptsLegacyTimestamp.class)).isTrue();
    }

    @Test
    void localDateTimeAnywhere() {
        assertThat(fires(TimeDisciplineRules.local_date_time_is_not_used_at_all, UsesLocalDateTime.class)).isTrue();
    }

    @Test
    void everyAmbientZoneCall() {
        for (Class<?> fixture : List.of(CallsLocalDateNow.class, CallsSystemDefault.class, CallsTimeZoneDefault.class,
                ConstructsDate.class, StartsDayInJvmZone.class, CallsSystemDefaultClock.class)) {
            assertThat(fires(TimeDisciplineRules.nothing_reads_the_jvm_default_zone, fixture)).as(fixture.getSimpleName()).isTrue();
        }
    }

    @Test
    void cronWithoutZone() {
        assertThat(fires(TimeDisciplineRules.cron_schedules_name_their_zone, CronWithoutZone.class)).isTrue();
    }

    @Test
    void sessionZoneSql() {
        for (Class<?> fixture : List.of(CastsToDate.class, LiteralZone.class, CurrentDate.class)) {
            assertThat(fires(TimeDisciplineRules.queries_do_not_depend_on_the_session_zone, fixture)).as(fixture.getSimpleName()).isTrue();
        }
    }

    private static List<ArchRule> allRules() throws IllegalAccessException {
        List<ArchRule> rules = new ArrayList<>();
        for (Field f : TimeDisciplineRules.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && ArchRule.class.isAssignableFrom(f.getType())) {
                rules.add((ArchRule) f.get(null));
            }
        }
        assertThat(rules).as("rules discovered").hasSize(7);
        return rules;
    }
}
