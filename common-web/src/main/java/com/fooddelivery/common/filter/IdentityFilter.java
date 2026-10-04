package com.fooddelivery.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import com.fooddelivery.common.constants.RequestAttributeConstants;

@Component
@lombok.RequiredArgsConstructor
public class IdentityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String userId = authentication.getName();
            request.setAttribute(RequestAttributeConstants.X_USER_ID, userId);
            var roles = authentication.getAuthorities().stream().map(authority -> authority.getAuthority()).toList();
            if (roles.contains("ROLE_CUSTOMER")) request.setAttribute(RequestAttributeConstants.CUSTOMER_ID, userId);
            if (roles.contains("ROLE_RESTAURANT")) request.setAttribute(RequestAttributeConstants.OWNER_ID, userId);
            if (roles.contains("ROLE_DELIVERY")) request.setAttribute(RequestAttributeConstants.DELIVERY_EXECUTIVE_ID, userId);
        }

        filterChain.doFilter(request, response);
    }
}
