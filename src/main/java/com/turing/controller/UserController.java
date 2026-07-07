package com.turing.controller;

import com.turing.dao.UserDao;
import com.turing.dao.impl.UserDaoImpl;
import com.turing.model.User;
import com.turing.service.UserService;
import com.turing.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * Controller to bridge swing login/management views with the UserService.
 */
public class UserController {
    private final UserService userService;

    public UserController() {
        this.userService = new UserServiceImpl();
    }

    public User login(String username, String password) {
        return userService.login(username, password);
    }

    public User register(String username, String password) {
        return userService.register(username, password);
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public void createUser(String username, String password, String role, int tokens) {
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setRole(role);
        user.setTokens(tokens);
        user.setBlocked(false);
        userService.createUser(user);
    }

    public void updateUser(int id, String username, String password, String role, int tokens, boolean blocked) {
        User user = new User(id, username.trim(), password, role, tokens, blocked);
        userService.updateUser(user);
    }

    public void deleteUser(int id) {
        userService.deleteUser(id);
    }

    public void adjustUserTokens(int id, int tokens) {
        userService.adjustUserTokens(id, tokens);
    }

    public void toggleUserBlockStatus(int id, boolean blocked) {
        userService.toggleUserBlockStatus(id, blocked);
    }

    public List<User> getLeaderboard() {
        return userService.getLeaderboard();
    }

    public Map<String, String> getGameConfigs() {
        return userService.getGameConfigs();
    }



    public int rewardTokensIfEligible(int userId, int roundsUsed) {
        return userService.rewardTokensIfEligible(userId, roundsUsed);
    }
    
 // 確保你的 Controller 或是底層 Service 有持有這個物件
    private final UserDao userDao = new UserDaoImpl();

    /**
     * 提供給前台 MainFrame 呼叫的門戶
     */
    public List<User> getTopRichUsers() throws Exception {
        // 🟢 乾乾淨淨！沒有任何一行 SQL 與 JDBC，全權委託 DAO 處理
        return userDao.getTopRichUsers();
    }
    
    
}
