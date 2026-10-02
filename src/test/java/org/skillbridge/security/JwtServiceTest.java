package org.skillbridge.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-test-secret-test-secret-test-secret-1234567890");
        props.setAccessTokenTtlSeconds(900);
        props.setRefreshTokenTtlSeconds(86400);
        props.setIssuer("skillbridge-test");
        this.jwtService = new JwtService(props);
    }

    @Test
    void generatesAndParsesAccessToken() {
        String token = jwtService.generateAccessToken(42L, "ada@example.com", List.of("STUDENT"));
        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("email", String.class)).isEqualTo("ada@example.com");
        assertThat(jwtService.isAccessToken(claims)).isTrue();
        assertThat(jwtService.isRefreshToken(claims)).isFalse();
    }

    @Test
    void generatesRefreshToken() {
        String token = jwtService.generateRefreshToken(7L);
        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(jwtService.isRefreshToken(claims)).isTrue();
        assertThat(jwtService.isAccessToken(claims)).isFalse();
    }

    @Test
    void rejectsTooShortSecret() {
        JwtProperties props = new JwtProperties();
        props.setSecret("short");
        try {
            new JwtService(props);
            throw new AssertionError("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // expected
        }
    }
}