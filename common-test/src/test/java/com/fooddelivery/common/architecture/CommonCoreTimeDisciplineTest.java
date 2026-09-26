package com.fooddelivery.common.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * {@link TimeDisciplineRules} over common-core's own classes, run from here: common-test depends on common-core, so common-core can't depend back on it. Only this artifact's classes: in a reactor
 * build the sibling modules arrive as target/classes directories, which DoNotIncludeJars would not
 * filter out. RandomDocuments/TimezoneCorrectness_2026-09-25.
 */
@AnalyzeClasses(packages = "com.fooddelivery.common",
        importOptions = {ImportOption.DoNotIncludeTests.class, CommonCoreTimeDisciplineTest.OwnClassesOnly.class})
class CommonCoreTimeDisciplineTest {

    @ArchTest
    static final ArchTests time = ArchTests.in(TimeDisciplineRules.class);

    static final class OwnClassesOnly implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return location.contains("/common-core/");
        }
    }
}
