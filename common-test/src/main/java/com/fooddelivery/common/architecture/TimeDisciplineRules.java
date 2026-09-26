package com.fooddelivery.common.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noCodeUnits;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * Time rules, enforced on bytecode in every module:
 * {@code @ArchTest static final ArchTests time = ArchTests.in(TimeDisciplineRules.class);}
 *
 * <p>The rules, and why: RandomDocuments/TimezoneCorrectness_2026-09-25/README.md. In short, a
 * moment is an {@code Instant}, nothing reads the JVM's default zone, and calendar logic names its
 * zone. {@code validate_time.py} checks the same things at source level, plus SQL, schema, config and
 * the UI, which bytecode cannot see.
 *
 * <p>Every rule allows an empty subject: a module with no fields, or no @Scheduled methods, is
 * compliant, not misconfigured.
 *
 * <p>Zoned types ({@code ZonedDateTime}, {@code OffsetDateTime}) are banned from the API surface
 * (fields, parameters, return types) but not from the body of a method: {@code
 * date.atStartOfDay(zone).toInstant()} necessarily touches a {@code ZonedDateTime} in passing.
 */
public final class TimeDisciplineRules {

    private TimeDisciplineRules() {}

    /** The zone every surefire argLine sets: FoodDeliveryParent's, and each module's override. */
    public static final String HOSTILE_TEST_ZONE = "Pacific/Chatham";

    /**
     * Not a rule about the classes: a check that this test JVM really is in the hostile zone. It rides
     * along with the rules, so every module that runs them proves its own argLine took effect. A module
     * whose surefire config lost the flag would otherwise test in the developer's zone, where code that
     * leans on the default zone still passes. Chatham (+12:45/+13:45, DST) matches nobody's laptop.
     */
    @ArchTest
    public static void tests_run_in_the_hostile_zone(JavaClasses ignored) {
        String actual = TimeZone.getDefault().getID();
        if (!HOSTILE_TEST_ZONE.equals(actual)) {
            throw new AssertionError("This test JVM runs in " + actual + ", not " + HOSTILE_TEST_ZONE
                    + ". Add -Duser.timezone=" + HOSTILE_TEST_ZONE + " to this module's surefire <argLine>"
                    + " (RandomDocuments/TimezoneCorrectness_2026-09-25, Phase 7).");
        }
    }

    private static final Set<String> NOT_A_MOMENT_TYPE = Set.of(
            "java.time.LocalDateTime", "java.time.OffsetDateTime", "java.time.ZonedDateTime",
            "java.util.Date", "java.util.Calendar", "java.util.GregorianCalendar",
            "java.sql.Timestamp", "java.sql.Date", "java.sql.Time");

    private static final DescribedPredicate<JavaClass> BANNED_TIME_TYPE = DescribedPredicate.describe(
            "a zone-ambiguous or legacy time type (use Instant; LocalDate/LocalTime only with a named zone)",
            c -> NOT_A_MOMENT_TYPE.contains(c.getName()));

