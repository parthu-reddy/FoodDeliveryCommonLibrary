package com.fooddelivery.common.config;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ThreadLocalAccessor;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Configuration
public class ContextPropagationConfig {

    @PostConstruct
    public void registerAccessors() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new ThreadLocalAccessor<SecurityContext>() {
            @Override
            public Object key() {
                return "securityContext";
            }

            @Override
            public SecurityContext getValue() {
                return SecurityContextHolder.getContext();
            }

            @Override
            public void setValue(SecurityContext value) {
                SecurityContextHolder.setContext(value);
            }

            @Override
            public void reset() {
                SecurityContextHolder.clearContext();
            }
        });

        ContextRegistry.getInstance().registerThreadLocalAccessor(new ThreadLocalAccessor<RequestAttributes>() {
            @Override
            public Object key() {
                return "requestContext";
            }

            @Override
            public RequestAttributes getValue() {
                return RequestContextHolder.getRequestAttributes();
            }

            @Override
            public void setValue(RequestAttributes value) {
                RequestContextHolder.setRequestAttributes(value);
            }

            @Override
            public void reset() {
                RequestContextHolder.resetRequestAttributes();
            }
        });
    }
}
