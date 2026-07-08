package com.turing.util;

import com.turing.model.Code;
import com.turing.model.Puzzle;
import com.turing.model.VerifierCard;
import java.sql.Connection;
import java.util.*;

/**
 * 圖靈密碼遊戲核心產生器 - 終極 CSP 唯一解加密編碼整合版 (智慧難度與張數分離過濾版 + 逆向冗餘防排擠優化)
 */
public class PuzzleGenerator {

    // ==========================================
    // 🔒 秘密置換對應表 (100% 隱形洗牌防作弊矩陣)
    // ==========================================
    private static final int[] MAP_12 = {7, 2, 9, 0, 4, 11, 1, 6, 3, 10, 5, 8};
    private static final int[] MAP_15 = {11, 4, 0, 8, 14, 2, 9, 13, 5, 1, 7, 12, 6, 3, 10};
    private static final int[] MAP_18 = {11, 4, 15, 0, 8, 14, 2, 9, 13, 5, 17, 1, 7, 12, 6, 16, 3, 10};

    /**
     * 🔒 【核心混淆器】依據字串實際長度，自動對調字元順序
     */
    public static String shuffleString(String input) {
        int len = input.length();
        int[] map = (len == 12) ? MAP_12 : (len == 15) ? MAP_15 : (len == 18) ? MAP_18 : null;
        if (map == null) return input;
        
        char[] shuffled = new char[len];
        for (int i = 0; i < len; i++) {
            shuffled[map[i]] = input.charAt(i);
        }
        return new String(shuffled);
    }

    /**
     * 🔓 【核心還原器】逆向解開打亂的規律，還原原本的 3 碼連續字串
     */
    public static String unshuffleString(String shuffledInput) {
        int len = shuffledInput.length();
        int[] map = (len == 12) ? MAP_12 : (len == 15) ? MAP_15 : (len == 18) ? MAP_18 : null;
        if (map == null) return shuffledInput;
        
        char[] original = new char[len];
        for (int i = 0; i < len; i++) {
            original[i] = shuffledInput.charAt(map[i]);
        }
        return new String(original);
    }

