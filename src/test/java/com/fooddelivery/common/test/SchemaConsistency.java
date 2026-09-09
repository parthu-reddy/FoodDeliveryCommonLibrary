package com.fooddelivery.common.test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares a service's Flyway migrations against its JPA entities, with no database.
 *
 * <p>Every money service runs {@code ddl-auto: validate} in production, so a column the entities
 * disagree with stops the service booting. Nothing in this workspace can catch that at test time:
 * Testcontainers are excluded by project rule, and H2 cannot execute the shipped Postgres schema
 * even in PostgreSQL mode — measured, not assumed:
 *
 * <pre>
 *   CREATE TABLE ledger_accounts ...          Unknown data type: "TIMESTAMPTZ"
 *   CREATE UNIQUE INDEX ... WHERE active = true   Syntax error
 * </pre>
 *
 * <p>So the comparison is made statically instead. This caught three columns in
 * {@code V1__init_ledger.sql} that were {@code TIMESTAMPTZ} under {@code LocalDateTime} fields —
 * LedgerService would not have started in production.
 */
public final class SchemaConsistency {

    private SchemaConsistency() {}

    /**
     * Tables created by common-library's shared migrations, which every service's Flyway run also
     * applies. Without these, outbox_events and idempotency_keys look like missing tables.
     */
    public static final Path COMMON_MIGRATIONS =
            Path.of("../CommonLibrary/src/main/resources/db/migration/common");

