package com.warehouse.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton that manages the single JDBC connection to MySQL.
 *
 * <p>
 * Configuration is read from {@code db.properties} on the classpath.
 * For simplicity in this university project the credentials are embedded;
 * in production they should be externalised as environment variables.
 * </p>
 */
public class DatabaseConnection {

    private static final Logger LOG = LogManager.getLogger(DatabaseConnection.class);

    // ---- JDBC settings – adjust to your local MySQL instance -----------
    private static final String URL = "jdbc:mysql://localhost:3306/warehouse_db"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "root"; // change as needed

    // Singleton instance
    private static DatabaseConnection instance;
    private Connection connection;

    // ---- Private constructor -------------------------------------------

    private DatabaseConnection() {
        openConnection();
    }

    // ---- Singleton accessor -------------------------------------------

    /**
     * Returns the shared singleton.
     * Thread-safe via {@code synchronized}; adequate for a single-user desktop app.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ---- Public API ----------------------------------------------------

    /**
     * Returns an active {@link Connection}, re-opening it if it has been closed
     * or the network link was lost.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                openConnection();
            }
        } catch (SQLException ex) {
            LOG.error("Failed to check connection state: {}", ex.getMessage(), ex);
            openConnection();
        }
        return connection;
    }

    /** Cleanly closes the JDBC connection. */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                LOG.info("Database connection closed.");
            } catch (SQLException ex) {
                LOG.warn("Error while closing DB connection: {}", ex.getMessage());
            }
        }
    }

    // ---- Private helpers -----------------------------------------------

    private void openConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            LOG.info("Database connection established: {}", URL);
        } catch (ClassNotFoundException ex) {
            LOG.fatal("MySQL JDBC driver not found!", ex);
            throw new RuntimeException("MySQL driver missing", ex);
        } catch (SQLException ex) {
            LOG.fatal("Cannot connect to database: {}", ex.getMessage(), ex);
            throw new RuntimeException("DB connection failed", ex);
        }
    }
}
