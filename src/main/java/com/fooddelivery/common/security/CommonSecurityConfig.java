package com.fooddelivery.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class CommonSecurityConfig {

    private final SecurityContextFilter securityContextFilter;

    public CommonSecurityConfig(SecurityContextFilter securityContextFilter) {
        this.securityContextFilter = securityContextFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(securityContextFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                .requestMatchers("/error").permitAll() // Allow internal error dispatch to return real 500s instead of 403s
                .requestMatchers("/webhooks/**", "/api/v1/webhooks/**", "/webhooks/providers/**").permitAll() // Webhooks are secured via signature validation usually
                .requestMatchers("/api/v1/internal/auth/**").permitAll() // Internal Auth endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/restaurants/**", "/api/v1/categories/**").permitAll() // Public catalog endpoints
                .anyRequest().authenticated() // Enforce authentication by default (Default-Deny)
            );
            
        return http.build();
    }
}
