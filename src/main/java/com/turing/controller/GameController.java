package com.turing.controller;

import com.turing.dao.QuestionDao;
import com.turing.dao.impl.QuestionDaoImpl;
import com.turing.model.Code;
import com.turing.model.GameRecord;
import com.turing.model.GameResultDto;
import com.turing.model.Puzzle;
import com.turing.model.User;
import com.turing.model.VerifierCard;
import com.turing.service.GameService;
import com.turing.service.impl.GameServiceImpl;
import com.turing.util.PuzzleGenerator; // 確保引入產生器

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class coordinating interactions between Swing views and the Game Service.
 * (已完美整合大腦搬遷：掌管隨機出題與序號還原的核心業務邏輯)
 */
public class GameController {
    
    private final GameService gameService;
    private Puzzle currentPuzzle;
    
    // Gameplay stats
    private int currentRound;
    private int testsThisRound;
    private int totalTests;
    private String playerName = "Agent Player";

    // Notepad state: column reference (B, Y, P) -> (value 1-5 -> state: 0=normal, 1=circled, 2=crossed)
    private final Map<Character, Map<Integer, Integer>> notepadState;

    
    public GameController() {
        this.gameService = new GameServiceImpl();
        this.notepadState = new HashMap<>();
        resetNotepad();
    }

    /**
     * 🧠 業務邏輯搬遷：隨機出題並同步設定賽局
     */
    public String generateAndSetupRandomGame(Connection conn, List<PuzzleGenerator.ActiveCondition> cachedConditions, List<int[]> cachedCodes, char difficulty, int kCount) throws Exception {
        // 🟢 確保傳入的 difficulty ('A', 'B', 'C') 完美傳進產生器中
        PuzzleGenerator.PuzzleResult dynamicPuzzle = 
            PuzzleGenerator.generatePuzzle(conn, cachedConditions, cachedCodes, difficulty, kCount);
        
        // 同步覆寫至資料庫 (id=1)
        PuzzleGenerator.overwriteLivePuzzle(conn, dynamicPuzzle);
        
        // 鎖定記憶體賽局
        this.currentPuzzle = new Puzzle(
            dynamicPuzzle.puzzleCode, "Custom", 
            new Code(dynamicPuzzle.blueAns, dynamicPuzzle.yellowAns, dynamicPuzzle.purpleAns)
        );
        
        // 初始化局勢
        this.currentRound = 1;
        this.testsThisRound = 0;
        this.totalTests = 0;
        resetNotepad();
        
        return dynamicPuzzle.puzzleCode;
    }

    /**
     * 🧠 業務邏輯搬遷：輸入編號載入並同步設定賽局
     */
    public void loadAndSetupGameByCode(Connection conn, String inputCode, List<PuzzleGenerator.ActiveCondition> cachedConditions) throws Exception {
        // 1. 呼叫反向解碼與還原洗牌
        PuzzleGenerator.PuzzleResult dynamicPuzzle = 
            PuzzleGenerator.decodePuzzleCode(inputCode, cachedConditions);
        
        // 2. 同步覆寫至資料庫 (id=1)
        PuzzleGenerator.overwriteLivePuzzle(conn, dynamicPuzzle);
        
        // 3. 鎖定記憶體賽局
        this.currentPuzzle = new Puzzle(
            "1", "Custom", 
            new Code(dynamicPuzzle.blueAns, dynamicPuzzle.yellowAns, dynamicPuzzle.purpleAns)
        );
        
        // 4. 初始化局勢
        this.currentRound = 1;
        this.testsThisRound = 0;
        this.totalTests = 0;
        resetNotepad();
    }

    /**
     * 直接從產生器載入現場生好的完美題目，跳過 Service 重複檢索資料庫的死角
     * @param livePuzzle 記憶體中現場建構完畢的 Puzzle 物件
     * @param player 玩家名稱
     */
    public void startNewGameWithLivePuzzle(Puzzle livePuzzle, String player) {
        if (player != null && !player.trim().isEmpty()) {
            this.playerName = player.trim();
        }
        this.currentPuzzle = livePuzzle; 
        this.currentRound = 1;
        this.testsThisRound = 0;
        this.totalTests = 0;
        resetNotepad(); 
    }

    @Deprecated
    public void startNewGame(String difficulty, String player) {
        throw new UnsupportedOperationException("請改用 startNewGameWithLivePuzzle 或大腦方法！");
    }

