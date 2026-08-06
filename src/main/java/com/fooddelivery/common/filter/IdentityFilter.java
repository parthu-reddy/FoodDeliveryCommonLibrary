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
import com.fooddelivery.common.constants.RequestAttributeConstants;
import com.fooddelivery.common.constants.HeaderConstants;

@Component
public class IdentityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        String userId = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ID);
        String userRolesStr = request.getHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ROLES);
        // Fallback for older tokens just in case
        if (userRolesStr == null) {
            userRolesStr = request.getHeader(HeaderConstants.HEADER_USER_ROLE_FALLBACK);
        }

        if (userId != null) {
            request.setAttribute(RequestAttributeConstants.X_USER_ID, userId);
            
            if (userRolesStr != null && !userRolesStr.isEmpty()) {
                List<String> roles = Arrays.asList(userRolesStr.split(","));
                
                if (roles.contains(com.fooddelivery.common.enums.RoleName.CUSTOMER.name())) {
                    request.setAttribute(RequestAttributeConstants.CUSTOMER_ID, userId);
                }
                if (roles.contains(com.fooddelivery.common.enums.RoleName.RESTAURANT.name())) {
                    request.setAttribute(RequestAttributeConstants.OWNER_ID, userId);
                }
                if (roles.contains(com.fooddelivery.common.enums.RoleName.DELIVERY.name())) {
                    request.setAttribute(RequestAttributeConstants.DELIVERY_EXECUTIVE_ID, userId);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
