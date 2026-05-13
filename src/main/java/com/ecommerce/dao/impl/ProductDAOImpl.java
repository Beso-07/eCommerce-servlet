package com.ecommerce.dao.impl;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.helper.DBConnection;
import com.ecommerce.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {
    @Override
    public List<Product> findAll() {
        String sql = "SELECT id, name, description, price, image_url FROM products ORDER BY id DESC";
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                products.add(map(rs));
            }
            return products;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load products", ex);
        }
    }

    @Override
    public Optional<Product> findById(long id) {
        String sql = "SELECT id, name, description, price, image_url FROM products WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Could not load product", ex);
        }
        return Optional.empty();
    }

    @Override
    public Product create(Product product) {
        String sql = "INSERT INTO products(name, description, price, image_url) VALUES(?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setString(4, product.getImageUrl());
            statement.executeUpdate();
            try (ResultSet generated = statement.getGeneratedKeys()) {
                if (generated.next()) {
                    product.setId(generated.getLong(1));
                }
            }
            return product;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not create product", ex);
        }
    }

    @Override
    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, image_url = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setString(4, product.getImageUrl());
            statement.setLong(5, product.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not update product", ex);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not delete product", ex);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getString("image_url")
        );
    }
}
