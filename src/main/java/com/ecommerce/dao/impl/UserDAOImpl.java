package com.ecommerce.dao.impl;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.helper.DBConnection;
import com.ecommerce.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, name, email, password_hash, role FROM users WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load user", ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(long id) {
        String sql = "SELECT id, name, email, password_hash, role FROM users WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load user", ex);
        }
        return Optional.empty();
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users(name, email, password_hash, role) VALUES(?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole());
            statement.executeUpdate();
            try (ResultSet generated = statement.getGeneratedKeys()) {
                if (generated.next()) {
                    user.setId(generated.getLong(1));
                }
            }
            return user;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not create user", ex);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not delete user", ex);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("role")
        );
    }
}
