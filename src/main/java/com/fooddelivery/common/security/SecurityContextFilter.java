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
public class SecurityContextFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityContextFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ID);
        String rolesHeader = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ROLES);
        String phoneHeader = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_PHONE);

        log.info("SecurityContextFilter [{} {}] - Headers: X-User-Id={}, X-User-Roles={}", request.getMethod(), request.getRequestURI(), userId, rolesHeader);

        if (userId != null && rolesHeader != null) {
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            
            if (phoneHeader != null && !phoneHeader.isEmpty()) {
                java.util.Map<String, String> details = new java.util.HashMap<>();
                details.put("phone", phoneHeader);
                authentication.setDetails(details);
            }
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("SecurityContextFilter - Authenticated User ID: {} with Roles: {}", userId, authorities);
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
