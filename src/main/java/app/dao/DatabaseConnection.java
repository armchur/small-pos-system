package app.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connection lifecycle.
 * Reads database parameters from an external config.properties file.
 */
public class DatabaseConnection {

    private static String dbUrl;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                // Fallback to default SQLite file if config is missing
                dbUrl = "jdbc:sqlite:pos.db";
                System.err.println("Warning: config.properties not found, falling back to default " + dbUrl);
            } else {
                props.load(input);
                dbUrl = props.getProperty("db.url", "jdbc:sqlite:pos.db");
            }
        } catch (IOException e) {
            dbUrl = "jdbc:sqlite:pos.db";
            System.err.println("Error reading config.properties: " + e.getMessage());
        }
    }

    /**
     * Gets a new JDBC Connection.
     * Callers must close the connection using try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        // SQLite foreign keys are disabled by default; enable them explicitly
        try (var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Allows setting a custom URL, particularly useful for in-memory testing.
     */
    public static void setCustomUrl(String customUrl) {
        dbUrl = customUrl;
    }
}
