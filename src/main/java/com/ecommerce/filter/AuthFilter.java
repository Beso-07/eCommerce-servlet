package com.ecommerce.filter;

import com.ecommerce.helper.JwtHelper;
import com.ecommerce.helper.RedisHelper;
import com.ecommerce.model.User;
import com.ecommerce.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebFilter(urlPatterns = {"/profile", "/profile/*", "/reviews/*", "/products/add", "/products/delete", "/admin"})
public class AuthFilter implements Filter {
    private static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        // Try JWT authentication first (for API calls)
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                // Check if token is blacklisted
                if (isTokenBlacklisted(token)) {
                    JsonUtil.writeJson(resp, 401, Map.of("message", "Token has been revoked"));
                    return;
                }
                
                // Validate JWT token
                if (JwtHelper.validateToken(token)) {
                    Long userId = JwtHelper.getUserIdFromToken(token);
                    String role = JwtHelper.getRoleFromToken(token);
                    
                    // Create user object from JWT claims
                    User user = new User();
                    user.setId(userId);
                    user.setRole(role);
                    
                    // Set user attribute for downstream use
                    req.setAttribute("user", user);
                    req.setAttribute("authType", "JWT");
                    chain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                // JWT validation failed, continue to session check
                System.err.println("JWT validation failed: " + e.getMessage());
            }
        }
        
        // Fallback to session-based authentication (for web pages)
        User sessionUser = (User) req.getSession().getAttribute("user");
        if (sessionUser != null) {
            req.setAttribute("user", sessionUser);
            req.setAttribute("authType", "SESSION");
            chain.doFilter(request, response);
            return;
        }
        
        // Check if request expects JSON (API call) or HTML (web page)
        String acceptHeader = req.getHeader("Accept");
        boolean expectsJson = acceptHeader != null && acceptHeader.contains("application/json");
        
        if (expectsJson) {
            // Return JSON error for API calls
            JsonUtil.writeJson(resp, 401, Map.of(
                "message", "Authentication required",
                "code", "AUTH_REQUIRED"
            ));
        } else {
            // Redirect to login for web pages
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }
    
    /**
     * Check if JWT token is blacklisted in Redis
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String key = JWT_BLACKLIST_PREFIX + token;
            String result = RedisHelper.get(key);
            return result != null;
        } catch (Exception e) {
            // Redis unavailable, assume token is not blacklisted
            System.err.println("JWT blacklist check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Add JWT token to blacklist (for logout functionality)
     * This can be called from AuthService logout method
     */
    public static void blacklistToken(String token) {
        try {
            // Get token expiration time and set blacklist TTL accordingly
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
