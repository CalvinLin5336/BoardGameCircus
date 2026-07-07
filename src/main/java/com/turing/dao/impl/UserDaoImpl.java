package com.turing.dao.impl;

import com.turing.dao.UserDao;
import com.turing.model.User;
import com.turing.util.DbUtil;
import com.turing.exception.GameException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserDao interfacing with MySQL 8.0.
 */
public class UserDaoImpl implements UserDao {

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, role, tokens, is_blocked FROM users WHERE username = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getInt("tokens"),
                        rs.getBoolean("is_blocked")
                    );
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new GameException("查詢用戶失敗: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean authenticate(String username, String password) {
        Optional<User> opt = findByUsername(username);
        if (opt.isPresent()) {
            User user = opt.get();
            if (user.isBlocked()) {
                throw new GameException("該帳號已被封鎖，無法登入！");
            }
            return user.getPassword().equals(password);
        }
        return false;
    }

    @Override
    public void create(User user) {
        String sql = "INSERT INTO users (username, password, role, tokens, is_blocked) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.getTokens());
            pstmt.setBoolean(5, user.isBlocked());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new GameException("新增用戶失敗，可能帳號已存在: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, role = ?, tokens = ?, is_blocked = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.getTokens());
            pstmt.setBoolean(5, user.isBlocked());
            pstmt.setInt(6, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameException("更新用戶失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameException("刪除用戶失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, password, role, tokens, is_blocked FROM users ORDER BY id ASC";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getInt("tokens"),
                    rs.getBoolean("is_blocked")
                ));
            }
        } catch (SQLException e) {
            throw new GameException("獲取所有用戶列表失敗: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void updateTokens(int id, int tokens) {
        String sql = "UPDATE users SET tokens = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tokens);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameException("更新代幣失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateBlockedStatus(int id, boolean blocked) {
        String sql = "UPDATE users SET is_blocked = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, blocked);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameException("更新封鎖狀態失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getLeaderboard() {
        List<User> list = new ArrayList<>();
        // Rank active (unblocked) players by tokens descending
        String sql = "SELECT id, username, '', role, tokens, is_blocked FROM users WHERE is_blocked = FALSE ORDER BY tokens DESC LIMIT 10";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    "",
                    rs.getString("role"),
                    rs.getInt("tokens"),
                    rs.getBoolean("is_blocked")
                ));
            }
        } catch (SQLException e) {
            throw new GameException("獲取排行榜失敗: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<User> getTopRichUsers() throws Exception {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, role, tokens FROM users " +
                "WHERE role != 'ADMIN' " + 
                "ORDER BY tokens DESC LIMIT 10";
        
        try (Connection conn = DbUtil.getConnection(); // 完美使用你專案的 DbUtil
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    null, // 密碼安全隱藏
                    rs.getString("role"),
                    rs.getInt("tokens"),
                    false
                ));
            }
        }
        return list;
    }
}
