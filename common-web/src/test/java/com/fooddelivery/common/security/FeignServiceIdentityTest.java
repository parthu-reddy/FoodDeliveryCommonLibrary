package com.fooddelivery.common.security;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Background work has no principal, so before this existed a {@code @Scheduled} job's Feign call
 * arrived with no identity headers at all -- which is why internal endpoints could not require even
 * {@code isAuthenticated()} without 403-ing ad reconciliation and wallet backfill.
 *
 * <p>What matters is not that headers are set, but that the receiving side accepts them: these tests
 * verify the minted signature through the same {@link IdentityTokenService} that
 * {@link SecurityContextFilter} uses.
 */
class FeignServiceIdentityTest {

    private static final String SECRET = "service-identity-round-trip-test-key";

    private final IdentityTokenService tokens = new IdentityTokenService(SECRET, new MockEnvironment());

    private FeignSecurityInterceptor interceptor() {
        FeignSecurityInterceptor i = new FeignSecurityInterceptor(tokens);
        ReflectionTestUtils.setField(i, "applicationName", "bidding-engine");
        return i;
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    private static String header(RequestTemplate t, String name) {
        var values = t.headers().get(name);
        return values == null || values.isEmpty() ? null : values.iterator().next();
    }

    @Test
    void backgroundCallMintsAServiceIdentityTheReceiverAccepts() {
        RequestTemplate t = new RequestTemplate();
        interceptor().apply(t);   // no SecurityContext: this is the scheduled-job path

        String userId    = header(t, "X-User-Id");
        String roles     = header(t, "X-User-Roles");
        String signature = header(t, "X-Identity-Signature");
        String issuedAt  = header(t, "X-Issued-At");

        assertEquals("bidding-engine", userId, "the service names itself, so logs attribute the call");
        assertEquals("SERVICE", roles);
        assertNotNull(signature);
        assertNotNull(issuedAt);

        // The point of the whole mechanism: SecurityContextFilter must accept this.
        assertTrue(tokens.verify(signature, userId, roles, null, null, Long.parseLong(issuedAt)),
                "the minted signature must verify through the same path SecurityContextFilter uses");
    }

    @Test
    void theMintedRolesBecomeRoleServiceOnTheReceiver() {
        // SecurityContextFilter uppercases and prefixes ROLE_; hasRole('SERVICE') matches ROLE_SERVICE.
        RequestTemplate t = new RequestTemplate();
        interceptor().apply(t);
        String roles = header(t, "X-User-Roles");
        String authority = roles.toUpperCase().startsWith("ROLE_") ? roles.toUpperCase() : "ROLE_" + roles.toUpperCase();
        assertEquals("ROLE_SERVICE", authority);
    }

    @Test
    void aTamperedServiceIdentityIsRejected() {
        RequestTemplate t = new RequestTemplate();
        interceptor().apply(t);
        // Claim ADMIN while presenting the signature minted for SERVICE.
        assertFalse(tokens.verify(header(t, "X-Identity-Signature"), header(t, "X-User-Id"),
                        "ADMIN", null, null, Long.parseLong(header(t, "X-Issued-At"))),
                "escalating the roles header must invalidate the signature");
    }

    @Test
    void aRealCallerIsForwardedAndNotReplacedByAServiceIdentity() {
        long issuedAt = System.currentTimeMillis();
        String userId = "11111111-1111-1111-1111-111111111111";
        String roles = "CUSTOMER,LOYALTY_MEMBER";
        String phone = "+919999999999";
        String sessionId = "session-123";
        String signature = tokens.sign(userId, roles, phone, sessionId, issuedAt);
        var auth = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(
                        new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                        new SimpleGrantedAuthority("ROLE_LOYALTY_MEMBER")));
        auth.setDetails(Map.of(
                "roles", roles,
                "phone", phone,
                "sessionId", sessionId,
                "signature", signature,
                "issuedAt", String.valueOf(issuedAt)));
        SecurityContextHolder.getContext().setAuthentication(auth);

        RequestTemplate t = new RequestTemplate();
        interceptor().apply(t);

        assertEquals(userId, header(t, "X-User-Id"));
        assertEquals(roles, header(t, "X-User-Roles"), "the exact signed role string must be retained");
        assertEquals(phone, header(t, "X-User-Phone"));
        assertEquals(sessionId, header(t, "X-Session-Id"));
        assertEquals(signature, header(t, "X-Identity-Signature"),
                "the gateway's original signature is forwarded, not re-minted");
        assertTrue(tokens.verify(signature, userId, roles, phone, sessionId, issuedAt),
                "the forwarded tuple must remain valid at the receiving service");
    }

