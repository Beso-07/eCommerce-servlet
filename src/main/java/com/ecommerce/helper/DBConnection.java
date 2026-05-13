package com.ecommerce.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL = System.getenv().getOrDefault("ECOMMERCE_DB_URL", "jdbc:mysql://localhost:3306/ecommerce");
    private static final String USER = System.getenv().getOrDefault("ECOMMERCE_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("ECOMMERCE_DB_PASSWORD", "password");

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}