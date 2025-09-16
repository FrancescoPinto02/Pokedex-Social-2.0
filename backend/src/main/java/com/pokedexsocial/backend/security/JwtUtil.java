package com.pokedexsocial.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Utility class for generating, validating, and parsing JSON Web Tokens (JWT).
 * <p>
 * This class encapsulates JWT operations using the JJWT library, including
 * token creation, signature validation, and claims extraction.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     String token = jwtUtil.generateToken(userId, username, role);
 *     boolean isValid = jwtUtil.validateToken(token);
 *     String username = jwtUtil.extractUsername(token);
 * </pre>
 */
@Component
public class JwtUtil {
    private final Key secretKey;
    private final long expirationMs;

    /**
     * Constructs a new {@code JwtUtil} with the provided secret key and token expiration.
     *
     * @param secret        the secret key used for signing tokens
     * @param expirationMs  the token expiration time in milliseconds
     */
    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a new JWT token for the given user details.
     *
     * @param userId   the unique identifier of the user
     * @param username the username of the user
     * @param role     the role assigned to the user
     * @return a signed JWT token containing the specified claims
     */
    public String generateToken(Integer userId, String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the provided JWT token.
     *
     * @param token the JWT token to validate
     * @return {@code true} if the token is valid and properly signed; {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the username (subject) from the provided JWT token.
     *
     * @param token the JWT token
     * @return the username (subject) stored in the token
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the user ID from the provided JWT token.
     *
     * @param token the JWT token
     * @return the user ID claim, or {@code null} if not present
     */
    public Integer extractUserId(String token) {
        return extractAllClaims(token).get("userId", Integer.class);
    }

    /**
     * Extracts the role from the provided JWT token.
     *
     * @param token the JWT token
     * @return the role claim, or {@code null} if not present
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extracts the expiration date from the provided JWT token.
     *
     * @param token the JWT token
     * @return the expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
}
