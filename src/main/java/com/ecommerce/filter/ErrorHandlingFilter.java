package com.ecommerce.filter;

import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.exception.UserNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandlingFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandlingFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        try {
            chain.doFilter(request, response);
        } catch (ValidationException ex) {
            LOGGER.fine("Validation error: " + ex.getMessage());
            sendError(req, resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (NumberFormatException ex) {
            LOGGER.fine("Bad numeric parameter: " + ex.getMessage());
            sendError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request parameter");
        } catch (UnauthorizedException ex) {
            LOGGER.fine("Unauthorized: " + ex.getMessage());
            if (resp.isCommitted()) return;
            String accept = req.getHeader("Accept");
            if (accept != null && accept.contains("application/json")) {
                JsonUtil.writeJson(resp, 401, Map.of("message", ex.getMessage()));
            } else {
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
            }
        } catch (UserNotFoundException ex) {
            LOGGER.fine("Not found: " + ex.getMessage());
            sendError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Resource not found");
        } catch (IOException | ServletException ex) {
            LOGGER.log(Level.SEVERE, "Unhandled servlet exception", ex);
            sendError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Unhandled runtime exception", ex);
            sendError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void sendError(HttpServletRequest req, HttpServletResponse resp, int status, String message) throws IOException {
        if (resp.isCommitted()) return;
        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            JsonUtil.writeJson(resp, status, Map.of("message", message, "status", status));
            return;
        }
        resp.sendError(status, message);
    }
}
