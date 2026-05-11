package com.ecommerce.controller;

import com.ecommerce.exception.ValidationException;
import com.ecommerce.model.User;
import com.ecommerce.service.ReviewService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(urlPatterns = "/reviews/add")
public class ReviewServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReviewServlet.class.getName());
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        if (!"USER".equalsIgnoreCase(user.getRole())) {
            req.getSession().setAttribute("reviewError", "Only users can add reviews.");
            String fallbackId = req.getParameter("productId");
            resp.sendRedirect(req.getContextPath() + "/products/details?id=" + fallbackId);
            return;
        }

        long productId;
        try {
            productId = Long.parseLong(req.getParameter("productId"));
            int rating = Integer.parseInt(req.getParameter("rating"));
            reviewService.addReview(productId, user.getId(), user.getName(), rating, req.getParameter("comment"));
            resp.sendRedirect(req.getContextPath() + "/products/details?id=" + productId);
        } catch (NumberFormatException | ValidationException ex) {
            LOGGER.warning("Review validation failed: " + ex.getMessage());
            req.getSession().setAttribute("reviewError", ex.getMessage());
            String fallbackId = req.getParameter("productId");
            resp.sendRedirect(req.getContextPath() + "/products/details?id=" + fallbackId);
        }
    }
}
