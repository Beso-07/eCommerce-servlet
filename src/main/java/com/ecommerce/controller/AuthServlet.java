package com.ecommerce.controller;

import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.model.User;
import com.ecommerce.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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
                req.getSession().setAttribute("user", user);
                req.getSession().setAttribute("jwt", com.ecommerce.helper.JwtHelper.generateToken(user.getId(), user.getRole()));
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            if ("/signin".equals(uri)) {
                Map<String, Object> result = authService.login(req.getParameter("email"), req.getParameter("password"));
                User user = (User) result.get("user");
                HttpSession session = req.getSession();
                session.setAttribute("user", user);
                session.setAttribute("jwt", result.get("token"));
                LOGGER.info("Successful login for " + user.getEmail());
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            if ("/logout".equals(uri)) {
                req.getSession().invalidate();
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
}
