package com.fooddelivery.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import com.fooddelivery.common.constants.HeaderConstants;

/**
 * Puts an identity on every outbound Feign call.
 *
 * <p>With a principal in the context it forwards the caller's own identity and the gateway's original
 * signature. Without one -- a {@code @Scheduled} job, a startup runner, a Kafka listener -- it mints a
 * SERVICE identity instead. Before that existed, background calls arrived with no headers at all, so
 * internal endpoints could not require anything: adding even {@code isAuthenticated()} to
 * {@code /api/v1/internal/campaigns/active-for-bidding} would have 403'd ad index reconciliation.
 *
 * <p>The service identity is signed with the same {@code IDENTITY_HMAC_SECRET} the gateway uses, so
 * the receiver's {@link SecurityContextFilter} verifies it through the identical path and grants
 * {@code ROLE_SERVICE}. No new secret and no new verification code.
 *
 * <p><b>Trust boundary.</b> Every service already holds that secret in order to verify, so any of them
 * could already mint any identity; this widens no trust between services. It does mean the secret is
 * now a service-authentication credential and not only an integrity check. An outsider cannot use it:
 * {@code GlobalJwtAuthFilter} strips {@code X-User-*} and {@code X-Identity-Signature} from inbound
 * requests and 403s external {@code /api/v1/internal/**}, so a SERVICE identity can only originate
 * inside the mesh.
 */
@Component
@lombok.RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class FeignSecurityInterceptor implements RequestInterceptor {

    /** Role granted to background work. Matched by hasRole('SERVICE') on internal endpoints. */
    public static final String SERVICE_ROLE = "SERVICE";

    private final IdentityTokenService identityTokenService;

    @org.springframework.beans.factory.annotation.Value("${spring.application.name:unknown-service}")
    private String applicationName;

    @Override
    public void apply(RequestTemplate template) {
        // A Feign RequestTemplate appends repeated header values. Clear the complete signed tuple
        // first so another interceptor, retry, or preconfigured client cannot leave us with a user
        // id from one identity and a signature from another.
        clearIdentityHeaders(template);

        // If this is an internal service-to-service call, always use the SERVICE identity.
        // Internal endpoints expect the caller to be a service, not the original customer.
        if (template.url().contains("/internal/")) {
            applyServiceIdentity(template);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() != null
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String userId = authentication.getName();
            Map<?, ?> details = authentication.getDetails() instanceof Map<?, ?> map ? map : Map.of();
            String signature = detail(details, "signature");
            String issuedAt = detail(details, "issuedAt");

            // SecurityContextFilter verified this exact tuple. Do not reconstruct signed values
            // from authorities: role ordering, ROLE_ normalization, or a missing session id would
            // make the original HMAC invalid at the next service.
            if (signature != null && issuedAt != null) {
                String roles = detail(details, "roles");
                if (roles == null) {
                    roles = authentication.getAuthorities().stream()
                            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                            .reduce((a, b) -> a + "," + b)
                            .orElse("");
                }

                template.header(HeaderConstants.HEADER_USER_ID, userId);
                template.header(HeaderConstants.HEADER_USER_ROLES, roles);
                putIfPresent(template, HeaderConstants.HEADER_USER_PHONE, detail(details, "phone"));
                putIfPresent(template, HeaderConstants.HEADER_SESSION_ID, detail(details, "sessionId"));
                template.header(HeaderConstants.HEADER_IDENTITY_SIGNATURE, signature);
                template.header(HeaderConstants.HEADER_ISSUED_AT, issuedAt);
                return;
            }

            // An authenticated context without the gateway-verified tuple cannot be propagated as
            // that user. Mint the calling service's least-privileged identity instead of sending an
            // unsigned or partially signed user identity.
            log.warn("FEIGN_CALLER_IDENTITY_INCOMPLETE application={} userId={}; using SERVICE identity",
                    applicationName, userId);
        }

        applyServiceIdentity(template);
    }

    /**
     * No principal: this call originates from background work rather than a request. Sign this
     * service's own name as a SERVICE identity so the receiver can authorize it.
     */
    public void applyServiceIdentity(RequestTemplate template) {
        clearIdentityHeaders(template);
        long issuedAt = System.currentTimeMillis();
        String signature = identityTokenService.sign(applicationName, SERVICE_ROLE, null, null, issuedAt);
        template.header(HeaderConstants.HEADER_USER_ID, applicationName);
        template.header(HeaderConstants.HEADER_USER_ROLES, SERVICE_ROLE);
        template.header(HeaderConstants.HEADER_IDENTITY_SIGNATURE, signature);
        template.header(HeaderConstants.HEADER_ISSUED_AT, String.valueOf(issuedAt));
    }

    static void clearIdentityHeaders(RequestTemplate template) {
        template.removeHeader(HeaderConstants.HEADER_USER_ID);
        template.removeHeader(HeaderConstants.HEADER_USER_ROLES);
        template.removeHeader(HeaderConstants.HEADER_USER_PHONE);
        template.removeHeader(HeaderConstants.HEADER_SESSION_ID);
        template.removeHeader(HeaderConstants.HEADER_IDENTITY_SIGNATURE);
        template.removeHeader(HeaderConstants.HEADER_ISSUED_AT);
    }

    private static void putIfPresent(RequestTemplate template, String header, String value) {
        if (value != null) {
            template.header(header, value);
        }
    }

    private static String detail(Map<?, ?> details, String key) {
        Object value = details.get(key);
        return value instanceof String stringValue ? stringValue : null;
    }
}