    /**
     * 📊 智慧輔助方法：幫 1～48 張卡片評定邏輯難度權重分
     */
    private static int getCardWeightScore(int cardId) {
        if (cardId >= 1 && cardId <= 7) return 1;   // 一階直覺卡（絕對值、奇偶）
        if (cardId >= 8 && cardId <= 13) return 2;  // 二階卡（計數、雙色比較）
        if (cardId >= 14 && cardId <= 32) return 3; // 三階全域卡（極值、三色總和、重複性）
        return 4;                                   // #33~#48 地獄大師卡（多判斷、複合連鎖）
    }

// ==========================================
    // 🟢 核心方法：隨機產生完美題目 (外觀權重 + 精確邏輯難度雙重過濾版)
    // ==========================================
    public static PuzzleResult generatePuzzle(
            Connection conn, 
            List<ActiveCondition> allConditions, 
            List<int[]> allCodes, 
            char targetDifficulty, // 傳入 'A'(簡單), 'B'(普通), 'C'(困難)
            int cardCount) throws Exception {

        Random random = new Random();
        
        // 【第一層外觀過濾】設定基本的分數限制區間與大師卡上限
        int minScore = 0;
        int maxScore = 0;
        int maxMasterCards = 0;

        if (targetDifficulty == 'A') {
            minScore = 4;
            maxScore = cardCount * 2 - 2; 
            maxMasterCards = 0; 
        } else if (targetDifficulty == 'C') {
            minScore = cardCount * 3 - 1; 
            maxScore = cardCount * 4;
            maxMasterCards = cardCount;   
        } else { 
            minScore = cardCount * 2 - 1; 
            maxScore = cardCount * 3 - 2;
            maxMasterCards = 1; 
        }

        Map<Integer, List<ActiveCondition>> cardMap = new HashMap<>();
        for (ActiveCondition cond : allConditions) {
            cardMap.computeIfAbsent(cond.cardId, k -> new ArrayList<>()).add(cond);
        }
        List<Integer> availableCardIds = new ArrayList<>(cardMap.keySet());

        while (true) {
            // 🔹 第一步：盲抽玩家指定數量的卡片
            Collections.shuffle(availableCardIds);
            List<Integer> chosenCardIds = new ArrayList<>(availableCardIds.subList(0, cardCount));
            
            // 🔹 第二步：外觀難度審查 (Pass 1 - 卡片權重初篩)
            int totalScore = 0;
            int masterCardCount = 0;
            for (int cardId : chosenCardIds) {
                totalScore += getCardWeightScore(cardId);
                if (cardId >= 33 && cardId <= 48) {
                    masterCardCount++;
                }
            }

            if (totalScore < minScore || totalScore > maxScore || masterCardCount > maxMasterCards) {
                continue;
            }

            Collections.sort(chosenCardIds); 

            // 🔹 第三步：隨機抽出正確答案並抓取對應子條件
            int ansIndex = random.nextInt(allCodes.size());
            int[] secretAns = allCodes.get(ansIndex); 

            List<ActiveCondition> activeConditions = new ArrayList<>();
            for (int cardId : chosenCardIds) {
                List<ActiveCondition> subs = cardMap.get(cardId);
                for (ActiveCondition sub : subs) {
                    if (sub.bitMatrix.get(ansIndex)) {
                        activeConditions.add(sub);
                        break;
                    }
                }
            }

            if (isExcludedByDb(conn, activeConditions)) {
                continue; 
            }

            // 🔹 第四步：使用 BitSet 進行唯一解驗證 (Pass 2)
            BitSet gameIntersect = new BitSet(125);
            gameIntersect.set(0, 125); 

            for (int k = 0; k < activeConditions.size(); k++) {
                gameIntersect.and(activeConditions.get(k).bitMatrix); 
            }

            int trueCount = 0;
            for (int m = 0; m < 125; m++) {
                if (gameIntersect.get(m)) trueCount++;
            }

            // =================================================================
            // 🛡️ 第五步：逆向冗餘檢查 兼 精確邏輯難度計算 (Pass 3 & Pass 4)
            // =================================================================
            if (trueCount == 1 && activeConditions.size() == cardCount) {
                
                boolean isEveryCardEssential = true;
                int totalInterference = 0; // 統計少了單張卡後，多出來的「干擾解總數」
                
                for (int i = 0; i < cardCount; i++) {
                    BitSet testIntersect = new BitSet(125);
                    testIntersect.set(0, 125); 
                    
                    for (int j = 0; j < cardCount; j++) {
                        if (i == j) continue; 
                        testIntersect.and(activeConditions.get(j).bitMatrix);
                    }
                    
                    int testTrueCount = 0;
                    for (int m = 0; m < 125; m++) {
                        if (testIntersect.get(m)) testTrueCount++;
                    }
                    
                    // 🚨 抓到冗餘卡，直接淘汰
                    if (testTrueCount == 1) {
                        isEveryCardEssential = false;
                        break; 
                    }
                    
                    // 📈 精確累加：這張卡被拿掉後多出來的錯誤答案數量（減掉唯一解本身）
                    totalInterference += (testTrueCount - 1);
                }
                
                // 冗餘檢查失敗，直接重抽選
                if (!isEveryCardEssential) {
                    continue;
                }

                // 🧮 計算不含張數權重的「純邏輯平均干擾係數」
                double avgInterference = (double) totalInterference / cardCount;
                
                // 精確判定這道題目的實際推理難度
                char actualLogicDifficulty;
                if (avgInterference <= 1.5) {
                    actualLogicDifficulty = 'A'; // 簡單：拿掉任何卡都只多1~2個錯字，線索非常獨立直覺
                } else if (avgInterference <= 4.0) {
                    actualLogicDifficulty = 'B'; // 普通：線索間有一定交織，拿掉一張會多出不少干擾
                } else {
                    actualLogicDifficulty = 'C'; // 困難：環環相扣，漏掉任何一張，解空間會大爆炸
                }

                // 🏆 最終核心審查：只有外觀權重、唯一解、無冗餘、且【精確邏輯難度】完全吻合玩家選擇時，才正式出題！
                if (actualLogicDifficulty == targetDifficulty) {
                    PuzzleResult result = new PuzzleResult();
                    result.blueAns = secretAns[0];
                    result.yellowAns = secretAns[1];
                    result.purpleAns = secretAns[2];
                    result.cardIds = chosenCardIds;
                    result.activeConditions = activeConditions;
                    
                    // 呼叫序號生成器
                    result.puzzleCode = buildPuzzleCode(targetDifficulty, cardCount, activeConditions);
                    return result;
                }
            }
        }
    }

