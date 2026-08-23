package com.fooddelivery.common.security;

import com.fooddelivery.common.constants.HeaderConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The filter chain must actually reject an unauthenticated caller.
 *
 * <p>The companion {@code EndpointAuthorizationCoverageTest} in each service proves every endpoint
 * *declares* an authorization rule. This proves the chain *enforces* one — the other half of gap
 * G-6 in the 2026-08-22 core services review, which found 26 endpoints open and no test anywhere
 * asserting that any endpoint rejects anybody.
 *
 * <p>Tested once here rather than in all 34 controllers: {@link CommonSecurityConfig} is the single
 * chain every service imports, so this is where the behaviour lives.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = SecurityFilterChainRejectionTest.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
                "outbox.enabled=false",
                "security.identity.hmac-secret=test-only-secret-for-filter-chain-rejection-tests"
        })
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
class SecurityFilterChainRejectionTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    @Import({CommonSecurityConfig.class, SecurityContextFilter.class, IdentityTokenService.class})
    static class TestApp {

        @RestController
        static class ProbeController {

            /** Stands in for any ordinary business endpoint. */
            @GetMapping("/api/v1/probe/protected")
            String protectedEndpoint() {
                return "ok";
            }

            /** Stands in for an endpoint under the internal prefix. */
            @GetMapping("/api/v1/internal/probe")
            String internalEndpoint() {
                return "ok";
            }
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IdentityTokenService identityTokenService;

    @Test
    void anonymousCallerIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/probe/protected"))
                .andExpect(status().is4xxClientError());
    }

    /**
     * The finding that started this: {@code /api/v1/internal/**} was {@code permitAll()}, so
     * anything that reached a pod could call it. It must not be anonymous again.
     */
    @Test
    void internalPrefixIsNotAnonymouslyAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/internal/probe"))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Unsigned identity headers must not authenticate. This is the I-3 attack: set
     * {@code X-User-Roles: ADMIN} and be an administrator.
     */
    @Test
    void unsignedIdentityHeadersDoNotAuthenticate() throws Exception {
        mockMvc.perform(get("/api/v1/probe/protected")
                        .header(HeaderConstants.HEADER_USER_ID, "11111111-1111-1111-1111-111111111111")
                        .header(HeaderConstants.HEADER_USER_ROLES, "ADMIN"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void correctlySignedIdentityHeadersAreAccepted() throws Exception {
        String userId = "11111111-1111-1111-1111-111111111111";
        String roles = "CUSTOMER";
        long issuedAt = System.currentTimeMillis();
        String signature = identityTokenService.sign(userId, roles, null, null, issuedAt);

        mockMvc.perform(get("/api/v1/probe/protected")
                        .header(HeaderConstants.HEADER_USER_ID, userId)
                        .header(HeaderConstants.HEADER_USER_ROLES, roles)
                        .header(HeaderConstants.HEADER_IDENTITY_SIGNATURE, signature)
                        .header(HeaderConstants.HEADER_ISSUED_AT, String.valueOf(issuedAt)))
                .andExpect(status().isOk());
    }

    /** A genuinely public path must stay reachable, or the chain is simply broken. */
    @Test
    void publicActuatorPathRemainsAnonymous() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().is(org.hamcrest.Matchers.not(401)));
    }
}
