package com.turing.dao.impl;


import com.turing.dao.ReportDao;
import com.turing.util.DbUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDaoImpl implements ReportDao {

    @Override
    public Date getLatestReportDate() throws SQLException {
        String sql = "SELECT MAX(summary_date) FROM daily_platform_summary";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDate(1); // 取得最新的一筆日期
            }
        }
        return null;
    }

    @Override
    public void generateDailySummary(Date targetDate) throws SQLException {
        String sql = "INSERT INTO daily_platform_summary " +
                     "(summary_date, new_users_count, active_users_count, total_games_played, tokens_minted_game, " +
                     " easy_games, easy_wins, std_games, std_wins, hard_games, hard_wins, " +
                     " easy_reward_wins, std_reward_wins, hard_reward_wins) " + // 👈 補上新欄位
                     "VALUES (?, " +
                     "  (SELECT COUNT(*) FROM users WHERE DATE(created_at) = ?), " + 
                     "  (SELECT COUNT(DISTINCT player_name) FROM game_records WHERE DATE(played_at) = ?), " + 
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ?), " + 
                     "  (SELECT IFNULL(SUM(tokens_rewarded), 0) FROM game_records WHERE DATE(played_at) = ?), " +
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ? AND puzzle_id LIKE '#A%'), " +
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ? AND puzzle_id LIKE '#A%' AND is_won = 1), " +
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ? AND puzzle_id LIKE '#B%'), " +
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ? AND puzzle_id LIKE '#B%' AND is_won = 1), " +
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ? AND puzzle_id LIKE '#C%'), " +
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ? AND puzzle_id LIKE '#C%' AND is_won = 1), " +
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ? AND puzzle_id LIKE '#A%' AND is_won = 1 AND tokens_rewarded > 0), " + // 👈 A拿幣數
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ? AND puzzle_id LIKE '#B%' AND is_won = 1 AND tokens_rewarded > 0), " + // 👈 B拿幣數
                     "  (SELECT COUNT(*) FROM game_records WHERE DATE(played_at) = ? AND puzzle_id LIKE '#C%' AND is_won = 1 AND tokens_rewarded > 0) " +  // 👈 C拿幣數
                     ") ON DUPLICATE KEY UPDATE summary_date = summary_date"; 

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 14; i++) { // 💡 迴圈上限改為 14 次填入日期參數
                pstmt.setDate(i, targetDate);
            }
            pstmt.executeUpdate();
            System.out.println("📊 [報表系統] 已成功生成 " + targetDate + " 包含通關率與拿幣率的完整精細報表。");
        }
    }

    public List<Object[]> getDailyReports() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT summary_date, new_users_count, active_users_count, total_games_played, tokens_minted_game, " +
                     "easy_games, easy_wins, std_games, std_wins, hard_games, hard_wins, " +
                     "easy_reward_wins, std_reward_wins, hard_reward_wins " +
                     "FROM daily_platform_summary ORDER BY summary_date DESC LIMIT 365";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getDate("summary_date").toString(), rs.getInt("new_users_count"), rs.getInt("active_users_count"),
                    rs.getInt("total_games_played"), rs.getInt("tokens_minted_game"),
                    rs.getInt("easy_games"), rs.getInt("easy_wins"), rs.getInt("std_games"), rs.getInt("std_wins"), rs.getInt("hard_games"), rs.getInt("hard_wins"),
                    rs.getInt("easy_reward_wins"), rs.getInt("std_reward_wins"), rs.getInt("hard_reward_wins") // [11], [12], [13]
                });
            }
        }
        return list;
    }

    public List<Object[]> getMonthlyReports() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(summary_date, '%Y-%m') AS month, " +
                     "SUM(new_users_count) AS new_users, SUM(active_users_count) AS active_users, " +
                     "SUM(total_games_played) AS total_games, SUM(tokens_minted_game) AS total_tokens, " +
                     "SUM(easy_games) AS eg, SUM(easy_wins) AS ew, SUM(std_games) AS sg, SUM(std_wins) AS sw, SUM(hard_games) AS hg, SUM(hard_wins) AS hw, " +
                     "SUM(easy_reward_wins) AS erw, SUM(std_reward_wins) AS srw, SUM(hard_reward_wins) AS hrw " +
                     "FROM daily_platform_summary GROUP BY month ORDER BY month DESC";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("month"), rs.getInt("new_users"), rs.getInt("active_users"),
                    rs.getInt("total_games"), rs.getInt("total_tokens"),
                    rs.getInt("eg"), rs.getInt("ew"), rs.getInt("sg"), rs.getInt("sw"), rs.getInt("hg"), rs.getInt("hw"),
                    rs.getInt("erw"), rs.getInt("srw"), rs.getInt("hrw") // [11], [12], [13]
                });
            }
        }
        return list;
    }
    
    
}