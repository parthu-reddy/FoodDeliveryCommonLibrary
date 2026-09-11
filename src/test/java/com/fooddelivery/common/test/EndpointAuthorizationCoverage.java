package com.fooddelivery.common.test;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.annotation.security.RolesAllowed;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

/**
 * Reusable scan asserting that every HTTP endpoint carries an authorization rule.
 *
 * <p>Closes gap G-6 in the 2026-08-22 core services review: 26 of 150 endpoints had no
 * authorization at any level, and nothing in the build would have noticed. The findings were
 * fixed, but nothing stopped the next endpoint from arriving bare -- which is the regression
 * this guards.
 *
 * <p>It is deliberately a plain reflective scan rather than a Spring slice: it needs no context,
 * no database and no broker, so it runs in milliseconds and cannot be disabled by an
 * infrastructure problem.
 *
 * <p>Anything genuinely anonymous must be listed explicitly by the calling module. An endpoint
 * becoming public is then a visible, reviewable edit rather than an omission.
 */
public final class EndpointAuthorizationCoverage {

    private EndpointAuthorizationCoverage() {}

    private static final List<String> MAPPING_ANNOTATIONS = List.of(
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping");

    /** One unauthorized endpoint. */
    public record Unprotected(String className, String methodName) {
        @Override
        public String toString() {
            return className + "#" + methodName;
        }
    }

    /**
     * @param basePackage package to scan, e.g. {@code com.fooddelivery.customer}
     * @param allowlist   {@code SimpleClassName#methodName} entries that are intentionally anonymous
     * @return every endpoint with no authorization that is not allowlisted
     */
    public static List<Unprotected> scan(String basePackage, Set<String> allowlist) {
        List<Unprotected> unprotected = new ArrayList<>();
        for (Class<?> type : classesIn(basePackage)) {
            if (!isController(type)) {
                continue;
            }
            boolean classLevel = hasAuthorization(type.getAnnotations());
            for (Method method : type.getDeclaredMethods()) {
                if (!isMapping(method)) {
                    continue;
                }
                if (classLevel || hasAuthorization(method.getAnnotations())) {
                    continue;
                }
                String key = type.getSimpleName() + "#" + method.getName();
                if (allowlist.contains(key)) {
                    continue;
                }
                unprotected.add(new Unprotected(type.getSimpleName(), method.getName()));
            }
        }
        unprotected.sort((a, b) -> a.toString().compareTo(b.toString()));
        return unprotected;
    }


    /** One endpoint whose authorization rule does not mention every resource id it takes. */
    public record UnboundResource(String className, String methodName, List<String> unbound, String expression) {
        @Override
        public String toString() {
            return className + "#" + methodName + " does not bind " + unbound + " (rule: " + expression + ")";
        }
    }

    /**
     * Every {@code @PathVariable} ending in {@code Id} must be named by the authorization rule.
     *
     * <p>{@link #scan} proves a rule is <em>present</em>. It cannot see whether the rule constrains
     * the resource actually being acted on, and that gap shipped a real cross-tenant write: a
     * fulfillment endpoint took {@code /restaurants/{restaurantId}/orders/{orderId}/accept}, proved
     * the caller owned {@code restaurantId}, and never once compared {@code orderId} to it — so any
     * outlet owner could accept, reject, prepare, ready or cancel any other restaurant's order.
     *
     * <p>The rule here is mechanical and catches the shape rather than the instance: two or more
     * resource ids in the path, and a rule that mentions only some of them, is reported. Binding
     * the tenant into the query is the usual fix; where an endpoint genuinely does not need it,
     * the module allowlists it with a reason.
     *
     * @param basePackage package to scan
     * @param allowlist   {@code SimpleClassName#methodName} entries reviewed and deliberately exempt
     */
    public static List<UnboundResource> unboundResourceParameters(String basePackage, Set<String> allowlist) {
        List<UnboundResource> findings = new ArrayList<>();
        for (Class<?> type : classesIn(basePackage)) {
            if (!isController(type)) {
                continue;
            }
            String classRule = authorizationExpression(type.getAnnotations());
            for (Method method : type.getDeclaredMethods()) {
                if (!isMapping(method)) {
                    continue;
                }
                List<String> ids = resourceIdParameters(method);
                if (ids.size() < 2) {
                    continue;
                }
                String key = type.getSimpleName() + "#" + method.getName();
                if (allowlist.contains(key)) {
                    continue;
                }
                String rule = authorizationExpression(method.getAnnotations());
                String effective = (rule == null || rule.isBlank()) ? classRule : rule;
                String expression = effective == null ? "" : effective;
                List<String> unbound = new ArrayList<>();
                for (String id : ids) {
                    if (!expression.contains("#" + id)) {
                        unbound.add(id);
                    }
                }
                if (!unbound.isEmpty()) {
                    findings.add(new UnboundResource(type.getSimpleName(), method.getName(), unbound,
                            expression.isEmpty() ? "<none>" : expression));
                }
            }
        }
        findings.sort((a, b) -> a.toString().compareTo(b.toString()));
        return findings;
    }

    /** Endpoints with two or more resource ids -- the population the binding rule applies to. */
    public static int countMultiResourceEndpoints(String basePackage) {
        int n = 0;
        for (Class<?> type : classesIn(basePackage)) {
            if (!isController(type)) {
                continue;
            }
            for (Method method : type.getDeclaredMethods()) {
                if (isMapping(method) && resourceIdParameters(method).size() >= 2) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * Path variables naming a resource, in declaration order.
     *
     * <p>Prefers the annotation's explicit name; falls back to the parameter name, which is present
     * because these modules compile with {@code -parameters}.
     */
    private static List<String> resourceIdParameters(Method method) {
        List<String> ids = new ArrayList<>();
        java.lang.reflect.Parameter[] params = method.getParameters();
        for (java.lang.reflect.Parameter param : params) {
            org.springframework.web.bind.annotation.PathVariable pv =
                    param.getAnnotation(org.springframework.web.bind.annotation.PathVariable.class);
            if (pv == null) {
                continue;
            }
            String name = !pv.value().isBlank() ? pv.value()
                    : (!pv.name().isBlank() ? pv.name() : param.getName());
            if (name.endsWith("Id")) {
                ids.add(name);
            }
        }
        return ids;
    }

    /** The SpEL of whichever authorization annotation is present, or null. */
    private static String authorizationExpression(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof PreAuthorize preAuthorize) {
                return preAuthorize.value();
            }
            if (annotation instanceof Secured secured) {
                return String.join(",", secured.value());
            }
            if (annotation instanceof RolesAllowed rolesAllowed) {
                return String.join(",", rolesAllowed.value());
            }
        }
        return null;
    }


    /**
     * Binding-allowlist entries that match no endpoint with two or more resource ids.
     *
     * <p>Same reasoning as {@link #staleAllowlistEntries}: an entry that exempts nothing today will
     * silently exempt the next method that takes its name.
     */
    public static List<String> staleBindingAllowlistEntries(String basePackage, Set<String> allowlist) {
        Set<String> live = new java.util.HashSet<>();
        for (Class<?> type : classesIn(basePackage)) {
            if (!isController(type)) {
                continue;
            }
            for (Method method : type.getDeclaredMethods()) {
                if (isMapping(method) && resourceIdParameters(method).size() >= 2) {
                    live.add(type.getSimpleName() + "#" + method.getName());
                }
            }
        }
        List<String> stale = new ArrayList<>();
        for (String entry : allowlist) {
            if (!live.contains(entry)) {
                stale.add(entry);
            }
        }
        stale.sort(String::compareTo);
        return stale;
    }

    /** Total endpoints discovered. A scan that finds none must not pass as "nothing unprotected". */
    public static int countEndpoints(String basePackage) {
        int n = 0;
        for (Class<?> type : classesIn(basePackage)) {
            if (!isController(type)) {
                continue;
            }
            for (Method method : type.getDeclaredMethods()) {
                if (isMapping(method)) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * Allowlist entries that no longer match any endpoint. A stale entry silently exempts nothing
     * today but will exempt the next method that happens to take that name.
     */
    public static List<String> staleAllowlistEntries(String basePackage, Set<String> allowlist) {
        List<String> live = new ArrayList<>();
        for (Class<?> type : classesIn(basePackage)) {
            if (!isController(type)) {
                continue;
            }
            for (Method method : type.getDeclaredMethods()) {
                if (isMapping(method)) {
                    live.add(type.getSimpleName() + "#" + method.getName());
                }
            }
        }
        List<String> stale = new ArrayList<>(allowlist);
        stale.removeAll(live);
        stale.sort(String::compareTo);
        return stale;
    }

    private static boolean isController(Class<?> type) {
        for (Annotation a : type.getAnnotations()) {
            String n = a.annotationType().getName();
            if (n.equals("org.springframework.web.bind.annotation.RestController")
                    || n.equals("org.springframework.stereotype.Controller")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMapping(Method method) {
        for (Annotation a : method.getAnnotations()) {
            if (MAPPING_ANNOTATIONS.contains(a.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAuthorization(Annotation[] annotations) {
        for (Annotation a : annotations) {
            Class<?> t = a.annotationType();
            if (t.equals(PreAuthorize.class) || t.equals(Secured.class) || t.equals(RolesAllowed.class)
                    || t.getName().equals("org.springframework.security.access.prepost.PostAuthorize")) {
                return true;
            }
        }
        return false;
    }

    private static Collection<Class<?>> classesIn(String basePackage) {
        List<Class<?>> found = new ArrayList<>();
        try {
            String path = basePackage.replace('.', '/');
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> roots = loader.getResources(path);
            while (roots.hasMoreElements()) {
                URL url = roots.nextElement();
                if (!"file".equals(url.getProtocol())) {
                    continue;   // only the module's own classes dir; jars are not ours to police
                }
                collect(new File(url.toURI()), basePackage, loader, found);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not scan package " + basePackage, e);
        }
        return found;
    }

    private static void collect(File dir, String pkg, ClassLoader loader, List<Class<?>> out) {
        File[] entries = dir.listFiles();
        if (entries == null) {
            return;
        }
        for (File entry : entries) {
            if (entry.isDirectory()) {
                collect(entry, pkg + "." + entry.getName(), loader, out);
            } else if (entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
                String name = pkg + "." + entry.getName().substring(0, entry.getName().length() - 6);
                try {
                    out.add(Class.forName(name, false, loader));
                } catch (Throwable ignored) {
                    // a class we cannot load is a class we cannot police; the compiler already
                    // rejected anything genuinely broken
                }
            }
        }
    }
}
