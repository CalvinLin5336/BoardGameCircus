package com.turing.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game puzzle setup containing a secret code and a list of active verifiers.
 */
public class Puzzle {
    private String puzzleId;
    private String difficulty; // "Easy", "Standard", "Hard"
    private Code secretCode;
    private List<VerifierCard> verifiers;

    public Puzzle(String puzzleId, String difficulty, Code secretCode) {
        this.puzzleId = puzzleId;
        this.difficulty = difficulty;
        this.secretCode = secretCode;
        this.verifiers = new ArrayList<>();
    }

    public String getPuzzleId() {
        return puzzleId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Code getSecretCode() {
        return secretCode;
    }

    public List<VerifierCard> getVerifiers() {
        return verifiers;
    }

    public void addVerifier(VerifierCard verifier) {
        this.verifiers.add(verifier);
    }

    @Override
    public String toString() {
        return "Puzzle " + puzzleId + " (" + difficulty + ") with " + verifiers.size() + " verifiers";
    }
}
