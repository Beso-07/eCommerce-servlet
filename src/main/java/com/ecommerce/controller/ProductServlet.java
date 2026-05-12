package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.model.Review;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.ReviewService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/", "/products", "/products/details", "/products/add", "/products/edit", "/products/delete"})
public class ProductServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ProductServlet.class.getName());
    private final ProductService productService = new ProductService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/products/details".equals(path)) {
            long productId = Long.parseLong(req.getParameter("id"));
            var product = productService.getById(productId);
            if (product.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("product", product.get());
            req.setAttribute("reviews", reviewService.getByProduct(productId));
            req.getRequestDispatcher("/product-details.jsp").forward(req, resp);
            return;
        }

        Map<Product, List<Review>> productsWithReviews = productService.getProductsWithReviews();
        req.setAttribute("productsWithReviews", productsWithReviews);
        
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        if ("/products/add".equals(path)) {
            Product created = productService.addProduct(
                    req.getParameter("name"),
                    req.getParameter("description"),
                    req.getParameter("price"),
                    req.getParameter("imageUrl")
            );
            LOGGER.info("Product created with id " + created.getId());
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }
        if ("/products/edit".equals(path)) {
            long id = Long.parseLong(req.getParameter("id"));
            productService.updateProduct(
                    id,
                    req.getParameter("name"),
                    req.getParameter("description"),
                    req.getParameter("price"),
                    req.getParameter("imageUrl")
            );
            LOGGER.info("Product updated with id " + id);
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }
        if ("/products/delete".equals(path)) {
            long id = Long.parseLong(req.getParameter("id"));
            productService.deleteProduct(id);
            LOGGER.info("Product deleted with id " + id);
            resp.sendRedirect(req.getContextPath() + "/admin");
        }
    }
}