    /** table -> column -> declared SQL type, lower-cased. */
    public static Map<String, Map<String, String>> parseMigrations(Path... migrationDirs) {
        Map<String, Map<String, String>> tables = new LinkedHashMap<>();
        List<Path> files = new ArrayList<>();
        for (Path migrationDir : migrationDirs) {
            if (!Files.isDirectory(migrationDir)) continue;
            try (var stream = Files.list(migrationDir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".sql")).sorted().forEach(files::add);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot list " + migrationDir.toAbsolutePath(), e);
            }
        }
        if (files.isEmpty()) {
            throw new IllegalStateException("No migrations found under " + java.util.Arrays.toString(migrationDirs)
                    + " (working directory " + Path.of("").toAbsolutePath() + ")");
        }
        for (Path file : files) {
            String sql;
            try {
                sql = Files.readString(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + file, e);
            }
            // Strip line comments first. Splitting a CREATE TABLE body on ",\n" leaves a comment
            // line glued to the column that follows it, and the column is then skipped -- which
            // reported orders.quote_id and payment_intents.updated_at as missing when both exist.
            sql = sql.replaceAll("(?m)--[^\n]*", "");
            Matcher create = Pattern.compile(
                    "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)\\s*\\((.*?)\\n\\s*\\);",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(sql);
            while (create.find()) {
                tables.put(create.group(1).toLowerCase(), parseColumns(create.group(2)));
            }
            // A later migration can add a column or retype one. Under a forward-only migration
            // policy that is the only way a type ever changes, so both have to be followed or the
            // check reports the original type forever.
            Matcher alterAdd = Pattern.compile(
                    "ALTER\\s+TABLE\\s+(\\w+)\\s+ADD\\s+(?:COLUMN\\s+)?(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)\\s+([A-Za-z]+(?:\\s+WITH\\s+TIME\\s+ZONE)?(?:\\(\\d+(?:,\\s*\\d+)?\\))?)",
                    Pattern.CASE_INSENSITIVE).matcher(sql);
            while (alterAdd.find()) {
                tables.computeIfAbsent(alterAdd.group(1).toLowerCase(), k -> new LinkedHashMap<>())
                      .put(alterAdd.group(2).toLowerCase(), normalise(alterAdd.group(3)));
            }

            // One ALTER TABLE may retype several columns in a comma-separated list, so the table is
            // captured once and each ALTER COLUMN ... TYPE clause applied to it.
            Matcher alterTable = Pattern.compile(
                    "ALTER\\s+TABLE\\s+(?:ONLY\\s+)?(\\w+)([^;]*);", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(sql);
            while (alterTable.find()) {
                String table = alterTable.group(1).toLowerCase();
                Matcher retype = Pattern.compile(
                        "ALTER\\s+(?:COLUMN\\s+)?(\\w+)\\s+(?:SET\\s+DATA\\s+)?TYPE\\s+([A-Za-z]+(?:\\s+WITH\\s+TIME\\s+ZONE)?(?:\\(\\d+(?:,\\s*\\d+)?\\))?)",
                        Pattern.CASE_INSENSITIVE).matcher(alterTable.group(2));
                while (retype.find()) {
                    Map<String, String> columns = tables.get(table);
                    if (columns != null) {
                        columns.put(retype.group(1).toLowerCase(), normalise(retype.group(2)));
                    }
                }
            }
        }
        return tables;
    }

    private static Map<String, String> parseColumns(String body) {
        Map<String, String> columns = new LinkedHashMap<>();
        for (String line : body.split(",\\s*\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String upper = trimmed.toUpperCase();
            if (upper.startsWith("UNIQUE") || upper.startsWith("PRIMARY KEY") || upper.startsWith("CONSTRAINT")
                    || upper.startsWith("FOREIGN KEY") || upper.startsWith("CHECK") || upper.startsWith("--")) {
                continue;
            }
            Matcher col = Pattern.compile(
                    "^(\\w+)\\s+([A-Za-z]+(?:\\s+WITH\\s+TIME\\s+ZONE)?(?:\\(\\d+(?:,\\s*\\d+)?\\))?)").matcher(trimmed);
            if (col.find()) {
                columns.put(col.group(1).toLowerCase(), normalise(col.group(2)));
            }
        }
        return columns;
    }

    private static String normalise(String sqlType) {
        return sqlType.toLowerCase().replaceAll("\\s+", " ");
    }

    public static List<Class<?>> entities(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        List<Class<?>> found = new ArrayList<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            try {
                found.add(Class.forName(bd.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return found;
    }

    public static String tableName(Class<?> entity) {
        Table table = entity.getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) return table.name().toLowerCase();
        return camelToSnake(entity.getSimpleName());
    }

    public static String columnName(Field f) {
        // A @ManyToOne/@OneToOne is stored in its join column, which is named by @JoinColumn or
        // defaults to <field>_id -- not by the field name. Reading the field name here reported
        // every association as a missing column.
        jakarta.persistence.JoinColumn join = f.getAnnotation(jakarta.persistence.JoinColumn.class);
        if (join != null && !join.name().isBlank()) return join.name().toLowerCase();
        if (isAssociation(f)) return camelToSnake(f.getName()) + "_id";
        Column column = f.getAnnotation(Column.class);
        if (column != null && !column.name().isBlank()) return column.name().toLowerCase();
        return camelToSnake(f.getName());
    }

    private static boolean isAssociation(Field f) {
        return f.getAnnotation(jakarta.persistence.ManyToOne.class) != null
                || f.getAnnotation(jakarta.persistence.OneToOne.class) != null;
    }

    public static boolean isMapped(Field f) {
        if (Modifier.isStatic(f.getModifiers()) || f.isAnnotationPresent(Transient.class) || f.isSynthetic()) {
            return false;
        }
        // The inverse side of an association owns no column.
        jakarta.persistence.OneToOne oneToOne = f.getAnnotation(jakarta.persistence.OneToOne.class);
        if (oneToOne != null && !oneToOne.mappedBy().isBlank()) return false;
        return !java.util.Collection.class.isAssignableFrom(f.getType())
                && !Map.class.isAssignableFrom(f.getType())
                && f.getAnnotation(jakarta.persistence.OneToMany.class) == null
                && f.getAnnotation(jakarta.persistence.ManyToMany.class) == null
                && f.getAnnotation(jakarta.persistence.Embedded.class) == null;
    }

    /** A join column's type is not policed: it is a foreign key, and the family check is temporal. */
    public static boolean policesType(Field f) {
        return !isAssociation(f);
    }

    private static String camelToSnake(String s) {
        return s.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    /** The SQL type family a Java type must be stored in, or null when it is not policed. */
    public static String requiredFamily(Class<?> javaType) {
        if (javaType == OffsetDateTime.class || javaType == Instant.class) return "timestamptz";
        if (javaType == LocalDateTime.class) return "timestamp";
        if (javaType == LocalDate.class) return "date";
        return null;
    }

    public static String family(String declaredSqlType) {
        if (declaredSqlType.startsWith("timestamptz") || declaredSqlType.startsWith("timestamp with time zone")) {
            return "timestamptz";
        }
        if (declaredSqlType.startsWith("timestamp")) return "timestamp";
        if (declaredSqlType.startsWith("date")) return "date";
        return declaredSqlType;
    }

    /**
     * @return every disagreement between the entities and the migrations, empty when they match.
     */
    public static List<String> mismatches(Path migrationDir, String basePackage, java.util.Set<String> ignoredTables) {
        Map<String, Map<String, String>> schema = parseMigrations(migrationDir, COMMON_MIGRATIONS);
        List<String> problems = new ArrayList<>();

        for (Class<?> entity : entities(basePackage)) {
            String table = tableName(entity);
            if (ignoredTables.contains(table)) continue;
            Map<String, String> columns = schema.get(table);
            if (columns == null) {
                problems.add(entity.getSimpleName() + " maps table '" + table
                        + "', which no migration creates");
                continue;
            }
            for (Field f : entity.getDeclaredFields()) {
                if (!isMapped(f)) continue;
                String column = columnName(f);
                String declared = columns.get(column);
                if (declared == null) {
                    problems.add(entity.getSimpleName() + "." + f.getName() + " maps column '"
                            + table + "." + column + "', which no migration creates");
                    continue;
                }
                String required = policesType(f) ? requiredFamily(f.getType()) : null;
                if (required != null && !required.equals(family(declared))) {
                    problems.add(entity.getSimpleName() + "." + f.getName() + " is "
                            + f.getType().getSimpleName() + " (Hibernate maps it to " + required
                            + ") but " + table + "." + column + " is declared " + declared);
                }
            }
        }
        return problems;
    }
}
