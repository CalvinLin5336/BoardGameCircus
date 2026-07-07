package com.turing.service.impl;

import com.turing.dao.UserDao;
import com.turing.dao.GameConfigDao;
import com.turing.dao.impl.UserDaoImpl;
import com.turing.dao.impl.GameConfigDaoImpl;
import com.turing.model.User;
import com.turing.service.UserService;
import com.turing.exception.GameException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service implementation for managing Users and Game configurations.
 * 完美連動全新二維 RDBMS 結構的 game_config 資料表
 */
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final GameConfigDaoImpl configDao; // 更改為實作型別以調用全新擴充的難度欄位方法

    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
        this.configDao = new GameConfigDaoImpl();
    }

    @Override
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new GameException("帳號或密碼不得為空！");
        }
        
        Optional<User> opt = userDao.findByUsername(username.trim());
        if (!opt.isPresent()) {
            throw new GameException("該帳號不存在！");
        }

        User user = opt.get();
        if (user.isBlocked()) {
            throw new GameException("您的帳號已被管理員封鎖！如有疑問，請聯絡系統管理員。");
        }

        if (!user.getPassword().equals(password)) {
            throw new GameException("密碼輸入錯誤，請重新確認！");
        }

        return user;
    }

    @Override
    public User register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new GameException("帳號或密碼不得為空！");
        }

        String trimmedUser = username.trim();
        if (userDao.findByUsername(trimmedUser).isPresent()) {
            throw new GameException("該帳號已存在！請選擇其他名稱註冊。");
        }

        User newUser = new User();
        newUser.setUsername(trimmedUser);
        newUser.setPassword(password);
        newUser.setRole("USER");
        newUser.setTokens(10); // Start with 10 complimentary tokens
        newUser.setBlocked(false);

        userDao.create(newUser);
        return newUser;
    }

    @Override
    public void createUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new GameException("帳號不得為空！");
        }
        userDao.create(user);
    }

    @Override
    public void updateUser(User user) {
        userDao.update(user);
    }

    @Override
    public void deleteUser(int userId) {
        userDao.delete(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public void adjustUserTokens(int userId, int tokens) {
        userDao.updateTokens(userId, tokens);
    }

    @Override
    public void toggleUserBlockStatus(int userId, boolean blocked) {
        userDao.updateBlockedStatus(userId, blocked);
    }

    @Override
    public List<User> getLeaderboard() {
        return userDao.getLeaderboard();
    }

    @Override
    public Map<String, String> getGameConfigs() {
        // 💡 提示：如果你的管理後台有用到此舊方法，建議配合新 DAO 實作轉換，或暫時保留
        // 由於底層表格已 Drop 掉改為橫向欄位，此舊方法可回傳空 Map 或進行對應重構
        return new java.util.HashMap<>();
    }



    /**
     * 🟢 舊方法多載相容 (不破壞原先未帶難度參數的呼叫點)
     * 預設以 'B' (標準模式) 進行獎勵判定
     */
    @Override
    public int rewardTokensIfEligible(int userId, int roundsUsed) {
        return rewardTokensIfEligible(userId, roundsUsed, 'B');
    }

    /**
     * 🟢 全新核心商務邏輯：依據目前挑戰的「賽局難度」，動態自資料庫過濾對應的極速發放條件
     * 
     * @param userId       玩家 ID
     * @param roundsUsed   本次通關消耗輪次
     * @param difficultyChar 當前賽局難度代號 ('A':簡單, 'B':標準, 'C':困難)
     * @return 實際核發的獎勵代幣數量 (若未達標則回傳 0)
     */
    public int rewardTokensIfEligible(int userId, int roundsUsed, char difficultyChar) {
        // 1. 設定安全防呆預設值 (萬一資料庫連線或讀取失敗時的保險機制)
        int limit = 5;
        int award = 10;
        
        if (difficultyChar == 'A') { limit = 3; award = 5; }
        else if (difficultyChar == 'C') { limit = 7; award = 20; }

        // 2. 🧠 動態向全新的 GameConfig 讀取橫向欄位配置
        try {
            int dbLimit = configDao.getConfigValue(difficultyChar, "reward_rounds_limit");
            int dbAward = configDao.getConfigValue(difficultyChar, "reward_tokens");
            
            // 只要從資料庫有成功撈到大於 0 的設定值，便即時蓋過程式碼預設值
            if (dbLimit > 0) limit = dbLimit;
            if (dbAward > 0) award = dbAward;
            
        } catch (Exception e) {
            System.err.println("🚨 [Service] 讀取難度 " + difficultyChar + " 的動態獎勵配置失敗，啟用程式碼安全預設值! 錯誤: " + e.getMessage());
        }

        // 3. 🎯 核心業務規章比對：消耗輪次小於等於資料庫設定的門檻，即符合發放資格
        if (roundsUsed <= limit) {
            List<User> users = userDao.findAll();
            for (User u : users) {
                if (u.getId() == userId) {
                    int updatedTokens = u.getTokens() + award;
                    userDao.updateTokens(userId, updatedTokens);
                    u.setTokens(updatedTokens); // 同步記憶體物件狀態
                    return award;
                }
            }
        }
        return 0; // 未達極速門檻，回傳 0 枚代幣
    }
}