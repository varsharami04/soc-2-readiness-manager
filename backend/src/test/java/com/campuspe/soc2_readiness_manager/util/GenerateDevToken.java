package com.campuspe.soc2_readiness_manager.util;

import com.campuspe.soc2_readiness_manager.config.JwtUtil;

public class GenerateDevToken {

    public static void main(String[] args) {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET environment variable is not set");
        }
        long expirationMs = 86400000L;

        JwtUtil jwtUtil = new JwtUtil(secret, expirationMs);
        String token = jwtUtil.generateToken("atul_dev", "ADMIN");

        System.out.println("==================================================");
        System.out.println("Valid Development JWT Token (expires in 24 hours):");
        System.out.println("==================================================");
        System.out.println(token);
        System.out.println("==================================================");
    }
}
