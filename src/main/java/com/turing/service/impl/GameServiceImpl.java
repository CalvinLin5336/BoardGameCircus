package com.turing.service.impl;

import com.turing.dao.RecordDao;
import com.turing.dao.QuestionDao;
import com.turing.dao.impl.RecordDaoImpl;
import com.turing.dao.impl.QuestionDaoImpl;
import com.turing.model.Code;
import com.turing.model.GameRecord;
import com.turing.model.Puzzle;
import com.turing.model.VerifierCard;
import com.turing.model.GameResultDto;
import com.turing.service.GameService;
import com.turing.model.User;

import java.util.List;
import java.util.Random;

/**
 * Implementation of GameService orchestrating game state and DAO operations.
 * 完美串接三大難度動態獎勵結算
 */
public class GameServiceImpl implements GameService {

    private final RecordDao recordDao;
    private final QuestionDao questionDao;

    public GameServiceImpl() {
        this.recordDao = new RecordDaoImpl();
        this.questionDao = new QuestionDaoImpl();
    }

    @Override
    public Puzzle createRandomPuzzleFromBank(int kCount) {
        return questionDao.getRandomPuzzle(kCount).orElseGet(() -> {
            Puzzle fallback = new Puzzle("F100-Offline", "4-Card Standard", new Code(3, 2, 1));
            fallback.addVerifier(new VerifierCard(1, "1-2 藍色大小", "1-2 藍色大於 1"));
            fallback.addVerifier(new VerifierCard(5, "5-2 藍色奇偶", "5-2 藍色是奇數"));
            fallback.addVerifier(new VerifierCard(6, "6-1 黃色奇偶", "6-1 黃色是偶數"));
            fallback.addVerifier(new VerifierCard(23, "23-2 密碼總和", "23-2 密碼總和等於 6"));
            return fallback;
        });
    }

    @Override
    public Puzzle createNewPuzzle(String difficulty) {
        Random rand = new Random();
        String puzzleId;
        Code secret;
        Puzzle puzzle;

        if ("Easy".equalsIgnoreCase(difficulty)) {
            puzzleId = "E" + (100 + rand.nextInt(900));
            secret = new Code(3, 1, 4);
            puzzle = new Puzzle(puzzleId, "Easy", secret);
            
            puzzle.addVerifier(new VerifierCard(2, "Compare Blue with 3", "Tests if Blue is < 3, == 3, or > 3."));
            puzzle.addVerifier(new VerifierCard(3, "Compare Yellow with 3", "Tests if Yellow is < 3, == 3, or > 3."));
            puzzle.addVerifier(new VerifierCard(6, "Yellow parity", "Tests if Yellow is even or odd."));
            puzzle.addVerifier(new VerifierCard(11, "Compare Blue with Yellow", "Tests if Blue is <, ==, or > Yellow."));
            
        } else if ("Hard".equalsIgnoreCase(difficulty)) {
            puzzleId = "H" + (100 + rand.nextInt(900));
            secret = new Code(5, 2, 5);
            puzzle = new Puzzle(puzzleId, "Hard", secret);
            
            puzzle.addVerifier(new VerifierCard(5, "Blue parity", "Tests if Blue is even or odd."));
            puzzle.addVerifier(new VerifierCard(8, "Count 1s", "Counts how many digits in the code are exactly 1."));
            puzzle.addVerifier(new VerifierCard(11, "Compare Blue with Yellow", "Tests if Blue is <, ==, or > Yellow."));
            puzzle.addVerifier(new VerifierCard(13, "Compare Yellow with Purple", "Tests if Yellow is <, ==, or > Purple."));
            puzzle.addVerifier(new VerifierCard(14, "Unique Smallest", "Identifies which digit is uniquely smallest (or tie)."));
            puzzle.addVerifier(new VerifierCard(15, "Unique Largest", "Identifies which digit is uniquely largest (or tie)."));
            
        } else { // Standard
            puzzleId = "S" + (100 + rand.nextInt(900));
            secret = new Code(2, 4, 5);
            puzzle = new Puzzle(puzzleId, "Standard", secret);
            
            puzzle.addVerifier(new VerifierCard(2, "Compare Blue with 3", "Tests if Blue is < 3, == 3, or > 3."));
            puzzle.addVerifier(new VerifierCard(4, "Compare Purple with 4", "Tests if Purple is < 4, == 4, or > 4."));
            puzzle.addVerifier(new VerifierCard(10, "Count 4s", "Counts how many digits in the code are exactly 4."));
            puzzle.addVerifier(new VerifierCard(12, "Compare Blue with Purple", "Tests if Blue is <, ==, or > Purple."));
            puzzle.addVerifier(new VerifierCard(16, "Count Even Digits", "Counts how many digits in the code are even (0-3)."));
        }

        return puzzle;
    }

