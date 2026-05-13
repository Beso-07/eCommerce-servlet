package com.ecommerce.controller;

import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.filter.AuthFilter;
import com.ecommerce.helper.JwtHelper;
import com.ecommerce.model.User;
import com.ecommerce.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/signin", "/signup", "/logout"})
public class AuthServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AuthServlet.class.getName());
    private static final int JWT_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 12;
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String uri = req.getServletPath();
        try {
            if ("/signup".equals(uri)) {
                User user = authService.register(
                        req.getParameter("name"),
                        req.getParameter("email"),
                        req.getParameter("password"),
                        req.getParameter("confirmPassword")
                );
                String token = JwtHelper.generateToken(user.getId(), user.getRole());
                HttpSession session = req.getSession();
                session.setAttribute("user", user);
                session.setAttribute("jwt", token);
                writeJwtCookie(req, resp, token);
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            if ("/signin".equals(uri)) {
                Map<String, Object> result = authService.login(req.getParameter("email"), req.getParameter("password"));
                User user = (User) result.get("user");
                String token = (String) result.get("token");
                HttpSession session = req.getSession();
                session.setAttribute("user", user);
                session.setAttribute("jwt", token);
                writeJwtCookie(req, resp, token);
                LOGGER.info("Successful login for " + user.getEmail());
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            if ("/logout".equals(uri)) {
                String token = findTokenForLogout(req);
                if (token != null) {
                    AuthFilter.blacklistToken(token);
                }
                clearJwtCookie(req, resp);
                HttpSession session = req.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                resp.sendRedirect(req.getContextPath() + "/");
            }
        } catch (ValidationException | UnauthorizedException ex) {
            LOGGER.warning("Auth failed: " + ex.getMessage());
            HttpSession session = req.getSession();
            if ("/signup".equals(uri)) {
                session.setAttribute("registerError", ex.getMessage());
                resp.sendRedirect(req.getContextPath() + "/register.jsp");
                return;
            }
            session.setAttribute("loginError", ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }

    private static String findTokenForLogout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object sessionToken = session.getAttribute("jwt");
            if (sessionToken instanceof String s && !s.isEmpty()) {
                return s;
            }
        }
        String cookieToken = AuthFilter.extractTokenFromCookie(req);
        if (cookieToken != null) return cookieToken;
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private static void writeJwtCookie(HttpServletRequest req, HttpServletResponse resp, String token) {
        Cookie cookie = new Cookie(AuthFilter.JWT_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath(cookiePath(req));
        cookie.setMaxAge(JWT_COOKIE_MAX_AGE_SECONDS);
        cookie.setSecure(req.isSecure());
        resp.addCookie(cookie);
    }

    private static void clearJwtCookie(HttpServletRequest req, HttpServletResponse resp) {
        Cookie cookie = new Cookie(AuthFilter.JWT_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath(cookiePath(req));
        cookie.setMaxAge(0);
        cookie.setSecure(req.isSecure());
        resp.addCookie(cookie);
    }

    private static String cookiePath(HttpServletRequest req) {
        String ctx = req.getContextPath();
        return (ctx == null || ctx.isEmpty()) ? "/" : ctx;
    }
}
