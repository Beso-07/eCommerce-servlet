package com.ecommerce.filter;

import com.ecommerce.helper.RedisHelper;
import com.ecommerce.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Map;

public class RateLimitFilter implements Filter {
    private static final int MAX_REQUESTS = 30;
    private static final int WINDOW_SECONDS = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            String clientIp = getClientIp(req);
            String key = "ratelimit:" + clientIp;
            
            try (Jedis jedis = RedisHelper.getPool().getResource()) {
                long currentCount = jedis.incr(key);
                
                if (currentCount == 1) {
                    jedis.expire(key, WINDOW_SECONDS);
                }
                
                if (currentCount > MAX_REQUESTS) {
                    JsonUtil.writeJson(resp, 429, Map.of(
                        "message", "Too Many Requests",
                        "retryAfter", WINDOW_SECONDS,
                        "limit", MAX_REQUESTS
                    ));
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Rate limiting unavailable: " + e.getMessage());
        }
        chain.doFilter(request, response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
