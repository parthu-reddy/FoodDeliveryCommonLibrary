package com.fooddelivery.common.security;

import com.fooddelivery.common.constants.HeaderConstants;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@lombok.RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class SecurityHeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    private static final String SERVICE_ROLE = "SERVICE";

    private final IdentityTokenService identityTokenService;

    @org.springframework.beans.factory.annotation.Value("${spring.application.name:unknown-service}")
    private String applicationName;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        clearIdentityHeaders(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() != null
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String userId = authentication.getName();
            Map<?, ?> details = authentication.getDetails() instanceof Map<?, ?> map ? map : Map.of();
            String signature = detail(details, "signature");
            String issuedAt = detail(details, "issuedAt");

            if (signature != null && issuedAt != null) {
                String roles = detail(details, "roles");
                if (roles == null) {
                    roles = authentication.getAuthorities().stream()
                            .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                            .reduce((left, right) -> left + "," + right)
                            .orElse("");
                }

                request.getHeaders().set(HeaderConstants.HEADER_USER_ID, userId);
                request.getHeaders().set(HeaderConstants.HEADER_USER_ROLES, roles);
                setIfPresent(request, HeaderConstants.HEADER_USER_PHONE, detail(details, "phone"));
                setIfPresent(request, HeaderConstants.HEADER_SESSION_ID, detail(details, "sessionId"));
                request.getHeaders().set(HeaderConstants.HEADER_IDENTITY_SIGNATURE, signature);
                request.getHeaders().set(HeaderConstants.HEADER_ISSUED_AT, issuedAt);
                return execution.execute(request, body);
            }

            log.warn("REST_CALLER_IDENTITY_INCOMPLETE application={} userId={}; using SERVICE identity",
                    applicationName, userId);
        }

        applyServiceIdentity(request);
        return execution.execute(request, body);
    }

    private void applyServiceIdentity(HttpRequest request) {
        long issuedAt = System.currentTimeMillis();
        String signature = identityTokenService.sign(applicationName, SERVICE_ROLE, null, null, issuedAt);
        request.getHeaders().set(HeaderConstants.HEADER_USER_ID, applicationName);
        request.getHeaders().set(HeaderConstants.HEADER_USER_ROLES, SERVICE_ROLE);
        request.getHeaders().set(HeaderConstants.HEADER_IDENTITY_SIGNATURE, signature);
        request.getHeaders().set(HeaderConstants.HEADER_ISSUED_AT, String.valueOf(issuedAt));
    }

    private static void clearIdentityHeaders(HttpRequest request) {
        request.getHeaders().remove(HeaderConstants.HEADER_USER_ID);
        request.getHeaders().remove(HeaderConstants.HEADER_USER_ROLES);
        request.getHeaders().remove(HeaderConstants.HEADER_USER_PHONE);
        request.getHeaders().remove(HeaderConstants.HEADER_SESSION_ID);
        request.getHeaders().remove(HeaderConstants.HEADER_IDENTITY_SIGNATURE);
        request.getHeaders().remove(HeaderConstants.HEADER_ISSUED_AT);
    }

    private static void setIfPresent(HttpRequest request, String header, String value) {
        if (value != null) {
            request.getHeaders().set(header, value);
        }
    }

    private static String detail(Map<?, ?> details, String key) {
        Object value = details.get(key);
        return value instanceof String stringValue ? stringValue : null;
    }
}
