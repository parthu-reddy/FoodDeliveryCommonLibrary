package com.fooddelivery.common.test;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Test-only setup for controller authorization tests that inject a login with @WithMockUser.
 * Replace only the identity-header filter with a mock and pass requests to the remaining
 * Spring Security filters and method authorization. Do not use this in signed-identity tests:
 * those must exercise the real identity filter and supply correctly signed request headers.
 */
public final class MockIdentityFilterSupport {
    private MockIdentityFilterSupport() {}

    public static void passThrough(Filter mockedIdentityFilter) throws IOException, ServletException {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(mockedIdentityFilter).doFilter(any(), any(), any());
    }
}
