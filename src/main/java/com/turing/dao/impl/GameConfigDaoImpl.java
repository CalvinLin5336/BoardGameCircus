package com.turing.dao.impl;

import com.turing.dao.GameConfigDao;
import com.turing.util.DbUtil;
import com.turing.exception.GameException;
import com.turing.model.GameConfig;

import java.sql.*;
import java.util.ArrayList;

import java.util.List;


/**
 * JDBC implementation of GameConfigDao.
 */
public class GameConfigDaoImpl implements GameConfigDao {

    @Override
    public String getValue(String key) {
        String sql = "SELECT config_value FROM game_config WHERE config_key = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("config_value");
                }
            }
        } catch (SQLException e) {
            throw new GameException("獲取配置失敗 [key=" + key + "]: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void updateValue(String key, String value) {
        String sql = "UPDATE game_config SET config_value = ? WHERE config_key = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.setString(2, key);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameException("更新配置失敗 [key=" + key + ", val=" + value + "]: " + e.getMessage(), e);
        }
    }

    public List<GameConfig> getAllConfigs() throws SQLException {
        List<GameConfig> list = new ArrayList<>();
        String sql = "SELECT * FROM game_config ORDER BY difficulty_char ASC";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new GameConfig(
                    rs.getString("difficulty_char").charAt(0),
                    rs.getInt("base_reward"),
                    rs.getInt("speed_bonus_threshold"),
                    rs.getInt("speed_bonus_amount")
                ));
            }
        }
        return list;
    }

    public void updateConfig(GameConfig config) throws SQLException {
        String sql = "UPDATE game_config SET base_reward = ?, speed_bonus_threshold = ?, speed_bonus_amount = ? WHERE difficulty_char = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, config.getBaseReward());
            pstmt.setInt(2, config.getSpeedBonusThreshold());
            pstmt.setInt(3, config.getSpeedBonusAmount());
            pstmt.setString(4, String.valueOf(config.getDifficultyChar()));
            pstmt.executeUpdate();
        }
    }
    
    /**
     * 讀取特定難度的設定值 (配合你原本的 UserServiceImpl 邏輯)
     * @param diffChar 'A', 'B', 'C'
     * @param columnName "reward_rounds_limit" 或 "reward_tokens"
     */
    public int getConfigValue(char diffChar, String columnName) {
        String sql = "SELECT " + columnName + " FROM game_config WHERE difficulty_char = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, String.valueOf(diffChar));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(columnName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // 發生異常回傳 0 防呆
    }

    /**
     * 更新特定難度的變數設定 (提供給管理後台使用)
     */
    public void updateConfig(char diffChar, int roundsLimit, int tokens) throws SQLException {
        String sql = "UPDATE game_config SET reward_rounds_limit = ?, reward_tokens = ? WHERE difficulty_char = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roundsLimit);
            pstmt.setInt(2, tokens);
            pstmt.setString(3, String.valueOf(diffChar));
            pstmt.executeUpdate();
        }
   
    
    
    
    }
}
