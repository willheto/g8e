package com.g8e.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConnection {
    public static Connection createDatabaseConnection() throws SQLException {
        Dotenv dotenv = Dotenv.load();

        return DriverManager.getConnection(
                dotenv.get("DB_URL"),
                dotenv.get("DB_USERNAME"),
                dotenv.get("DB_PASSWORD"));
    }
}