package com.fooddelivery.common.security;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            String userId = authentication.getName();
            // Propagate the X-User-Id header to internal microservices
            request.getHeaders().add(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ID, userId);
            
            // Propagate X-User-Roles
            String roles = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            if (!roles.isEmpty()) {
                request.getHeaders().add(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ROLES, roles);
            }
            
            // Propagate X-User-Phone
            if (authentication.getDetails() instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> details = (java.util.Map<String, String>) authentication.getDetails();
                if (details.containsKey("phone")) {
                    request.getHeaders().add(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_PHONE, details.get("phone"));
                }
            }
        }
        return execution.execute(request, body);
    }
}
