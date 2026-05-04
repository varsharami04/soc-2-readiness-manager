package com.campuspe.soc2_readiness_manager.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "mySecretKeyMustBeAtLeast32BytesLongForHmacSha256!";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secret, expirationMs);
    }

    @Test
    void generateToken_WithRole_ReturnsValidToken() {
        String token = jwtUtil.generateToken("testuser", "ROLE_ADMIN");
        
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testuser");
        assertThat(jwtUtil.extractRoles(token)).containsExactly("ROLE_ADMIN");
    }

    @Test
    void generateToken_WithMultipleRoles_ReturnsValidToken() {
        String token = jwtUtil.generateToken("testuser", List.of("ROLE_USER", "ROLE_ADMIN"));
        
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        List<String> roles = jwtUtil.extractRoles(token);
        assertThat(roles).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() throws InterruptedException {
        // Create a JwtUtil with 1ms expiration
        JwtUtil shortLivedJwtUtil = new JwtUtil(secret, 1);
        String token = shortLivedJwtUtil.generateToken("testuser", "ROLE_USER");
        
        // Wait for it to expire
        Thread.sleep(10);
        
        assertThat(shortLivedJwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_WithInvalidSignature_ReturnsFalse() {
        String token = jwtUtil.generateToken("testuser", "ROLE_USER");
        String invalidToken = token + "invalid";
        
        assertThat(jwtUtil.validateToken(invalidToken)).isFalse();
    }

    @Test
    void generateToken_WithShortSecret_ThrowsException() {
        JwtUtil shortSecretUtil = new JwtUtil("short", expirationMs);
        assertThatThrownBy(() -> shortSecretUtil.generateToken("testuser", "ROLE_USER"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("must be at least 32 characters long");
    }
}
