package com.fooddelivery.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Import(SecurityContextFilter.class)
public class CommonSecurityConfig {

    /**
     * Optional: SecurityContextFilter is @Profile("!contract-test"), so it is absent under the
     * contract-test profile. Requiring it unconditionally made every contract-test context fail
     * to start. When it is absent the chain simply has no identity filter -- which is exactly
     * what that profile wants.
     */
    private final org.springframework.beans.factory.ObjectProvider<SecurityContextFilter> securityContextFilter;

    public CommonSecurityConfig(org.springframework.beans.factory.ObjectProvider<SecurityContextFilter> securityContextFilter) {
        this.securityContextFilter = securityContextFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/error").permitAll() // Allow internal error dispatch to return real 500s instead of 403s
                .requestMatchers("/webhooks/**", "/api/v1/webhooks/**", "/webhooks/providers/**").permitAll() // Webhooks are secured via signature validation usually
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/restaurants/**", "/api/v1/categories/**").permitAll() // Public catalog endpoints
                .requestMatchers("/api/v1/tracking/**").permitAll() // Allow tracking pixels from unauthenticated users
                .anyRequest().authenticated() // Enforce authentication by default (Default-Deny)
            );
            
        SecurityContextFilter filter = securityContextFilter.getIfAvailable();
        if (filter != null) {
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
