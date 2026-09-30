package com.fooddelivery.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@lombok.extern.slf4j.Slf4j
@org.springframework.context.annotation.Profile("!contract-test")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.redis.enabled", matchIfMissing = true)
@lombok.RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {
private final RedisOperations<String, String> redisTemplate;
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final Duration IDEMPOTENCY_EXPIRATION = Duration.ofHours(24);
    private List<BypassRoute> bypassRoutes = Collections.emptyList();
    @org.springframework.beans.factory.annotation.Value("${spring.application.name:unknown-service}")
    private String appName;

    /**
     * Endpoints with a durable, transactionally stored idempotency operation may opt out of this
     * Redis-only duplicate guard. Each configured value is a {@code METHOD:/path} pair; a single
     * {@code *} may stand for one non-empty path segment, which is useful for an opaque resource
     * identifier. It never spans slashes or query parameters.
     * This prevents the generic filter from returning a 409 before that endpoint can safely
     * replay a completed operation.
     */
    @org.springframework.beans.factory.annotation.Value("${idempotency.filter.bypass-routes:}")
    void setBypassRoutes(String configuredRoutes) {
        if (configuredRoutes == null || configuredRoutes.isBlank()) {
            bypassRoutes = Collections.emptyList();
            return;
        }
        // Parse segment-by-segment rather than converting configuration to a regex. A bypass can
        // only match the configured HTTP method and the configured number of URI segments.
        bypassRoutes = Arrays.stream(configuredRoutes.split(","))
                .map(String::trim)
                .map(BypassRoute::parse)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (bypassRoutes.isEmpty()) {
            return false;
        }
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (method == null || path == null) {
            return false;
        }
        return bypassRoutes.stream().anyMatch(route -> route.matches(method, path));
    }

    private record BypassRoute(String method, List<String> pathSegments) {
        private static java.util.Optional<BypassRoute> parse(String configuredRoute) {
            int separator = configuredRoute.indexOf(':');
            if (separator <= 0 || separator == configuredRoute.length() - 1) {
                return java.util.Optional.empty();
            }
            String method = configuredRoute.substring(0, separator).trim();
            String path = configuredRoute.substring(separator + 1).trim();
            if (method.isEmpty() || !path.startsWith("/") || path.endsWith("/")) {
                return java.util.Optional.empty();
            }
            List<String> segments = Arrays.stream(path.substring(1).split("/", -1)).toList();
            if (segments.isEmpty() || segments.stream().anyMatch(segment -> segment.isEmpty()
                    || (!"*".equals(segment) && segment.contains("*")))) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(new BypassRoute(method.toUpperCase(Locale.ROOT), segments));
        }

        private boolean matches(String requestMethod, String requestPath) {
            if (!method.equals(requestMethod.toUpperCase(Locale.ROOT))
                    || !requestPath.startsWith("/") || requestPath.endsWith("/")) {
                return false;
            }
            List<String> requestSegments = Arrays.stream(requestPath.substring(1).split("/", -1)).toList();
            if (requestSegments.size() != pathSegments.size()
                    || requestSegments.stream().anyMatch(String::isEmpty)) {
                return false;
            }
            for (int index = 0; index < pathSegments.size(); index++) {
                String configuredSegment = pathSegments.get(index);
                if (!"*".equals(configuredSegment)
                        && !configuredSegment.equals(requestSegments.get(index))) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            String cacheKey = "idempotency:" + appName + ":" + idempotencyKey;
            // Try to acquire lock for this key
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(cacheKey, "PROCESSING", IDEMPOTENCY_EXPIRATION);
            if (Boolean.FALSE.equals(acquired)) {
                log.warn("Duplicate request detected for idempotency key: {}", idempotencyKey);
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("Duplicate request detected.");
                return;
            }
            try {
                filterChain.doFilter(request, response);
            } catch (Exception ex) {
                // Release lock on unhandled exception
                redisTemplate.delete(cacheKey);
                log.info("Released idempotency lock {} due to exception", idempotencyKey);
                throw ex;
            } finally {
                // Release lock if it's a 5xx error
                if (response.getStatus() >= 500) {
                    redisTemplate.delete(cacheKey);
                    log.info("Released idempotency lock {} due to 5xx response", idempotencyKey);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

}
