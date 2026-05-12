package com.ecommerce.dao.impl;

import com.ecommerce.dao.ReviewDAO;
import com.ecommerce.helper.DBConnection;
import com.ecommerce.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAOImpl implements ReviewDAO {
    @Override
    public List<Review> findByProductId(long productId) {
        String sql = """
                SELECT r.id, r.product_id, r.user_id, r.rating, r.comment, u.name AS reviewer_name
                FROM reviews r
                JOIN users u ON u.id = r.user_id
                WHERE product_id = ?
                ORDER BY r.id DESC
                """;
        List<Review> reviews = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getLong("user_id"),
                            rs.getString("reviewer_name"),
                            rs.getInt("rating"),
                            rs.getString("comment")
                    ));
                }
            }
            return reviews;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load reviews", ex);
        }
    }

    @Override
    public Review save(Review review) {
        String sql = "INSERT INTO reviews(product_id, user_id, rating, comment) VALUES(?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, review.getProductId());
            statement.setLong(2, review.getUserId());
            statement.setInt(3, review.getRating());
            statement.setString(4, review.getComment());
            statement.executeUpdate();
            try (ResultSet generated = statement.getGeneratedKeys()) {
                if (generated.next()) {
                    review.setId(generated.getLong(1));
                }
            }
            return review;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not create review", ex);
        }
    }

    @Override
    public List<Review> findRecentReviews(int limit) {
        String sql = """
                SELECT r.id, r.product_id, r.user_id, r.rating, r.comment, u.name AS reviewer_name
                FROM reviews r
                JOIN users u ON u.id = r.user_id
                ORDER BY r.id DESC
                LIMIT ?
                """;
        List<Review> reviews = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getLong("user_id"),
                            rs.getString("reviewer_name"),
                            rs.getInt("rating"),
                            rs.getString("comment")
                    ));
                }
            }
            return reviews;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load recent reviews", ex);
        }
    }

    @Override
    public Review findById(long reviewId) {
        String sql = """
                SELECT r.id, r.product_id, r.user_id, r.rating, r.comment, u.name AS reviewer_name
                FROM reviews r
                JOIN users u ON u.id = r.user_id
                WHERE r.id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reviewId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Review(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getLong("user_id"),
                            rs.getString("reviewer_name"),
                            rs.getInt("rating"),
                            rs.getString("comment")
                    );
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not find review", ex);
        }
    }

    @Override
    public void deleteById(long reviewId, long userId) {
        String sql = "DELETE FROM reviews WHERE id = ? AND user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reviewId);
            statement.setLong(2, userId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Review not found or you don't have permission to delete it");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not delete review", ex);
        }
    }
}