    @ArchTest
    public static final ArchRule moments_are_not_stored_as_zone_ambiguous_types =
            noFields().should().haveRawType(BANNED_TIME_TYPE)
                    .because("a stored moment must be an Instant; see TimezoneCorrectness_2026-09-25")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule moments_are_not_returned_as_zone_ambiguous_types =
            noMethods().should().haveRawReturnType(BANNED_TIME_TYPE)
                    .because("a returned moment must be an Instant")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule moments_are_not_accepted_as_zone_ambiguous_types =
            noCodeUnits().should().haveRawParameterTypes(DescribedPredicate.describe(
                            "any parameter of " + BANNED_TIME_TYPE.getDescription(),
                            (List<JavaClass> params) -> params.stream().anyMatch(BANNED_TIME_TYPE)))
                    .because("an accepted moment must be an Instant")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule local_date_time_is_not_used_at_all =
            noClasses().should().dependOnClassesThat().haveFullyQualifiedName("java.time.LocalDateTime")
                    .because("a LocalDateTime is a wall-clock reading with no zone; it cannot identify a moment")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule nothing_reads_the_jvm_default_zone =
            // This class is exempt, by exact name only: its sentinel must read the zone to check it. In a
            // reactor build common-test arrives as target/classes, which DoNotIncludeJars doesn't filter.
            noClasses().that().doNotHaveFullyQualifiedName(TimeDisciplineRules.class.getName())
                    .should().callMethod(LocalDate.class, "now")
                    .orShould().callMethod(LocalTime.class, "now")
                    .orShould().callMethod(ZonedDateTime.class, "now")
                    .orShould().callMethod(OffsetDateTime.class, "now")
                    .orShould().callMethod(Year.class, "now")
                    .orShould().callMethod(YearMonth.class, "now")
                    .orShould().callMethod(MonthDay.class, "now")
                    .orShould().callMethod(ZoneId.class, "systemDefault")
                    .orShould().callMethod(TimeZone.class, "getDefault")
                    .orShould().callMethod(Clock.class, "systemDefaultZone")
                    .orShould().callMethod(Calendar.class, "getInstance")
                    .orShould().callMethod(LocalDate.class, "atStartOfDay")
                    .orShould().callConstructor(Date.class)
                    .because("the JVM's zone is configuration, not business logic: pass a Clock or a named ZoneId")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule cron_schedules_name_their_zone =
            methods().that().areAnnotatedWith("org.springframework.scheduling.annotation.Scheduled")
                    .should(new ArchCondition<JavaMethod>("give a zone to any cron expression") {
                        @Override
                        public void check(JavaMethod method, ConditionEvents events) {
                            Optional<JavaAnnotation<JavaMethod>> scheduled = method.getAnnotations().stream()
                                    .filter(a -> a.getRawType().getName().equals("org.springframework.scheduling.annotation.Scheduled"))
                                    .findFirst();
                            if (scheduled.isEmpty()) {
                                return;
                            }
                            String cron = scheduled.get().get("cron").map(Object::toString).orElse("");
                            String zone = scheduled.get().get("zone").map(Object::toString).orElse("");
                            if (!cron.isBlank() && zone.isBlank()) {
                                events.add(SimpleConditionEvent.violated(method,
                                        method.getFullName() + " runs cron '" + cron + "' in the JVM's zone"));
                            }
                        }
                    })
                    .allowEmptyShould(true);

    private static final Pattern SESSION_ZONE_SQL = Pattern.compile(
            "(?i)\\bcurrent_date\\b|\\bcurrent_time\\b|\\blocaltimestamp\\b|\\blocaltime\\b|::\\s*date\\b"
            + "|\\bcast\\s*\\([^()]*?\\bas\\s+date\\s*\\)|\\bdate_trunc\\s*\\(\\s*'[a-z]+'\\s*,\\s*[^,()]+\\)"
            + "|\\bdate_part\\s*\\(|\\bextract\\s*\\(\\s*(hour|day|dow|isodow|doy|week|month|year)\\b"
            + "|at\\s+time\\s+zone\\s+'");

    @ArchTest
    public static final ArchRule queries_do_not_depend_on_the_session_zone =
            methods().that().areAnnotatedWith("org.springframework.data.jpa.repository.Query")
                    .should(new ArchCondition<JavaMethod>("not depend on the database session's time zone") {
                        @Override
                        public void check(JavaMethod method, ConditionEvents events) {
                            method.getAnnotations().stream()
                                    .filter(a -> a.getRawType().getName().equals("org.springframework.data.jpa.repository.Query"))
                                    .forEach(a -> {
                                        String sql = a.get("value").map(Object::toString).orElse("");
                                        var m = SESSION_ZONE_SQL.matcher(sql);
                                        if (m.find()) {
                                            events.add(SimpleConditionEvent.violated(method, method.getFullName()
                                                    + " uses '" + m.group() + "': query a [from, to) window of instants,"
                                                    + " or AT TIME ZONE a zone taken from data"));
                                        }
                                    });
                        }
                    })
                    .allowEmptyShould(true);
}
