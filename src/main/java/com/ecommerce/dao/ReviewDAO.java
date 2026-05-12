package com.ecommerce.dao;

import com.ecommerce.model.Review;

import java.util.List;

public interface ReviewDAO {
    List<Review> findByProductId(long productId);
    Review save(Review review);
    List<Review> findRecentReviews(int limit);
    Review findById(long reviewId);
    void deleteById(long reviewId, long userId);
}
