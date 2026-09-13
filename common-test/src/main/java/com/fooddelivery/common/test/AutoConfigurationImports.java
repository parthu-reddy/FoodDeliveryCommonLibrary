package com.fooddelivery.common.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Asserts that every auto-configuration this service inherits is actually on its classpath.
 *
 * <p>Written 2026-09-12, when CommonLibrary was split into six modules. The
 * {@code AutoConfiguration.imports} file split with the classes it names — {@code OutboxConfiguration}
 * to common-messaging, {@code RedisSupportConfiguration} and {@code IdempotencySweepConfiguration} to
 * common-persistence. A service that takes one module and not the other must not receive an import
 * naming a class it does not have.
 *
 * <p>The failure this prevents happens at **boot**: Spring Boot aborts startup on an
 * auto-configuration import it cannot resolve. No compiler sees it, and no unit test that avoids
 * starting a context sees it either — which is why ten of seventeen services could have shipped it.
 *
 * <p>This needs no Spring context, so a service with no full-context test can still hold the line.
 */
public final class AutoConfigurationImports {

    private static final String RESOURCE =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    private AutoConfigurationImports() {
    }

    /** Every class named by every {@code .imports} file visible to this service, in load order. */
    public static List<String> declared() {
        List<String> names = new ArrayList<>();
        try {
            Enumeration<URL> found = Thread.currentThread().getContextClassLoader().getResources(RESOURCE);
            while (found.hasMoreElements()) {
                URL url = found.nextElement();
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        String trimmed = line.trim();
                        if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                            names.add(trimmed);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + RESOURCE + " from the classpath", e);
        }
        return names;
    }

    /**
     * The assertion itself. Fails naming the unresolvable class, because the Spring Boot failure it
     * stands in for names it too — and a test that only says "context did not start" is a worse
     * version of the thing it replaced.
     */
    public static void assertEveryImportResolves() {
        List<String> declared = declared();

        // Not a vacuous pass: a service on this platform always inherits at least the platform's own
        // auto-configurations, so an empty list means the scan found nothing rather than that
        // nothing is registered.
        assertFalse(declared.isEmpty(),
                "no AutoConfiguration.imports found on the classpath at all — this assertion is "
                        + "checking nothing. Expected at least the ones common-messaging and "
                        + "common-persistence publish.");

        List<String> missing = new ArrayList<>();
        for (String name : declared) {
            try {
                Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                missing.add(name);
            }
        }
        assertTrue(missing.isEmpty(),
                "auto-configuration(s) named on the classpath but not present: " + missing
                        + ". Spring Boot aborts startup on these, so this service would fail to "
                        + "boot. A common module was probably declared without the module that "
                        + "holds the class it registers.");
    }
}
