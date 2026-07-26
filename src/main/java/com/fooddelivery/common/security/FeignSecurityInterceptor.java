package com.fooddelivery.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Map;

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
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
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
            }
        }
    }
}