    @Override
    public boolean verifyProposal(Code proposal, int verifierId, Code secret) {
        VerifierCard card = new VerifierCard(verifierId, "", "");
        return card.verify(proposal, secret);
    }

    @Override
    public boolean checkWin(Code proposal, Code secret) {
        return proposal.equals(secret);
    }

    @Override
    public void saveRecord(GameRecord record) {
        recordDao.save(record);
    }

    @Override
    public List<GameRecord> getHistory() {
        return recordDao.findAll();
    }

    @Override
    public void clearHistory() {
        recordDao.deleteAll();
    }

    @Override
    public int determineCardCount(String uiSelectedText) {
        if (uiSelectedText == null || "全部隨機".equals(uiSelectedText)) {
            return new java.util.Random().nextBoolean() ? (new java.util.Random().nextBoolean() ? 4 : 5) : 6;
        }
        return Integer.parseInt(uiSelectedText.replace(" 張卡片", "").trim());
    }

    /**
     * 🟢 唯一保留：完美符合新介面合約的動態難度結算機制
     */
    @Override
    public com.turing.model.GameResultDto verifyFinalGuess(
        com.turing.model.Puzzle activePuzzle, 
        com.turing.model.User currentUser, 
        int currentRound, 
        int totalTests, 
        int b, int y, int p, 
        char diffChar // 👈 接收前端與 Controller 遞交過來的難度字元
    ) {
        // 勝負結局判定
        boolean won = activePuzzle.getSecretCode().getBlue() == b 
                   && activePuzzle.getSecretCode().getYellow() == y 
                   && activePuzzle.getSecretCode().getPurple() == p;
        
        com.turing.model.Code secretCode = activePuzzle.getSecretCode();
        String answerRevealMsg = String.format("[ 藍 %d - 黃 %d - 紫 %d ]", secretCode.getBlue(), secretCode.getYellow(), secretCode.getPurple());

        // 動態難度代幣核發
        int reward = 0;
        if (won && currentUser != null && currentUser.getId() != -1) {
            try {
                // 💡 調用你剛剛改造完、對齊全新橫向欄位資料表的 UserServiceImpl 核心
                com.turing.service.UserService userService = new com.turing.service.impl.UserServiceImpl();
                reward = userService.rewardTokensIfEligible(currentUser.getId(), currentRound, diffChar); // 👈 完美帶入
            } catch (Exception e) {
                System.err.println("發放獎勵代幣失敗: " + e.getMessage());
            }
        }

        // 📢 攤牌公告文字組裝
        StringBuilder msg = new StringBuilder();
        if (won) {
            msg.append("【解碼成功】恭喜你！推理正確！ \n");
            msg.append("你所推測的密碼與正確答案完全吻合： ").append(answerRevealMsg).append("\n\n");
            msg.append("本次消耗輪次： ").append(currentRound).append(" 輪\n");
            msg.append("累計驗證測試： ").append(totalTests).append(" 次\n");
            if (reward > 0) {
                msg.append("達成極速解答獎勵！額外獲得了 ").append(reward).append(" 枚代幣！");
            }
        } else {
            msg.append("【解碼失敗】推理遺憾出錯！\n");
            msg.append("你最終提交的猜測是： [ 藍 ").append(b).append(" - 黃 ").append(y).append(" - 紫 ").append(p).append(" ]\n");
            msg.append("本局密碼機隱藏的真正解答是： ").append(answerRevealMsg).append("\n\n");
            msg.append("核心紀錄：本次在第 ").append(currentRound).append(" 輪終止，累計進行了 ").append(totalTests).append(" 次卡片測試。\n");
            msg.append("戰績已上傳至歷史紀錄，調整思維再來一局吧！");
        }

        return new com.turing.model.GameResultDto(won, msg.toString(), reward);
    }
}