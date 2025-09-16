package com.pokedexsocial.backend.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the JWT authentication filter for protected API routes.
 *
 * <p>The filter is applied only to URLs that require authentication (e.g., /user/*),
 * ensuring that unauthenticated requests are rejected before reaching controllers.</p>
 */
@Configuration
public class FilterConfig {

    private final JwtUtil jwtUtil;

    public FilterConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter() {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthFilter(jwtUtil));
        registration.addUrlPatterns("/user/*");
        registration.setOrder(1);
        return registration;
    }
}
