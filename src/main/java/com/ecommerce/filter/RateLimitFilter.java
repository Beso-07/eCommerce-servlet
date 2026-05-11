package com.ecommerce.filter;

import com.ecommerce.helper.RedisHelper;
import com.ecommerce.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

//@WebFilter(urlPatterns = "/*")
public class RateLimitFilter implements Filter {
    private static final int MAX_REQUESTS = 10;
    private static final int WINDOW_SECONDS = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String key = "ratelimit:" + req.getRemoteAddr();
        boolean allowed = RedisHelper.incrementWithLimit(key, WINDOW_SECONDS, MAX_REQUESTS);
        if (!allowed) {
            JsonUtil.writeJson(resp, 429, Map.of("message", "Too Many Requests"));
            return;
        }
        chain.doFilter(request, response);
    }
}
