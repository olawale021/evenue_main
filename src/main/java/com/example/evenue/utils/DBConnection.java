package com.example.evenue.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:sqlite:/Users/olawale/Downloads/EVENUE-main/database.db"; // Replace with your database URL

    // Private constructor to prevent instantiation
    private DBConnection() {}

    // Method to establish a connection to the database
    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL);
            System.out.println("Database connection established.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return connection;
    }

    public static void main(String[] args) {
        Connection connection = DBConnection.getConnection();
        if (connection != null) {
            // Optionally, you can close the connection after testing
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing the database connection: " + e.getMessage());
            }
        }
    }
}
