package com.fooddelivery.common.security;

import com.fooddelivery.common.constants.HeaderConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebSocketSecurityInterceptorTest {

    private final WebSocketSecurityInterceptor interceptor = new WebSocketSecurityInterceptor();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsArbitraryRawIdentityHeadersWithoutVerifiedSecurityContext() throws Exception {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HeaderConstants.HEADER_USER_ID, "forged-admin");
        headers.add(HeaderConstants.HEADER_USER_ROLES, "ADMIN");
        when(request.getHeaders()).thenReturn(headers);
        Map<String, Object> attributes = new HashMap<>();

        boolean allowed = interceptor.beforeHandshake(
                request, mock(ServerHttpResponse.class), mock(WebSocketHandler.class), attributes);

        assertFalse(allowed);
        assertTrue(attributes.isEmpty());
    }

    @Test
    void derivesHandshakeAttributesFromTheVerifiedSecurityContext() throws Exception {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "customer-1", null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Map<String, Object> attributes = new HashMap<>();

        boolean allowed = interceptor.beforeHandshake(
                mock(ServerHttpRequest.class), mock(ServerHttpResponse.class), mock(WebSocketHandler.class), attributes);

        assertTrue(allowed);
        assertEquals("customer-1", attributes.get("userId"));
        assertEquals("ROLE_CUSTOMER", attributes.get("roles"));
    }
}
