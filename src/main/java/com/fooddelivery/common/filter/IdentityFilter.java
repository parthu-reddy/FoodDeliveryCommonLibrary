package com.fooddelivery.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class IdentityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        String userId = request.getHeader("X-User-Id");
        String userRolesStr = request.getHeader("X-User-Roles");
        // Fallback for older tokens just in case
        if (userRolesStr == null) {
            userRolesStr = request.getHeader("X-User-Role");
        }

        if (userId != null) {
            request.setAttribute("X_USER_ID", userId);
            
            if (userRolesStr != null && !userRolesStr.isEmpty()) {
                List<String> roles = Arrays.asList(userRolesStr.split(","));
                
                if (roles.contains("CUSTOMER")) {
                    request.setAttribute("CUSTOMER_ID", userId);
                }
                if (roles.contains("RESTAURANT_OWNER") || roles.contains("OUTLET_MANAGER")) {
                    request.setAttribute("OWNER_ID", userId);
                }
                if (roles.contains("DELIVERY_PARTNER") || roles.contains("DELIVERY_EXECUTIVE")) {
                    request.setAttribute("DELIVERY_EXECUTIVE_ID", userId);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
