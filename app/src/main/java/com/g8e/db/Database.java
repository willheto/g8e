package com.g8e.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.g8e.util.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

public class Database {
    private static HikariDataSource dataSource;

    static {
        Dotenv dotenv = Dotenv.load();

        Logger.printInfo("Connecting to the database");
        Logger.printInfo(dotenv.get("DB_URL"));
        Logger.printInfo(dotenv.get("DB_USERNAME"));
        Logger.printInfo(dotenv.get("DB_PASSWORD"));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dotenv.get("DB_URL"));
        config.setUsername(dotenv.get("DB_USERNAME"));
        config.setPassword(dotenv.get("DB_PASSWORD"));
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}