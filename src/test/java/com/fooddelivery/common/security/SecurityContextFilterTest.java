package com.fooddelivery.common.security;

import com.fooddelivery.common.constants.HeaderConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityContextFilterTest {

    private SecurityContextFilter filter;
    private IdentityTokenService identityTokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        identityTokenService = new IdentityTokenService("my-super-secret-key-that-is-long-enough-to-be-secure", new org.springframework.mock.env.MockEnvironment());
        filter = new SecurityContextFilter(identityTokenService);
    }

    @Test
    void shouldAuthenticateValidSignature() throws Exception {
        String userId = "user-123";
        String roles = "ROLE_USER";
        String phone = "1234567890";
        String sessionId = "session-1";
        long issuedAt = 1692698400000L;

        String signature = identityTokenService.sign(userId, roles, phone, sessionId, issuedAt);

        when(request.getHeader(HeaderConstants.HEADER_USER_ID)).thenReturn(userId);
        when(request.getHeader(HeaderConstants.HEADER_USER_ROLES)).thenReturn(roles);
        when(request.getHeader(HeaderConstants.HEADER_USER_PHONE)).thenReturn(phone);
        when(request.getHeader(HeaderConstants.HEADER_SESSION_ID)).thenReturn(sessionId);
        when(request.getHeader(HeaderConstants.HEADER_ISSUED_AT)).thenReturn(String.valueOf(issuedAt));
        when(request.getHeader(HeaderConstants.HEADER_IDENTITY_SIGNATURE)).thenReturn(signature);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userId, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateTamperedRoles() throws Exception {
        String userId = "user-123";
        String originalRoles = "ROLE_USER";
        String tamperedRoles = "ROLE_ADMIN";
        String phone = "1234567890";
        String sessionId = "session-1";
        long issuedAt = 1692698400000L;

        String signature = identityTokenService.sign(userId, originalRoles, phone, sessionId, issuedAt);

        when(request.getHeader(HeaderConstants.HEADER_USER_ID)).thenReturn(userId);
        when(request.getHeader(HeaderConstants.HEADER_USER_ROLES)).thenReturn(tamperedRoles);
        when(request.getHeader(HeaderConstants.HEADER_USER_PHONE)).thenReturn(phone);
        when(request.getHeader(HeaderConstants.HEADER_SESSION_ID)).thenReturn(sessionId);
        when(request.getHeader(HeaderConstants.HEADER_ISSUED_AT)).thenReturn(String.valueOf(issuedAt));
        when(request.getHeader(HeaderConstants.HEADER_IDENTITY_SIGNATURE)).thenReturn(signature);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateMissingSignature() throws Exception {
        when(request.getHeader(HeaderConstants.HEADER_USER_ID)).thenReturn("user-123");
        when(request.getHeader(HeaderConstants.HEADER_USER_ROLES)).thenReturn("ROLE_USER");
        when(request.getHeader(HeaderConstants.HEADER_IDENTITY_SIGNATURE)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
