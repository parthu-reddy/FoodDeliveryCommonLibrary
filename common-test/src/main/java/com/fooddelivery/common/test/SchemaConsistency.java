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
import java.time.LocalTime;
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
     *
     * <p>Read from the classpath, not from a sibling directory. This was
     * {@code Path.of("../CommonLibrary/src/main/resources/db/migration/common")}, and
     * {@link #parseMigrations} skips a directory that does not exist -- so in a workspace checkout
     * the common tables were found and in CI, where CommonLibrary is a jar rather than a sibling
     * folder, they silently were not. CustomerApplication's schema test consequently passed locally
     * and in the reactor while failing in its own build, reporting outbox_events and
     * idempotency_keys as unmigrated. A check whose verdict depends on the directory layout is not
     * a check; found in the CI run of 2026-09-09.
     *
     * <p>The migrations ship inside the common-library jar at {@code db/migration/common/}, which is
     * on every service's test classpath, so this resolves identically everywhere.
     */
    public static final String COMMON_MIGRATIONS_CLASSPATH = "classpath*:db/migration/common/*.sql";

    /** @deprecated superseded by {@link #COMMON_MIGRATIONS_CLASSPATH}; kept only to fail loudly. */
    private static List<String> commonMigrationSql() {
        try {
            org.springframework.core.io.Resource[] found =
                    new org.springframework.core.io.support.PathMatchingResourcePatternResolver()
                            .getResources(COMMON_MIGRATIONS_CLASSPATH);
            // In Flyway version order, not classpath order: a later migration ALTERs a table an
            // earlier one CREATEs, and applied the other way round the ALTER finds no table and is
            // silently skipped (V20260925100000 retypes V20260811150000's outbox columns).
            java.util.Arrays.sort(found, java.util.Comparator.comparing(
                    (org.springframework.core.io.Resource r) -> String.valueOf(r.getFilename())));
            List<String> sql = new ArrayList<>();
            for (org.springframework.core.io.Resource r : found) {
                try (var in = r.getInputStream()) {
                    sql.add(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
            if (sql.isEmpty()) {
                throw new IllegalStateException(
                        "No shared migrations on the classpath at " + COMMON_MIGRATIONS_CLASSPATH
                        + ". Every service inherits outbox_events and idempotency_keys from them, so "
                        + "without them every entity mapping those tables looks unmigrated.");
            }
            return sql;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + COMMON_MIGRATIONS_CLASSPATH, e);
        }
    }

    /** table -> column -> declared SQL type, lower-cased. */
    public static Map<String, Map<String, String>> parseMigrations(Path... migrationDirs) {
        Map<String, Map<String, String>> tables = new LinkedHashMap<>();
        List<Path> files = new ArrayList<>();
        for (Path migrationDir : migrationDirs) {
            // A missing directory is a caller error, not something to skip quietly: skipping is how
            // the shared migrations went unnoticed-missing in CI. See COMMON_MIGRATIONS_CLASSPATH.
            if (!Files.isDirectory(migrationDir)) {
                throw new IllegalStateException("No migration directory at " + migrationDir.toAbsolutePath());
            }
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
        List<String> documents = new ArrayList<>();
        for (Path file : files) {
            try {
                documents.add(Files.readString(file, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + file, e);
            }
        }
        tables.putAll(parseSql(documents));
        return tables;
    }

    /** Parses CREATE TABLE / ALTER TABLE statements out of already-loaded SQL. */
    public static Map<String, Map<String, String>> parseSql(List<String> documents) {
        Map<String, Map<String, String>> tables = new LinkedHashMap<>();
        for (String sql : documents) {
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
            // check reports the original type forever. One ALTER TABLE can contain several
            // comma-separated ADD COLUMN clauses, so capture the table once and parse every clause
            // in its body rather than only the first ADD after ALTER TABLE.
            Matcher alterTable = Pattern.compile(
                    "ALTER\\s+TABLE\\s+(?:ONLY\\s+)?(\\w+)([^;]*);", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(sql);
            while (alterTable.find()) {
                String table = alterTable.group(1).toLowerCase();
                Matcher add = Pattern.compile(
                        "(?:^|,)\\s*ADD\\s+(?:COLUMN\\s+)?(?:IF\\s+NOT\\s+EXISTS\\s+)?"
                                + "(?!(?:CONSTRAINT|PRIMARY|UNIQUE|FOREIGN|CHECK)\\b)"
                                + "(\\w+)\\s+([A-Za-z]+(?:\\s+WITH\\s+TIME\\s+ZONE)?(?:\\(\\d+(?:,\\s*\\d+)?\\))?)",
                        Pattern.CASE_INSENSITIVE).matcher(alterTable.group(2));
                while (add.find()) {
                    tables.computeIfAbsent(table, k -> new LinkedHashMap<>())
                          .put(add.group(1).toLowerCase(), normalise(add.group(2)));
                }

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

    /** What an INSERT must supply for one column: it is NOT NULL, has no DEFAULT and is not generated. */
    private record Requirement(boolean notNull, boolean hasDefault, boolean generated) {
        boolean required() { return notNull && !hasDefault && !generated; }
    }

    /**
     * table -> columns every INSERT must supply: NOT NULL, no DEFAULT, not generated.
     *
     * <p>Migrations are followed in order, because a later one can add a required column or relax
     * one: ADD COLUMN, DROP COLUMN, RENAME COLUMN, ALTER COLUMN SET/DROP NOT NULL, SET/DROP DEFAULT
     * and DROP TABLE all change the answer.
     */
    public static Map<String, java.util.Set<String>> parseRequiredSql(List<String> documents) {
        Map<String, Map<String, Requirement>> tables = new LinkedHashMap<>();
        for (String sql : documents) {
            sql = sql.replaceAll("(?m)--[^\n]*", "");
            Matcher statement = Pattern.compile(
                    "(CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)\\s*\\((.*?)\\n\\s*\\);)"
                            + "|(ALTER\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?(?:ONLY\\s+)?(\\w+)([^;]*);)"
                            + "|(DROP\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?(\\w+))",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(sql);
            while (statement.find()) {
                if (statement.group(1) != null) {
                    Map<String, Requirement> columns = new LinkedHashMap<>();
                    for (String line : topLevelClauses(statement.group(3))) {
                        String upper = line.toUpperCase();
                        Matcher pk = Pattern.compile("^PRIMARY\\s+KEY\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE).matcher(line);
                        if (pk.find()) {
                            for (String c : pk.group(1).split(",")) {
                                String name = c.trim().toLowerCase();
                                Requirement r = columns.get(name);
                                if (r != null) columns.put(name, new Requirement(true, r.hasDefault(), r.generated()));
                            }
                            continue;
                        }
                        if (upper.startsWith("UNIQUE") || upper.startsWith("CONSTRAINT") || upper.startsWith("FOREIGN KEY")
                                || upper.startsWith("CHECK") || upper.startsWith("EXCLUDE")) {
                            continue;
                        }
                        Matcher col = Pattern.compile("^(\\w+)\\s+(.*)$", Pattern.DOTALL).matcher(line);
                        if (col.find()) columns.put(col.group(1).toLowerCase(), requirement(col.group(2)));
                    }
                    tables.put(statement.group(2).toLowerCase(), columns);
                } else if (statement.group(4) != null) {
                    Map<String, Requirement> columns = tables.computeIfAbsent(statement.group(5).toLowerCase(), k -> new LinkedHashMap<>());
                    for (String clause : topLevelClauses(statement.group(6))) {
                        applyAlterClause(columns, clause.trim());
                    }
                } else {
                    tables.remove(statement.group(8).toLowerCase());
                }
            }
        }
        Map<String, java.util.Set<String>> required = new LinkedHashMap<>();
        tables.forEach((table, columns) -> {
            java.util.Set<String> names = new java.util.LinkedHashSet<>();
            columns.forEach((column, r) -> { if (r.required()) names.add(column); });
            required.put(table, names);
        });
        return required;
    }

    private static Requirement requirement(String definition) {
        String upper = definition.toUpperCase();
        boolean generated = upper.matches("(?s)^\\s*(SMALL|BIG)?SERIAL\\b.*") || upper.contains("GENERATED ");
        return new Requirement(upper.contains("NOT NULL") || upper.contains("PRIMARY KEY"),
                upper.matches("(?s).*\\bDEFAULT\\b.*"), generated);
    }

    private static void applyAlterClause(Map<String, Requirement> columns, String clause) {
        Matcher m;
        if ((m = Pattern.compile("^ADD\\s+(?:COLUMN\\s+)?(?:IF\\s+NOT\\s+EXISTS\\s+)?(?!(?:CONSTRAINT|PRIMARY|UNIQUE|FOREIGN|CHECK|EXCLUDE)\\b)(\\w+)\\s+(.*)$",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(clause)).find()) {
            columns.put(m.group(1).toLowerCase(), requirement(m.group(2)));
        } else if ((m = Pattern.compile("^DROP\\s+(?:COLUMN\\s+)?(?:IF\\s+EXISTS\\s+)?(?!(?:CONSTRAINT)\\b)(\\w+)",
                Pattern.CASE_INSENSITIVE).matcher(clause)).find()) {
            columns.remove(m.group(1).toLowerCase());
        } else if ((m = Pattern.compile("^RENAME\\s+(?:COLUMN\\s+)?(\\w+)\\s+TO\\s+(\\w+)", Pattern.CASE_INSENSITIVE).matcher(clause)).find()) {
            Requirement r = columns.remove(m.group(1).toLowerCase());
            if (r != null) columns.put(m.group(2).toLowerCase(), r);
        } else if ((m = Pattern.compile("^ALTER\\s+(?:COLUMN\\s+)?(\\w+)\\s+(SET|DROP)\\s+(NOT\\s+NULL|DEFAULT)",
                Pattern.CASE_INSENSITIVE).matcher(clause)).find()) {
            String name = m.group(1).toLowerCase();
            Requirement r = columns.getOrDefault(name, new Requirement(false, false, false));
            boolean set = m.group(2).equalsIgnoreCase("SET");
            columns.put(name, m.group(3).toUpperCase().startsWith("NOT")
                    ? new Requirement(set, r.hasDefault(), r.generated())
                    : new Requirement(r.notNull(), set, r.generated()));
        }
    }

    /** Splits on commas outside parentheses, so NUMERIC(14,2) stays one clause. */
    private static List<String> topLevelClauses(String body) {
        List<String> clauses = new ArrayList<>();
        int depth = 0, start = 0;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == ',' && depth == 0) {
                clauses.add(body.substring(start, i).trim());
                start = i + 1;
            }
        }
        String last = body.substring(start).trim();
        if (!last.isEmpty()) clauses.add(last);
        return clauses;
    }

    /**
     * Every column an entity writes on INSERT, including inherited (@MappedSuperclass) and
     * embedded fields. A column marked {@code insertable = false} is not written.
     */
    public static java.util.Set<String> writtenColumns(Class<?> entity) {
        java.util.Set<String> columns = new java.util.LinkedHashSet<>();
        for (Class<?> c = entity; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.isAnnotationPresent(Transient.class) || f.isSynthetic()) continue;
                if (f.isAnnotationPresent(jakarta.persistence.Embedded.class) || f.isAnnotationPresent(jakarta.persistence.EmbeddedId.class)) {
                    columns.addAll(writtenColumns(f.getType()));
                    continue;
                }
                if (!isMapped(f)) continue;
                Column column = f.getAnnotation(Column.class);
                if (column != null && !column.insertable()) continue;
                columns.add(columnName(f));
            }
        }
        return columns;
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

    /** No column satisfies this: the Java type itself is not allowed to hold a stored moment. */
    static final String FORBIDDEN = "nothing -- store moments as java.time.Instant";

    /**
     * The SQL type family a Java type must be stored in, or null when it is not policed.
     *
     * <p>A moment is an {@link Instant} in a {@code timestamptz}; calendar values are {@code date} and
     * {@code time}. LocalDateTime, OffsetDateTime and ZonedDateTime map to {@link #FORBIDDEN}, which no
     * declared column matches: RandomDocuments/TimezoneCorrectness_2026-09-25.
     */
    public static String requiredFamily(Class<?> javaType) {
        if (javaType == Instant.class) return "timestamptz";
        if (javaType == LocalDate.class) return "date";
        if (javaType == LocalTime.class) return "time";
        if (javaType.getName().equals("java.time.LocalDateTime")
                || javaType.getName().equals("java.time.OffsetDateTime")
                || javaType.getName().equals("java.time.ZonedDateTime")
                || javaType.getName().equals("java.util.Date")
                || javaType.getName().equals("java.sql.Timestamp")) {
            return FORBIDDEN;
        }
        return null;
    }

    public static String family(String declaredSqlType) {
        if (declaredSqlType.startsWith("timestamptz") || declaredSqlType.startsWith("timestamp with time zone")) {
            return "timestamptz";
        }
        if (declaredSqlType.startsWith("timestamp")) return "timestamp";
        if (declaredSqlType.startsWith("timetz") || declaredSqlType.startsWith("time with time zone")) return "timetz";
        if (declaredSqlType.startsWith("time")) return "time";
        if (declaredSqlType.startsWith("date")) return "date";
        return declaredSqlType;
    }

    /**
     * The reverse direction: a column the migrations require on every INSERT (NOT NULL, no DEFAULT,
     * not generated) that no entity mapping its table writes. Each such column makes every JPA insert
     * into the table fail on PostgreSQL. Tests that build their schema from the entities (H2
     * ddl-auto) never see it: {@code refund_items.amount} broke every item-level refund this way.
     * Only tables some entity maps are judged; tables written by native SQL are not.
     */
    public static List<String> unwrittenRequiredColumns(Path migrationDir, String basePackage, java.util.Set<String> ignoredTables) {
        List<String> documents = new ArrayList<>();
        try (var stream = Files.list(migrationDir)) {
            for (Path file : stream.filter(p -> p.getFileName().toString().endsWith(".sql")).sorted().toList()) {
                documents.add(Files.readString(file, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + migrationDir.toAbsolutePath(), e);
        }
        List<String> all = new ArrayList<>(commonMigrationSql());
        all.addAll(documents);
        Map<String, java.util.Set<String>> required = parseRequiredSql(all);
        Map<String, java.util.Set<String>> written = new LinkedHashMap<>();
        for (Class<?> entity : entities(basePackage)) {
            written.computeIfAbsent(tableName(entity), k -> new java.util.LinkedHashSet<>()).addAll(writtenColumns(entity));
        }
        List<String> problems = new ArrayList<>();
        written.forEach((table, columns) -> {
            if (ignoredTables.contains(table)) return;
            for (String column : required.getOrDefault(table, java.util.Set.of())) {
                if (!columns.contains(column)) {
                    problems.add(table + "." + column + " is NOT NULL with no default, but no entity mapping '"
                            + table + "' writes it: every insert into " + table + " fails");
                }
            }
        });
        return problems;
    }

    /**
     * @return every disagreement between the entities and the migrations, empty when they match.
     */
    public static List<String> mismatches(Path migrationDir, String basePackage, java.util.Set<String> ignoredTables) {
        // The service's own migrations from its source tree, the shared ones from the classpath --
        // the latter are a jar in CI and a sibling directory in a workspace checkout.
        Map<String, Map<String, String>> schema = parseMigrations(migrationDir);
        schema.putAll(parseSql(commonMigrationSql()));
        List<String> problems = new ArrayList<>();
        problems.addAll(unwrittenRequiredColumns(migrationDir, basePackage, ignoredTables));

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
                            + f.getType().getSimpleName() + " (it belongs in " + required
                            + ") but " + table + "." + column + " is declared " + declared);
                }
            }
        }
        return problems;
    }
}