    /**
     * 🔒 序號生成：固定 3 碼組裝 -> 洗牌混淆 -> 轉 36 進位
     */
    public static String buildPuzzleCode(char difficulty, int cardCount, List<ActiveCondition> activeConditions) {
        StringBuilder sb10 = new StringBuilder();
        
        for (ActiveCondition cond : activeConditions) {
            String cIdStr = String.format("%02d", cond.cardId); // 固定 2 位卡片 ID (例如 01)
            int conditionSubIndex = (cond.conditionIndex % 10) + 1; // 1 位條件 ID
            sb10.append(cIdStr).append(conditionSubIndex);
        }

        // 呼叫混淆器大洗牌
        String shuffled10 = shuffleString(sb10.toString());

        // 轉成高質感 36 進位大字串
        java.math.BigInteger bigNum = new java.math.BigInteger(shuffled10);
        String code36 = bigNum.toString(36).toUpperCase();

        return "#" + difficulty + cardCount + " " + code36;
    }

    // ==========================================
    // 🔓 核心方法：輸入題目編號解碼逆向載入 (Decode 入口)
    // ==========================================
    public static PuzzleResult decodePuzzleCode(String puzzleCode, List<ActiveCondition> allConditions) throws Exception {
        try {
            puzzleCode = puzzleCode.trim();
            String[] tokens = puzzleCode.split("\\s+");
            if (tokens.length < 2) throw new IllegalArgumentException("序號格式不完整");

            String prefix = tokens[0]; // 例如 #C6
            int cardCount = Integer.parseInt(prefix.substring(2));
            int expectedLength = cardCount * 3; // 預期位數

            // 1. 36 進位還原為 10 進位字串
            java.math.BigInteger bigNum = new java.math.BigInteger(tokens[1], 36);
            String shuffled10 = bigNum.toString();
            
            // 2. 補足被吃掉的前導零
            while (shuffled10.length() < expectedLength) {
                shuffled10 = "0" + shuffled10;
            }

            // 3. 🟢 關鍵還原：切 3 碼前解開秘密洗牌規律
            String restored10 = unshuffleString(shuffled10);

            // 4. 每 3 碼切一刀，精準提取卡片條件
            List<ActiveCondition> activeConditions = new ArrayList<>();
            List<Integer> cardIds = new ArrayList<>();

            for (int i = 0; i < restored10.length(); i += 3) {
                int cardId = Integer.parseInt(restored10.substring(i, i + 2));
                int subConditionNum = Integer.parseInt(restored10.substring(i + 2, i + 3));
                
                // 🟢 呼叫查找器精準鎖定快取物件
                ActiveCondition targetCond = findConditionInCache(allConditions, cardId, subConditionNum);
                if (targetCond != null) {
                    activeConditions.add(targetCond);
                    if (!cardIds.contains(cardId)) cardIds.add(cardId);
                }
            }

            // 5. 記憶體 CSP 矩陣重算答案，驗證唯一解並鎖定謎底
            BitSet intersect = new BitSet(125);
            intersect.set(0, 125);
            for (ActiveCondition cond : activeConditions) {
                intersect.and(cond.bitMatrix);
            }

            int trueCount = 0;
            for (int m = 0; m < 125; m++) {
                if (intersect.get(m)) trueCount++;
            }

            if (trueCount != 1) {
                throw new IllegalStateException("該序號交集無法還原出唯一解，可能遭到竄改！");
            }

            int ansIndex = intersect.nextSetBit(0);
            int b = (ansIndex / 25) + 1;
            int y = ((ansIndex % 25) / 5) + 1;
            int p = (ansIndex % 5) + 1;

            PuzzleResult result = new PuzzleResult();
            result.puzzleCode = puzzleCode.toUpperCase();
            result.blueAns = b;
            result.yellowAns = y;
            result.purpleAns = p;
            result.cardIds = cardIds;
            result.activeConditions = activeConditions;
            return result;

        } catch (Exception e) {
            throw new Exception("序號還原失敗！錯誤原因: " + e.getMessage());
        }
    }

