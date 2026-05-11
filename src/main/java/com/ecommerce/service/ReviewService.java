package com.ecommerce.service;

import com.ecommerce.dao.ReviewDAO;
import com.ecommerce.dao.impl.ReviewDAOImpl;
import com.ecommerce.model.Review;
import com.ecommerce.util.ValidationUtil;

import java.util.List;

public class ReviewService {
    private final ReviewDAO reviewDAO = new ReviewDAOImpl();

    public List<Review> getByProduct(long productId) {
        ValidationUtil.validateId(productId, "Product id");
        return reviewDAO.findByProductId(productId);
    }

    public Review addReview(long productId, long userId, String reviewerName, int rating, String comment) {
        ValidationUtil.validateId(productId, "Product id");
        ValidationUtil.validateId(userId, "User id");
        ValidationUtil.requireNotBlank(reviewerName, "Reviewer name");
        ValidationUtil.validateRating(rating);
        ValidationUtil.requireNotBlank(comment, "Review comment");
        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setReviewerName(reviewerName.trim());
        review.setRating(rating);
        review.setComment(comment.trim());
        return reviewDAO.save(review);
    }
}
