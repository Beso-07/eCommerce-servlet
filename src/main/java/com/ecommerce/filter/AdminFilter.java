package com.ecommerce.filter;

import com.ecommerce.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = {"/products/add", "/products/edit", "/products/delete", "/admin"})
public class AdminFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        User user = (User) req.getSession().getAttribute("user");
        boolean isAdminRole = user != null && "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isAdminRole) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }
        chain.doFilter(request, response);
    }
}
