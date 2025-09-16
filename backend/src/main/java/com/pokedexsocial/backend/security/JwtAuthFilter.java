package com.pokedexsocial.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet filter that validates JWT tokens for protected endpoints.
 *
 * <p>This filter performs the following steps:
 * <ul>
 *   <li>Extracts the JWT from the Authorization header.</li>
 *   <li>Validates the token using {@link JwtUtil}.</li>
 *   <li>If valid, extracts user information and populates {@link AuthContext}.</li>
 *   <li>If missing or invalid, immediately returns 401 Unauthorized.</li>
 *   <li>Clears the {@link AuthContext} after processing the request.</li>
 * </ul>
 * </p>
 */
public class JwtAuthFilter extends HttpFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        Integer userId = jwtUtil.extractUserId(token);
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        AuthContext.setCurrentUser(new AuthenticatedUser(userId, username, role));

        try {
            chain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }
}
