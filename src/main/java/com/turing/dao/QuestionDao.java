package com.turing.dao;

import com.turing.model.Puzzle;
import com.turing.model.VerifierCard;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for managing Question Bank.
 */
public interface QuestionDao {
    /**
     * Fetches a random puzzle from the database questions table with a specified card count (kCount).
     * If kCount <= 0, fetches any puzzle randomly.
     */
    Optional<Puzzle> getRandomPuzzle(int kCount);
    
    List<VerifierCard> getLiveVerifierCards();
}
