package com.turing.dao.impl;

import com.turing.dao.RecordDao;
import com.turing.model.GameRecord;
import com.turing.util.DbUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of RecordDao using JDBC to access MySQL 8.0 with memory-fallback.
 * (已完美修復欄位錯位 Bug，打通離線備援與代幣統計電路)
 */
public class RecordDaoImpl implements RecordDao {

    // Fallback in-memory list for local execution without database
    private static final List<GameRecord> fallbackMemoryList = new ArrayList<>();
    private static int fallbackIdSequence = 1;

    @Override
    public void save(GameRecord record) {
        // 如果系統已經因為唯讀錯誤被切到離線模式，直接寫入記憶體
        if (DbUtil.isOfflineMode()) {
            saveToMemory(record);
            return;
        }

        // 🟢 統一欄位名稱：使用 is_won
        String sql = "INSERT INTO game_records (player_name, puzzle_id, rounds_used, tests_used, secret_code, is_won, tokens_rewarded) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getPlayerName());
            pstmt.setString(2, record.getPuzzleId());
            pstmt.setInt(3, record.getRoundsUsed());
            pstmt.setInt(4, record.getTestsUsed());
            pstmt.setString(5, record.getSecretCode());
            pstmt.setBoolean(6, record.isWon());
            pstmt.setInt(7, record.getTokensRewarded()); 
            
            pstmt.executeUpdate();
            System.out.println("✓ 戰績已成功同步寫入 MySQL 資料庫。");
        } catch (SQLException e) {
            System.err.println("🚨 MySQL 寫入失敗！原因：" + e.getMessage() + " 正在自動切換為本地離線記憶體備援...");
            e.printStackTrace();
            // 🟢 核心修正：資料庫壞掉時，一定要記得補救存進記憶體！
            DbUtil.setOfflineMode(true);
            saveToMemory(record);
        }
    }

    private void saveToMemory(GameRecord record) {
        record.setId(fallbackIdSequence++);
        if (record.getPlayedAt() == null) {
            record.setPlayedAt(new Timestamp(System.currentTimeMillis()));
        }
        fallbackMemoryList.add(record);
        System.out.println("✓ Game record saved to offline memory: " + record);
    }

    @Override
    public List<GameRecord> findAll() {
        if (DbUtil.isOfflineMode()) {
            return new ArrayList<>(fallbackMemoryList);
        }

        List<GameRecord> list = new ArrayList<>();
        // 🟢 核心修正：將錯誤的 won 修改為正確的 is_won，並在結尾補上 tokens_rewarded 的檢索
        String sql = "SELECT id, player_name, puzzle_id, rounds_used, tests_used, secret_code, is_won, tokens_rewarded, played_at " +
                     "FROM game_records ORDER BY played_at DESC";

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                GameRecord record = new GameRecord();
                record.setId(rs.getInt("id"));
                record.setPlayerName(rs.getString("player_name"));
                record.setPuzzleId(rs.getString("puzzle_id"));
                record.setRoundsUsed(rs.getInt("rounds_used"));
                record.setTestsUsed(rs.getInt("tests_used"));
                record.setSecretCode(rs.getString("secret_code"));
                record.setWon(rs.getBoolean("is_won")); // 👈 完美對齊 is_won
                record.setTokensRewarded(rs.getInt("tokens_rewarded")); // 👈 完美撈出代幣
                record.setPlayedAt(rs.getTimestamp("played_at"));
                list.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Database read error. Reason: " + e.getMessage() + " -> Pulling history from local session memory.");
            DbUtil.setOfflineMode(true);
            return new ArrayList<>(fallbackMemoryList);
        }
        return list;
    }

    @Override
    public void deleteAll() {
        fallbackMemoryList.clear();
        if (DbUtil.isOfflineMode()) {
            return;
        }

        String sql = "DELETE FROM game_records";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✓ Cleared all records in MySQL database.");
        } catch (SQLException e) {
            System.err.println("Database clear error.");
            DbUtil.setOfflineMode(true);
        }
    }
}