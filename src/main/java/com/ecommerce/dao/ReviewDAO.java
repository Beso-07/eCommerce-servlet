package com.ecommerce.dao;

import com.ecommerce.model.Review;

import java.util.List;

public interface ReviewDAO {
    List<Review> findByProductId(long productId);
    Review save(Review review);
}
