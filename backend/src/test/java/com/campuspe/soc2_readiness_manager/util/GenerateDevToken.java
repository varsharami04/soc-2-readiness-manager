package com.campuspe.soc2_readiness_manager.util;

import com.campuspe.soc2_readiness_manager.config.JwtUtil;

public class GenerateDevToken {

    public static void main(String[] args) {
        // Use the exact same values from backend/.env
        String secret = "this-is-a-development-only-secret-key-that-must-be-at-least-32-chars";
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
