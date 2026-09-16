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
}
