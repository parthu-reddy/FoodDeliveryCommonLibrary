package com.fooddelivery.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import com.fooddelivery.common.constants.SecurityConstants;

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
public class FeignSecurityInterceptor implements RequestInterceptor {

    /** Role granted to background work. Matched by hasRole('SERVICE') on internal endpoints. */
    public static final String SERVICE_ROLE = "SERVICE";

    private final IdentityTokenService identityTokenService;

    @org.springframework.beans.factory.annotation.Value("${spring.application.name:unknown-service}")
    private String applicationName;

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String userId = authentication.getName();
            template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ID, userId);
            
            String roles = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            if (!roles.isEmpty()) {
                template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ROLES, roles);
            }
            
            if (authentication.getDetails() instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> details = (java.util.Map<String, String>) authentication.getDetails();
                if (details.containsKey("phone")) {
                    template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_PHONE, details.get("phone"));
                }
                if (details.containsKey("signature")) {
                    template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_IDENTITY_SIGNATURE, details.get("signature"));
                }
                if (details.containsKey("issuedAt")) {
                    template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_ISSUED_AT, details.get("issuedAt"));
                }
            }
        } else {
            applyServiceIdentity(template);
        }
    }

    /**
     * No principal: this call originates from background work rather than a request. Sign this
     * service's own name as a SERVICE identity so the receiver can authorize it.
     */
    public void applyServiceIdentity(RequestTemplate template) {
        long issuedAt = System.currentTimeMillis();
        String signature = identityTokenService.sign(applicationName, SERVICE_ROLE, null, null, issuedAt);
        template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ID, applicationName);
        template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ROLES, SERVICE_ROLE);
        template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_IDENTITY_SIGNATURE, signature);
        template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_ISSUED_AT, String.valueOf(issuedAt));
    }
}
