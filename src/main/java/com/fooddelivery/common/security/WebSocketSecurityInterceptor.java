package com.fooddelivery.common.security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.List;

@Component
public class WebSocketSecurityInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        List<String> userIds = request.getHeaders().get("X-User-Id");
        if (userIds != null && !userIds.isEmpty()) {
            attributes.put("userId", userIds.get(0));
            
            List<String> roles = request.getHeaders().get("X-User-Roles");
            if (roles != null && !roles.isEmpty()) {
                attributes.put("roles", roles.get(0));
            }
            return true;
        }
        
        // Block unauthenticated handshakes
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}
