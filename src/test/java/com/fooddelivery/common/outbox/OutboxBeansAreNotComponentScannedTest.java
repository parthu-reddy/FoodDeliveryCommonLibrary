package com.fooddelivery.common.outbox;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Nothing in {@code com.fooddelivery.common} that needs the outbox may be component-scanned.
 *
 * <p>Three services scan this package and have no datasource: BiddingEngine, UserTrackingService and
 * CommunicationIntegration. A stereotype annotation on a class requiring {@link
 * com.fooddelivery.common.outbox.repository.OutboxEventRepository} therefore puts an unsatisfiable
 * bean into their contexts and they fail to start — not just in tests, in production.
 *
 * <p>This has now happened twice. {@code NotificationRouterService} was an unconditional
 * {@code @Service} and broke all three services; the fix was {@code OutboxConfiguration}, an
 * auto-configuration that registers outbox beans only where a {@code DataSource} exists, and its
 * javadoc records the incident. That javadoc did not stop {@code OutboxBacklogMetrics} being added
 * as a bare {@code @Component} in the 2026-09-09 money work, which broke the same two services
 * again — caught by CI, not by any test.
 *
 * <p>So the rule is a test rather than a comment. Beans that need the outbox are registered by
 * {@code OutboxConfiguration}; the classes themselves carry no stereotype.
 */
class OutboxBeansAreNotComponentScannedTest {

    private static final Path SOURCES = Path.of("src/main/java/com/fooddelivery/common");

    /** A stereotype that makes Spring pick the class up during a component scan. */
    private static final Pattern STEREOTYPE =
            Pattern.compile("^\\s*@(Component|Service|Repository|Controller|RestController)\\b",
                    Pattern.MULTILINE);

    private static String stripComments(String java) {
        return java.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "");
    }

    @Test
    void noComponentScannedClassRequiresTheOutboxRepository() {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            files.filter(f -> f.toString().endsWith(".java")).forEach(f -> {
                String src;
                try {
                    src = stripComments(Files.readString(f, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                // The repository interface itself is a @Repository by definition; it is the thing
                // being conditionally created, not a consumer of it.
                if (f.getFileName().toString().equals("OutboxEventRepository.java")) {
                    return;
                }
                if (src.contains("OutboxEventRepository") && STEREOTYPE.matcher(src).find()) {
                    offenders.add(f.toString());
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        assertTrue(offenders.isEmpty(),
                "these classes need an OutboxEventRepository and are component-scanned, so every "
                + "service scanning com.fooddelivery.common without a datasource fails to start: "
                + offenders + ". Register them in OutboxConfiguration instead.");
    }

    /** And the walk must actually be reading sources, or the check above is vacuous. */
    @Test
    void theSourceTreeIsActuallyScanned() {
        assertTrue(Files.isDirectory(SOURCES), "cannot find " + SOURCES.toAbsolutePath());
        try (Stream<Path> files = Files.walk(SOURCES)) {
            long count = files.filter(f -> f.toString().endsWith(".java")).count();
            assertTrue(count > 50, "only " + count + " sources found; the scan is not reaching the tree");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
