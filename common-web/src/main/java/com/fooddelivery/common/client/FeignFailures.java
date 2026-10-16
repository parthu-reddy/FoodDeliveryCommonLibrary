package com.fooddelivery.common.client;

import feign.FeignException;

import java.util.function.Predicate;

/**
 * Reads a failed Feign call's HTTP answer through whatever wraps it. With the Feign circuit breaker on, the Spring Cloud
 * time limiter runs the call on a Future, so a fallback factory and the circuit breaker both receive an
 * {@code ExecutionException} whose cause is the {@link FeignException}: {@code cause instanceof FeignException.NotFound}
 * is never true there (ServiceDecomposition Phase 11, F-8, measured with a WireMock hop test).
 */
public final class FeignFailures {

    private FeignFailures() {
    }

    /** True when the call failed because the callee answered 404, however deep the FeignException sits. */
    public static boolean notFound(Throwable cause) {
        for (Throwable current = cause; current != null; current = current.getCause() == current ? null : current.getCause()) {
            if (current instanceof FeignException.NotFound) {
                return true;
            }
        }
        return false;
    }

    /**
     * For {@code resilience4j.circuitbreaker.configs.default.ignore-exception-predicate}: a 404 is the callee's answer
     * ("no such resource"), not a failure of the callee, so a burst of unknown ids must not open the breaker.
     */
    public static final class NotFoundIsAnAnswer implements Predicate<Throwable> {
        @Override
        public boolean test(Throwable failure) {
            return notFound(failure);
        }
    }
}