    // ==========================================
    // 🛠️ 內部資料查找與防呆輔助方法
    // ==========================================
    private static ActiveCondition findConditionInCache(List<ActiveCondition> allConditions, int cardId, int subNum) {
        for (ActiveCondition cond : allConditions) {
            // 檢查卡片 ID 是否吻合，且映射後的子條件尾數是否一致
            if (cond.cardId == cardId && ((cond.conditionIndex % 10) + 1) == subNum) {
                return cond;
            }
        }
        return null;
    }

    private static boolean isExcludedByDb(Connection conn, List<ActiveCondition> conditions) {
        return false;
    }

    // ==========================================
    // 📦 內部結構實體定義封裝 (Data Models)
    // ==========================================
    public static class ActiveCondition {
        public int cardId;
        public int conditionIndex;
        public BitSet bitMatrix; // 125 位元的布林匹配矩陣
        
        public ActiveCondition(int cardId, int conditionIndex, BitSet bitMatrix) {
            this.cardId = cardId;
            this.conditionIndex = conditionIndex;
            this.bitMatrix = bitMatrix;
        }
    }

    public static class PuzzleResult {
        public String puzzleCode;
        public int blueAns;
        public int yellowAns;
        public int purpleAns;
        public List<Integer> cardIds;
        public List<ActiveCondition> activeConditions;
    }
    
    /**
     * 🟢 完美對齊 Schema 版：將動態生成或解碼的謎題，覆寫進 questions 與 question_conditions (固定 ID = 1)
     */
    public static void overwriteLivePuzzle(Connection conn, PuzzleResult dynamicPuzzle) throws Exception {
        conn.setAutoCommit(false);
        
        String deleteConditionsSql = "DELETE FROM question_conditions WHERE question_id = 1";
        String deleteQuestionSql = "DELETE FROM questions WHERE id = 1";
        
        String insertQuestionSql = "INSERT INTO questions (id, ans_b, ans_y, ans_p, k_count) VALUES (1, ?, ?, ?, ?)";
        String insertConditionSql = "INSERT INTO question_conditions (question_id, condition_id, sub_index, description) VALUES (1, ?, ?, ?)";
        
        try (java.sql.PreparedStatement delCondStmt = conn.prepareStatement(deleteConditionsSql);
             java.sql.PreparedStatement delQuesStmt = conn.prepareStatement(deleteQuestionSql);
             java.sql.PreparedStatement insQuesStmt = conn.prepareStatement(insertQuestionSql);
             java.sql.PreparedStatement insCondStmt = conn.prepareStatement(insertConditionSql)) {
            
            delCondStmt.executeUpdate();
            delQuesStmt.executeUpdate();
            
            insQuesStmt.setInt(1, dynamicPuzzle.blueAns);
            insQuesStmt.setInt(2, dynamicPuzzle.yellowAns);
            insQuesStmt.setInt(3, dynamicPuzzle.purpleAns);
            insQuesStmt.setInt(4, dynamicPuzzle.cardIds.size()); 
            insQuesStmt.executeUpdate();
            
            for (ActiveCondition cond : dynamicPuzzle.activeConditions) {
                insCondStmt.setInt(1, cond.cardId); 
                
                int subNum = (cond.conditionIndex % 10) + 1; 
                insCondStmt.setInt(2, subNum); 
                
                insCondStmt.setString(3, "Dynamic Criteria for Card " + cond.cardId); 
                insCondStmt.executeUpdate();
            }
            
            conn.commit();
        } catch (Exception e) {
            conn.rollback(); 
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}