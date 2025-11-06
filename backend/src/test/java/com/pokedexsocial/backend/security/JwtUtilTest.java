package com.pokedexsocial.backend.security;

import com.pokedexsocial.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        // Chiave segreta di 32 byte (HS256 richiede almeno 256 bit)
        String secret = "12345678901234567890123456789012";
        long expirationMs = 1000 * 60 * 60; // 1 ora
        jwtUtil = new JwtUtil(secret, expirationMs);
    }

    @Test
    void generateToken_ShouldContainAllClaims_AndBeValid() {
        String token = jwtUtil.generateToken(1, "testUser", "USER");

        assertThat(token).isNotNull();
        assertThat(jwtUtil.validateToken(token)).isTrue();

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testUser");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(1);
        assertThat(jwtUtil.extractRole(token)).isEqualTo("USER");

        Date expiration = jwtUtil.extractExpiration(token);
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsMalformed() {
        assertThat(jwtUtil.validateToken("not.a.real.jwt")).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsTampered() {
        String validToken = jwtUtil.generateToken(1, "testUser", "USER");
        // Simulo alterazione del token
        String tampered = validToken.substring(0, validToken.length() - 2) + "xx";
        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenInvalid() {
        assertThatThrownBy(() -> jwtUtil.extractUsername("invalid.token"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractClaims_ShouldReturnCorrectData() {
        String token = jwtUtil.generateToken(42, "ashKetchum", "TRAINER");

        assertThat(jwtUtil.extractUserId(token)).isEqualTo(42);
        assertThat(jwtUtil.extractRole(token)).isEqualTo("TRAINER");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("ashKetchum");
    }
}
