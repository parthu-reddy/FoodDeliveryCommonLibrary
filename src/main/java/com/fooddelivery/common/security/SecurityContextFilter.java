package com.fooddelivery.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@lombok.extern.slf4j.Slf4j
@org.springframework.context.annotation.Profile("!contract-test")
@lombok.RequiredArgsConstructor
public class SecurityContextFilter extends OncePerRequestFilter {

    private final com.fooddelivery.common.security.IdentityTokenService identityTokenService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ID);
        String rolesHeader = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ROLES);
        String phoneHeader = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_PHONE);
        String sessionId = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_SESSION_ID);
        String issuedAtStr = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_ISSUED_AT);
        String signature = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_IDENTITY_SIGNATURE);

        long issuedAt = 0L;
        if (issuedAtStr != null && !issuedAtStr.isEmpty()) {
            try {
                issuedAt = Long.parseLong(issuedAtStr);
            } catch (NumberFormatException ignored) {
                log.warn("Invalid issuedAt header format: {}", issuedAtStr);
            }
        }

        log.info("SecurityContextFilter [{} {}] - Headers: X-User-Id={}, X-User-Roles={}", request.getMethod(), request.getRequestURI(), userId, rolesHeader);

        if (userId != null && rolesHeader != null) {
            boolean isValid = identityTokenService.verify(signature, userId, rolesHeader, phoneHeader, sessionId, issuedAt);
            if (!isValid) {
                log.warn("SecurityContextFilter - Invalid or missing X-Identity-Signature. Proceeding unauthenticated.");
            } else {
                List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .filter(role -> !role.isEmpty())
                        .map(role -> {
                            String uppercaseRole = role.toUpperCase();
                            return new SimpleGrantedAuthority(uppercaseRole.startsWith("ROLE_") ? uppercaseRole : "ROLE_" + uppercaseRole);
                        })
                        .collect(Collectors.toList());
    
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                
                if (phoneHeader != null || sessionId != null || signature != null || issuedAtStr != null) {
                    java.util.Map<String, String> details = new java.util.HashMap<>();
                    if (phoneHeader != null) details.put("phone", phoneHeader);
                    if (sessionId != null) details.put("sessionId", sessionId);
                    if (signature != null) details.put("signature", signature);
                    if (issuedAtStr != null) details.put("issuedAt", issuedAtStr);
                    authentication.setDetails(details);
                }
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("SecurityContextFilter - Authenticated User ID: {} with Roles: {}", userId, authorities);
            }
        } else {
            log.warn("SecurityContextFilter - Missing headers! Proceeding unauthenticated.");
        }

        filterChain.doFilter(request, response);
        
        if (response.getStatus() == 403) {
            log.error("SecurityContextFilter - 403 FORBIDDEN on [{} {}] User: {} Roles: {}", request.getMethod(), request.getRequestURI(), userId, rolesHeader);
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}
