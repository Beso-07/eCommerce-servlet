package com.ecommerce.filter;

import com.ecommerce.helper.JwtHelper;
import com.ecommerce.helper.RedisHelper;
import com.ecommerce.model.User;
import com.ecommerce.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class AuthFilter implements Filter {
    public static final String JWT_COOKIE_NAME = "jwt";
    private static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String token = extractTokenFromHeader(req);
        if (token == null) {
            token = extractTokenFromCookie(req);
        }

        if (token != null) {
            try {
                if (isTokenBlacklisted(token)) {
                    respondUnauthorized(req, resp, "Token has been revoked");
                    return;
                }
                if (JwtHelper.validateToken(token)) {
                    Long userId = JwtHelper.getUserIdFromToken(token);
                    String role = JwtHelper.getRoleFromToken(token);
                    User user = new User();
                    user.setId(userId);
                    user.setRole(role);
                    req.setAttribute("user", user);
                    req.setAttribute("authType", "JWT");
                    chain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                System.err.println("JWT validation failed: " + e.getMessage());
            }
        }

        User sessionUser = (User) req.getSession().getAttribute("user");
        if (sessionUser != null) {
            req.setAttribute("user", sessionUser);
            req.setAttribute("authType", "SESSION");
            chain.doFilter(request, response);
            return;
        }

        respondUnauthorized(req, resp, "Authentication required");
    }

    private static String extractTokenFromHeader(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public static String extractTokenFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    private void respondUnauthorized(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        String accept = req.getHeader("Accept");
        boolean expectsJson = accept != null && accept.contains("application/json");
        if (expectsJson) {
            JsonUtil.writeJson(resp, 401, Map.of("message", message, "code", "AUTH_REQUIRED"));
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    private boolean isTokenBlacklisted(String token) {
        try {
            String key = JWT_BLACKLIST_PREFIX + token;
            String result = RedisHelper.get(key);
            return result != null;
        } catch (Exception e) {
            System.err.println("JWT blacklist check failed: " + e.getMessage());
            return false;
        }
    }

    public static void blacklistToken(String token) {
        if (token == null || token.isEmpty()) return;
        try {
            long expirationTime = JwtHelper.getTokenExpiration(token);
            long ttlSeconds = (expirationTime - System.currentTimeMillis()) / 1000;
            if (ttlSeconds > 0) {
                String key = JWT_BLACKLIST_PREFIX + token;
                RedisHelper.setex(key, (int) ttlSeconds, "revoked");
            }
        } catch (Exception e) {
            System.err.println("Failed to blacklist token: " + e.getMessage());
        }
    }
}
