package com.pokedexsocial.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtAuthFilter filter;

    @Test
        // Ensures OPTIONS requests bypass authentication and continue the chain
    void doFilter_ShouldAllowPassThrough_WhenMethodIsOptions() throws IOException, ServletException {
        // Arrange
        when(request.getMethod()).thenReturn("OPTIONS");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }

    @Test
        // Ensures 401 is returned when Authorization header is missing
    void doFilter_ShouldReturnUnauthorized_WhenAuthorizationHeaderMissing() throws IOException, ServletException {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Missing or invalid Authorization header");
        verifyNoInteractions(jwtUtil);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
        // Ensures 401 is returned when header does not start with "Bearer "
    void doFilter_ShouldReturnUnauthorized_WhenHeaderHasInvalidPrefix() throws IOException, ServletException {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Token XYZ");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Missing or invalid Authorization header");
        verifyNoInteractions(jwtUtil);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
        // Ensures invalid token results in 401 Unauthorized
    void doFilter_ShouldReturnUnauthorized_WhenTokenIsInvalid() throws IOException, ServletException {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer badtoken");
        when(jwtUtil.validateToken("badtoken")).thenReturn(false);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(jwtUtil).validateToken("badtoken");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
        // Ensures valid token sets AuthContext, calls chain, and clears context afterward
    void doFilter_ShouldSetAuthContextAndContinueChain_WhenTokenIsValid()
            throws IOException, ServletException {

        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");

        when(jwtUtil.validateToken("validtoken")).thenReturn(true);
        when(jwtUtil.extractUserId("validtoken")).thenReturn(42);
        when(jwtUtil.extractUsername("validtoken")).thenReturn("AshKetchum");
        when(jwtUtil.extractRole("validtoken")).thenReturn("TRAINER");

        try (MockedStatic<AuthContext> mocked = mockStatic(AuthContext.class)) {

            // Act
            filter.doFilter(request, response, filterChain);

            // Assert
            mocked.verify(() -> AuthContext.setCurrentUser(
                    argThat(u ->
                            u.id() == 42 &&
                                    u.username().equals("AshKetchum") &&
                                    u.role().equals("TRAINER")
                    )
            ), times(1));

            verify(filterChain, times(1)).doFilter(request, response);

            mocked.verify(AuthContext::clear, times(1));
        }
    }
}

