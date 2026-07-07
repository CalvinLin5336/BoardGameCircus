package com.turing.dao;

import com.turing.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for managing User Accounts and Credentials.
 */
public interface UserDao {
    Optional<User> findByUsername(String username);
    boolean authenticate(String username, String password);
    void create(User user);
    void update(User user);
    void delete(int id);
    List<User> findAll();
    void updateTokens(int id, int tokens);
    void updateBlockedStatus(int id, boolean blocked);
    List<User> getLeaderboard();
    List<User> getTopRichUsers() throws Exception;
}
