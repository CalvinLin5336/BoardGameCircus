package com.turing.dao.impl;

import com.turing.dao.QuestionDao;
import com.turing.model.Code;
import com.turing.model.Puzzle;
import com.turing.model.VerifierCard;
import com.turing.util.DbUtil;
import com.turing.exception.GameException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of QuestionDao querying strictly from MySQL database.
 */
public class QuestionDaoImpl implements QuestionDao {

    @Override
    public Optional<Puzzle> getRandomPuzzle(int kCount) {
        String questionSql;
        if (kCount > 0) {
            // MySQL 8.0 query for random row
            questionSql = "SELECT id, ans_b, ans_y, ans_p, k_count FROM questions WHERE k_count = ? ORDER BY RAND() LIMIT 1";
        } else {
            questionSql = "SELECT id, ans_b, ans_y, ans_p, k_count FROM questions ORDER BY RAND() LIMIT 1";
        }

        try (Connection conn = DbUtil.getConnection()) {
            int qId = -1;
            int ansB = -1, ansY = -1, ansP = -1, realK = -1;
            
            try (PreparedStatement pstmt = conn.prepareStatement(questionSql)) {
                if (kCount > 0) {
                    pstmt.setInt(1, kCount);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        qId = rs.getInt("id");
                        ansB = rs.getInt("ans_b");
                        ansY = rs.getInt("ans_y");
                        ansP = rs.getInt("ans_p");
                        realK = rs.getInt("k_count");
                    }
                }
            }

            if (qId == -1) {
                return Optional.empty();
            }

            // Create Puzzle
            String difficultyStr = realK + "-張卡片版 (#" + qId + ")";
            Puzzle puzzle = new Puzzle(String.valueOf(qId), difficultyStr, new Code(ansB, ansY, ansP));

            // Load Conditions/Verifiers
            String condSql = "SELECT condition_id, description FROM question_conditions WHERE question_id = ? ORDER BY id ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(condSql)) {
                pstmt.setInt(1, qId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int cardId = rs.getInt("condition_id");
                        String desc = rs.getString("description");
                        String title = getTitleForCard(cardId);
                        puzzle.addVerifier(new VerifierCard(cardId, title, desc));
                    }
                }
            }

            return Optional.of(puzzle);

        } catch (SQLException e) {
            throw new GameException("資料庫連線失敗，無法取得題目！請確認 MySQL 伺服器已正常啟動並已載入 schema.sql 題庫！", e);
        }
    }

    private String getTitleForCard(int cardId) {
        switch (cardId) {
            case 1: return "藍色與1比較";
            case 2: return "藍色與3比較";
            case 3: return "黃色與3比較";
            case 4: return "紫色與4比較";
            case 5: return "藍色奇偶性";
            case 6: return "黃色奇偶性";
            case 7: return "紫色奇偶性";
            case 8: return "數字1的數量";
            case 9: return "數字3的數量";
            case 10: return "數字4的數量";
            case 11: return "藍色與黃色比較";
            case 12: return "藍色與紫色比較";
            case 13: return "黃色與紫色比較";
            case 14: return "尋找最小值";
            case 15: return "尋找最大值";
            case 16: return "偶數的數量";
            case 17: return "奇數的數量";
            case 18: return "密碼總和奇偶性";
            case 19: return "藍黃相加與6比較";
            case 20: return "重複數字個數";
            case 21: return "是否恰有重疊";
            case 22: return "密碼升降排序";
            case 23: return "密碼總和與6比較";
            default: return "驗證器 #" + cardId;
        }
    }

    @Override
    public List<VerifierCard> getLiveVerifierCards() {
        List<VerifierCard> cards = new ArrayList<>();
        // 固定查詢 question_id = 1 的當前關卡資訊
        String sql = "SELECT condition_id, description FROM question_conditions WHERE question_id = 1 ORDER BY id ASC";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int cardId = rs.getInt("condition_id");
                String desc = rs.getString("description");
                
                // 🟢 完美借用：直接調用你原本寫在 DAO 底部的 getTitleForCard 方法！
                String title = getTitleForCard(cardId); 
                
                cards.add(new VerifierCard(cardId, title, desc));
            }
        } catch (SQLException e) {
            // 遵循你專案的異常風格
            throw new com.turing.exception.GameException("無法加載當前進行中的驗證卡片明細！", e);
        }
        return cards;
    }
}
