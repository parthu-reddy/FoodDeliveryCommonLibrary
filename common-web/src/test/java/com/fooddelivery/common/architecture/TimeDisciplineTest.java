package com.fooddelivery.common.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * {@link TimeDisciplineRules} over common-web's own classes. Only this artifact's classes: in a reactor
 * build the sibling modules arrive as target/classes directories, which DoNotIncludeJars would not
 * filter out. RandomDocuments/TimezoneCorrectness_2026-09-25.
 */
@AnalyzeClasses(packages = "com.fooddelivery.common",
        importOptions = {ImportOption.DoNotIncludeTests.class, TimeDisciplineTest.OwnClassesOnly.class})
class TimeDisciplineTest {

    @ArchTest
    static final ArchTests time = ArchTests.in(TimeDisciplineRules.class);

    static final class OwnClassesOnly implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return location.contains("/common-web/");
        }
    }
}