    @Deprecated
    public void startNewGameFromBank(int kCount, String player) {
        throw new UnsupportedOperationException("請改用 startNewGameWithLivePuzzle 或大腦方法！");
    }

    public Puzzle getCurrentPuzzle() { return currentPuzzle; }
    public int getCurrentRound() { return currentRound; }
    public int getTestsThisRound() { return testsThisRound; }
    public int getTotalTests() { return totalTests; }
    public String getPlayerName() { return playerName; }

    public void nextRound() {
        currentRound++;
        testsThisRound = 0;
    }

    public boolean testVerifier(Code proposal, int cardId) {
        if (currentPuzzle == null) throw new IllegalStateException("No active game in progress.");
        boolean result = gameService.verifyProposal(proposal, cardId, currentPuzzle.getSecretCode());
        testsThisRound++;
        totalTests++;
        return result;
    }

    /**
     * Submits a final code guess to solve the puzzle.
     * Logs the play record in the database.
     * @return true if correct code, false otherwise.
     */
    public boolean submitSolution(Code proposal) {
        if (currentPuzzle == null) {
            return false;
        }

        boolean isCorrect = gameService.checkWin(proposal, currentPuzzle.getSecretCode());
        
        // 🟢 完美對齊：依序傳入 (String playerName, String puzzleId, int roundsUsed, int testsUsed, String secretCode, boolean won)
        GameRecord record = new GameRecord(
            playerName,                                 // 玩家名稱
            currentPuzzle.getPuzzleId(),                // puzzleId
            currentRound,                               // roundsUsed (本局使用的回合數)
            totalTests,                                 // testsUsed (總測試次數)
            currentPuzzle.getSecretCode().toString(),   // secretCode (答案密碼字串)
            isCorrect                                   // won (是否成功解出)
        );

        gameService.saveRecord(record);
        
        return isCorrect;
    }

    public List<GameRecord> getHistory() { return gameService.getHistory(); }
    public void clearHistory() { gameService.clearHistory(); }

    private void resetNotepad() {
        notepadState.clear();
        for (char col : new char[]{'B', 'Y', 'P'}) {
            Map<Integer, Integer> colMap = new HashMap<>();
            for (int val = 1; val <= 5; val++) colMap.put(val, 0);
            notepadState.put(col, colMap);
        }
    }

    public int getNotepadValue(char col, int val) { return notepadState.get(col).get(val); }
    public void cycleNotepadValue(char col, int val) {
        Map<Integer, Integer> colMap = notepadState.get(col);
        int currentState = colMap.get(val);
        colMap.put(val, (currentState + 1) % 3);
    }
    
    private final QuestionDao questionDao = new QuestionDaoImpl();

    public List<VerifierCard> getActiveVerifierCardsForLiveGame() {
        return questionDao.getLiveVerifierCards();
    }
    
    public int getFinalCardCount(String uiSelectedText) {
        return gameService.determineCardCount(uiSelectedText);
    }
    
    /**
     * 🟢 全新翻新：結算最終密碼並建立結果 DTO
     * @param currentUser 當前玩家
     * @param b 藍色推測值
     * @param y 黃色推測值
     * @param p 紫色推測值
     * @param diffChar 當前賽局難度字元 ('A', 'B', 'C')
     */
    public GameResultDto submitAndBuildResult(User currentUser, int b, int y, int p, char diffChar) {
        // 1. 呼叫底層結算邏輯，這時 Service 會計算出這次拿了多少代幣 (例如 10 枚或 20 枚)
        GameResultDto result = gameService.verifyFinalGuess(this.currentPuzzle, currentUser, this.currentRound, this.totalTests, b, y, p, diffChar);
        
        // 2. 🧠 核心改造：動態拿取真正核發的代幣數量 (未達標或失敗就是 0)
        int actualTokensAwarded = result.isWon() ? result.getRewardTokens() : 0;
        
        // 3. 建立帶有代幣數量的戰績紀錄
        Code proposal = new Code(b, y, p);
        boolean isCorrect = gameService.checkWin(proposal, currentPuzzle.getSecretCode());
        
        GameRecord record = new GameRecord(
            playerName,
            currentPuzzle.getPuzzleId(),
            currentRound,
            totalTests,
            currentPuzzle.getSecretCode().toString(),
            isCorrect,
            actualTokensAwarded // 👈 將代幣數量精準打入戰績物件
        );

        // 4. 直接調用寫入
        gameService.saveRecord(record);
        
        return result;
    }
}