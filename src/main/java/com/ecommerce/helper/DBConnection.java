package com.ecommerce.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce"; 
    private static final String USER = "root"; 
    private static final String PASSWORD = "password"; 

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
