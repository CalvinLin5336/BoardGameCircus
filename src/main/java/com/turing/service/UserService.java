package com.turing.service;

import com.turing.model.User;
import java.util.List;
import java.util.Map;

/**
 * Service interface for handling User Authentication, CRUD, Token system, and Configurations.
 */
public interface UserService {
    /**
     * Authenticates a user. Returns the authenticated User if successful, or throws a GameException.
     */
    User login(String username, String password);

    /**
     * Registers a new standard player account (USER role, 0 initial tokens, unblocked).
     */
    User register(String username, String password);

    /**
     * Admin method to create a user.
     */
    void createUser(User user);

    /**
     * Admin method to update a user's details.
     */
    void updateUser(User user);

    /**
     * Admin method to delete a user.
     */
    void deleteUser(int userId);

    /**
     * Returns all registered users (for Admin dashboard).
     */
    List<User> getAllUsers();

    /**
     * Explicitly adjusts a player's token count.
     */
    void adjustUserTokens(int userId, int tokens);

    /**
     * Toggles the block status of an account.
     */
    void toggleUserBlockStatus(int userId, boolean blocked);

    /**
     * Gets top 10 players ranked by their token count.
     */
    List<User> getLeaderboard();

    /**
     * Retrieves all system game configurations (e.g., token rewards).
     */
    Map<String, String> getGameConfigs();



    /**
     * Evaluates a player's performance and awards tokens if they completed the game within the required rounds.
     * @param userId the ID of the user
     * @param roundsUsed the rounds taken to solve the puzzle
     * @return the number of tokens awarded (or 0 if ineligible)
     */
    int rewardTokensIfEligible(int userId, int roundsUsed);
    
    int rewardTokensIfEligible(int userId, int roundsUsed, char diffChar);
}
