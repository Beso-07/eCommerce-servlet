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

    public void addReview(long productId, long userId, String reviewerName, int rating, String comment) {
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
        reviewDAO.save(review);
    }

    public List<Review> getRecentReviews(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        return reviewDAO.findRecentReviews(limit);
    }

    public void deleteReview(long reviewId, long userId) {
        ValidationUtil.validateId(reviewId, "Review id");
        ValidationUtil.validateId(userId, "User id");
        reviewDAO.deleteById(reviewId, userId);
    }

    public Review getReviewById(long reviewId) {
        ValidationUtil.validateId(reviewId, "Review id");
        return reviewDAO.findById(reviewId);
    }
}