    @Test
    void incompleteCallerIdentityIsReplacedWithoutLeavingMixedHeaders() {
        var auth = new UsernamePasswordAuthenticationToken(
                "11111111-1111-1111-1111-111111111111", null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        RequestTemplate t = new RequestTemplate();
        t.header("X-User-Phone", "+910000000000");
        t.header("X-Session-Id", "stale-session");
        interceptor().apply(t);

        assertEquals("bidding-engine", header(t, "X-User-Id"));
        assertEquals("SERVICE", header(t, "X-User-Roles"));
        assertNull(header(t, "X-User-Phone"));
        assertNull(header(t, "X-Session-Id"));
        assertTrue(tokens.verify(
                header(t, "X-Identity-Signature"),
                header(t, "X-User-Id"),
                header(t, "X-User-Roles"),
                null,
                null,
                Long.parseLong(header(t, "X-Issued-At"))));
    }

    @Test
    void aReusedFeignWorkerForwardsEachRequestsVerifiedCaller() throws Exception {
        var worker = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            // Model the stale principal observed on the deployed circuit-breaker worker.
            var stale = signedRequest("seeded-rider", "DELIVERY");
            new SecurityContextFilter(tokens).doFilterInternal(stale,
                    new org.springframework.mock.web.MockHttpServletResponse(), (request, response) -> {});
            var staleAuthentication = SecurityContextHolder.getContext().getAuthentication();
            worker.submit(() -> SecurityContextHolder.getContext().setAuthentication(staleAuthentication)).get();

            for (String userId : List.of("fresh-rider", "another-rider")) {
                var request = signedRequest(userId, "DELIVERY");
                new SecurityContextFilter(tokens).doFilterInternal(request,
                        new org.springframework.mock.web.MockHttpServletResponse(), (req, res) -> {});
                var attributes = new org.springframework.web.context.request.ServletRequestAttributes(request);
                var outgoing = worker.submit(() -> {
                    // OpenFeign 4.1.2 copies these attributes for each circuit-breaker call.
                    org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(attributes);
                    try {
                        RequestTemplate template = new RequestTemplate();
                        interceptor().apply(template);
                        return template;
                    } finally {
                        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
                    }
                }).get(5, java.util.concurrent.TimeUnit.SECONDS);
                assertEquals(userId, header(outgoing, "X-User-Id"));
                assertEquals(request.getHeader("X-Session-Id"), header(outgoing, "X-Session-Id"));
                assertEquals(request.getHeader("X-Identity-Signature"), header(outgoing, "X-Identity-Signature"));
                assertTrue(tokens.verify(header(outgoing, "X-Identity-Signature"), userId, "DELIVERY",
                        header(outgoing, "X-User-Phone"), header(outgoing, "X-Session-Id"),
                        Long.parseLong(header(outgoing, "X-Issued-At"))));
            }
        } finally {
            worker.shutdownNow();
        }
    }

    @Test
    void unsignedCurrentRequestCannotUseAnotherThreadsAuthenticatedCaller() throws Exception {
        var previous = signedRequest("previous-admin", "ADMIN");
        new SecurityContextFilter(tokens).doFilterInternal(previous,
                new org.springframework.mock.web.MockHttpServletResponse(), (req, res) -> {});
        var unsigned = new org.springframework.mock.web.MockHttpServletRequest();
        unsigned.addHeader("X-User-Id", "forged-admin");
        unsigned.addHeader("X-User-Roles", "ADMIN");
        new SecurityContextFilter(tokens).doFilterInternal(unsigned,
                new org.springframework.mock.web.MockHttpServletResponse(), (req, res) -> {});
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(unsigned));
        RequestTemplate template = new RequestTemplate();
        interceptor().apply(template);
        assertEquals("bidding-engine", header(template, "X-User-Id"));
        assertEquals("SERVICE", header(template, "X-User-Roles"));
        assertNull(header(template, "X-Session-Id"));
    }

    @Test
    void backgroundThreadsDoNotInheritTheUserThatCreatedThem() throws Exception {
        new CommonSecurityConfig(null).init();
        var request = signedRequest("first-rider", "DELIVERY");
        new SecurityContextFilter(tokens).doFilterInternal(request,
                new org.springframework.mock.web.MockHttpServletResponse(), (req, res) -> {});
        var worker = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            RequestTemplate template = worker.submit(() -> {
                RequestTemplate outgoing = new RequestTemplate();
                interceptor().apply(outgoing);
                return outgoing;
            }).get(5, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals("bidding-engine", header(template, "X-User-Id"));
            assertEquals("SERVICE", header(template, "X-User-Roles"));
        } finally {
            worker.shutdownNow();
            SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
        }
    }

    private org.springframework.mock.web.MockHttpServletRequest signedRequest(String userId, String roles) {
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        String phone = "7999000000";
        String sessionId = "session-" + userId;
        long issuedAt = System.currentTimeMillis();
        request.addHeader("X-User-Id", userId);
        request.addHeader("X-User-Roles", roles);
        request.addHeader("X-User-Phone", phone);
        request.addHeader("X-Session-Id", sessionId);
        request.addHeader("X-Issued-At", String.valueOf(issuedAt));
        request.addHeader("X-Identity-Signature", tokens.sign(userId, roles, phone, sessionId, issuedAt));
        return request;
    }

}
