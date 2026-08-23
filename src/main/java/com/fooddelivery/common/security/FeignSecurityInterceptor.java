package com.fooddelivery.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import com.fooddelivery.common.constants.SecurityConstants;

@Component
public class FeignSecurityInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            String userId = authentication.getName();
            // Propagate the X-User-Id header to internal microservices
            template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ID, userId);
            
            // Propagate X-User-Roles
            String roles = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace(SecurityConstants.ROLE_PREFIX, ""))
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            if (!roles.isEmpty()) {
                template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ROLES, roles);
            }
            
            // Propagate X-User-Phone
            if (authentication.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> details = (Map<String, String>) authentication.getDetails();
                if (details.containsKey("phone")) {
                    template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_PHONE, details.get("phone"));
                }
                if (details.containsKey("sessionId")) {
                    template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_SESSION_ID, details.get("sessionId"));
                }
            }
            
            // Forward signature and issuedAt from the incoming request attributes.
            // Spring MVC exposes request attributes in RequestContextHolder.
            org.springframework.web.context.request.RequestAttributes attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes) attributes).getRequest();
                String signature = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_IDENTITY_SIGNATURE);
                String issuedAt = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_ISSUED_AT);
                if (signature != null) {
                    template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_IDENTITY_SIGNATURE, signature);
                }
                if (issuedAt != null) {
                    template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_ISSUED_AT, issuedAt);
                }
            }
        }
    }
}
