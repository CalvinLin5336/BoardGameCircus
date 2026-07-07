package com.turing.util;

import com.turing.exception.GameException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database utility for MySQL 8.0 connectivity.
 */
public class DbUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/turing_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "1234"; // Default root password
    
    private static boolean useMockMode = false;
    private static boolean initialized = false;

    static {
        try {
            // Load MySQL 8.0 Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Defaulting to local memory mode: " + e.getMessage());
            useMockMode = true;
        }
    }

    private static synchronized void initializeDatabase(Connection conn) {
        if (initialized) return;
        try (Statement stmt = conn.createStatement()) {
            // 1. Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL DEFAULT 'USER', " +
                    "tokens INT DEFAULT 0, " +
                    "is_blocked BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 2. Create game_config table
            stmt.execute("CREATE TABLE IF NOT EXISTS game_config (" +
                    "config_key VARCHAR(50) PRIMARY KEY, " +
                    "config_value VARCHAR(100) NOT NULL, " +
                    "description VARCHAR(255)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 3. Create questions table
            stmt.execute("CREATE TABLE IF NOT EXISTS questions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ans_b INT NOT NULL, " +
                    "ans_y INT NOT NULL, " +
                    "ans_p INT NOT NULL, " +
                    "k_count INT NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 4. Create question_conditions table
            stmt.execute("CREATE TABLE IF NOT EXISTS question_conditions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "question_id INT NOT NULL, " +
                    "condition_id INT NOT NULL, " +
                    "sub_index INT NOT NULL, " +
                    "description VARCHAR(255) NOT NULL, " +
                    "FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 5. Create game_records table
            stmt.execute("CREATE TABLE IF NOT EXISTS game_records (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "player_name VARCHAR(100) NOT NULL, " +
                    "puzzle_id VARCHAR(50) NOT NULL, " +
                    "rounds_used INT NOT NULL, " +
                    "tests_used INT NOT NULL, " +
                    "secret_code CHAR(3) NOT NULL, " +
                    "won BOOLEAN NOT NULL, " +
                    "played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 6. Seed default users if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO users (username, password, role, tokens, is_blocked) VALUES " +
                            "('admin', 'admin123', 'ADMIN', 100, FALSE), " +
                            "('user', 'user123', 'USER', 10, FALSE), " +
                            "('turing', 'turing123', 'USER', 50, FALSE);");
                }
            }

            // 7. Seed default configs if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM game_config")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO game_config (config_key, config_value, description) VALUES " +
                            "('reward_rounds_limit', '5', '規定回合數限制 (在此回合數內解出可得代幣)'), " +
                            "('reward_tokens', '10', '達成條件時可獲得的代幣數 (N枚代幣)');");
                }
            }

            // 8. Seed questions if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO questions (id, ans_b, ans_y, ans_p, k_count) VALUES " +
                            "(1, 3, 2, 1, 4), " +
                            "(2, 4, 4, 5, 4), " +
                            "(3, 2, 4, 5, 5), " +
                            "(4, 1, 3, 5, 5);");

                    stmt.execute("INSERT INTO question_conditions (question_id, condition_id, sub_index, description) VALUES " +
                            "(1, 1, 1, '1-2藍色大於1'), " +
                            "(1, 5, 1, '5-2藍色是奇數'), " +
                            "(1, 6, 0, '6-1黃色是偶數'), " +
                            "(1, 23, 1, '23-2密碼總和等於6'), " +
                            "(2, 2, 2, '2-3藍色大於3'), " +
                            "(2, 3, 2, '3-3黃色大於3'), " +
                            "(2, 10, 2, '10-2密碼包含2個數字4'), " +
                            "(2, 13, 1, '13-1黃色小於紫色'), " +
                            "(3, 2, 0, '2-1藍色小於3'), " +
                            "(3, 4, 2, '4-3紫色大於4'), " +
                            "(3, 10, 1, '10-1密碼包含1個數字4'), " +
                            "(3, 12, 0, '12-1藍色小於紫色'), " +
                            "(3, 16, 2, '16-2密碼包含2個偶數'), " +
                            "(4, 1, 0, '1-1藍色等於1'), " +
                            "(4, 3, 1, '3-2黃色等於3'), " +
                            "(4, 7, 1, '7-2紫色是奇數'), " +
                            "(4, 12, 0, '12-1藍色小於紫色'), " +
                            "(4, 15, 2, '15-3紫色最大');");
                }
            }

            initialized = true;
            System.out.println("✨ Turing Machine Database auto-initialized successfully!");
        } catch (SQLException e) {
            System.err.println("⚠️ Warning during DB auto-init: " + e.getMessage());
        }
    }

    /**
     * Gets a connection to the MySQL database.
     * If the database is unreachable or not configured, throws an exception or returns null
     * depending on mode, allowing mock mode fallback in DAO.
     */
    public static Connection getConnection() throws SQLException {
        if (useMockMode) {
            throw new SQLException("Database driver not loaded. Operating in Offline Mode.");
        }
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            initializeDatabase(conn);
            return conn;
        } catch (SQLException e) {
            // Log once and signal DAO to use local fallback
            if (!useMockMode) {
                System.out.println("⚠️ MySQL database unreachable at localhost:3306. Operating in Offline Mode (Records saved to session memory).");
                useMockMode = true;
            }
            throw e;
        }
    }

    /**
     * Check if database utility is currently in offline/mock mode.
     */
    public static boolean isOfflineMode() {
        return useMockMode;
    }

    /**
     * Explicitly set mock mode (useful for testing or local bypass).
     */
    public static void setOfflineMode(boolean offline) {
        useMockMode = offline;
    }
}
