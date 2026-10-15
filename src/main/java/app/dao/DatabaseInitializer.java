package app.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility to run schema and initial seed scripts against the database.
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        executeSqlScript("db/schema.sql");
    }

    public static void seedDatabase() {
        executeSqlScript("db/seed.sql");
    }

    private static void executeSqlScript(String filePath) {
        try (Connection conn = DatabaseConnection.getConnection();
             BufferedReader reader = new BufferedReader(new FileReader(filePath));
             Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Skip comments and empty lines
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                sql.append(line).append(" ");
                if (trimmed.endsWith(";")) {
                    stmt.execute(sql.toString());
                    sql.setLength(0);
                }
            }
            System.out.println("Successfully executed SQL script: " + filePath);
        } catch (IOException | SQLException e) {
            System.err.println("Error executing script " + filePath + ": " + e.getMessage());
        }
    }
}
