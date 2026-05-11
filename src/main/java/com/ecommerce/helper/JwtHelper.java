package com.ecommerce.helper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class JwtHelper {
    private static final String SECRET = System.getenv().getOrDefault("ECOMMERCE_JWT_SECRET", "replace-this-dev-secret-with-32-bytes");
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 12;

    private JwtHelper() {
    }

    public static String generateToken(Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(KEY)
                .compact();
    }

    public static Claims validateAndParse(String token) {
        return Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
    }

    public static boolean validateToken(String token) {
        try {
            validateAndParse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Long getUserIdFromToken(String token) {
        Claims claims = validateAndParse(token);
        return Long.parseLong(claims.getSubject());
    }

    public static String getRoleFromToken(String token) {
        Claims claims = validateAndParse(token);
        return claims.get("role", String.class);
    }

    public static long getTokenExpiration(String token) {
        Claims claims = validateAndParse(token);
        return claims.getExpiration().getTime();
    }
}
