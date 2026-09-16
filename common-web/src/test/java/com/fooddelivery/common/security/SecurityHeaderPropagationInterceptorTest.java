package com.fooddelivery.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityHeaderPropagationInterceptorTest {

    private static final String SECRET = "rest-identity-round-trip-test-key";

    private final IdentityTokenService tokens = new IdentityTokenService(SECRET, new MockEnvironment());

    private SecurityHeaderPropagationInterceptor interceptor() {
        SecurityHeaderPropagationInterceptor interceptor = new SecurityHeaderPropagationInterceptor(tokens);
        ReflectionTestUtils.setField(interceptor, "applicationName", "customer-service");
        return interceptor;
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void forwardsTheExactGatewayVerifiedTuple() throws Exception {
        long issuedAt = System.currentTimeMillis();
        String userId = "11111111-1111-1111-1111-111111111111";
        String roles = "CUSTOMER,LOYALTY_MEMBER";
        String phone = "+919999999999";
        String sessionId = "session-123";
        String signature = tokens.sign(userId, roles, phone, sessionId, issuedAt);
        var authentication = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        authentication.setDetails(Map.of(
                "roles", roles,
                "phone", phone,
                "sessionId", sessionId,
                "signature", signature,
                "issuedAt", String.valueOf(issuedAt)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MockClientHttpRequest request = new MockClientHttpRequest();
        interceptor().intercept(request, new byte[0],
                (outboundRequest, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        assertEquals(userId, request.getHeaders().getFirst("X-User-Id"));
        assertEquals(roles, request.getHeaders().getFirst("X-User-Roles"));
        assertEquals(phone, request.getHeaders().getFirst("X-User-Phone"));
        assertEquals(sessionId, request.getHeaders().getFirst("X-Session-Id"));
        assertEquals(signature, request.getHeaders().getFirst("X-Identity-Signature"));
        assertTrue(tokens.verify(signature, userId, roles, phone, sessionId, issuedAt));
    }

    @Test
    void backgroundCallReplacesStaleHeadersWithAServiceIdentity() throws Exception {
        MockClientHttpRequest request = new MockClientHttpRequest();
        request.getHeaders().set("X-User-Id", "stale-user");
        request.getHeaders().set("X-User-Phone", "+910000000000");
        request.getHeaders().set("X-Session-Id", "stale-session");

        interceptor().intercept(request, new byte[0],
                (outboundRequest, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        String issuedAt = request.getHeaders().getFirst("X-Issued-At");
        String signature = request.getHeaders().getFirst("X-Identity-Signature");
        assertEquals("customer-service", request.getHeaders().getFirst("X-User-Id"));
        assertEquals("SERVICE", request.getHeaders().getFirst("X-User-Roles"));
        assertNull(request.getHeaders().getFirst("X-User-Phone"));
        assertNull(request.getHeaders().getFirst("X-Session-Id"));
        assertTrue(tokens.verify(signature, "customer-service", "SERVICE", null, null,
                Long.parseLong(issuedAt)));
    }
}
