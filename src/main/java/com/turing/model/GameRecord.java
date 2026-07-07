package com.turing.model;

import java.sql.Timestamp;

/**
 * Represents a saved game play session stored in the database.
 */
public class GameRecord {
    private Integer id;
    private String playerName;
    private String puzzleId;
    private int roundsUsed;
    private int testsUsed;
    private String secretCode;
    private boolean won;
    private Timestamp playedAt;
    private int tokensRewarded;
    
    
    public GameRecord() {
    }

    public GameRecord(String playerName, String puzzleId, int roundsUsed, int testsUsed, String secretCode, boolean won) {
        this.playerName = playerName;
        this.puzzleId = puzzleId;
        this.roundsUsed = roundsUsed;
        this.testsUsed = testsUsed;
        this.secretCode = secretCode;
        this.won = won;
    }

    public GameRecord(String playerName, String puzzleId, int roundsUsed, int testsUsed, String secretCode, boolean won, int tokensRewarded) {
        this.playerName = playerName;
        this.puzzleId = puzzleId;
        this.roundsUsed = roundsUsed;
        this.testsUsed = testsUsed;
        this.secretCode = secretCode;
        this.won = won;
        this.tokensRewarded = tokensRewarded;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPuzzleId() {
        return puzzleId;
    }

    public void setPuzzleId(String puzzleId) {
        this.puzzleId = puzzleId;
    }

    public int getRoundsUsed() {
        return roundsUsed;
    }

    public void setRoundsUsed(int roundsUsed) {
        this.roundsUsed = roundsUsed;
    }

    public int getTestsUsed() {
        return testsUsed;
    }

    public void setTestsUsed(int testsUsed) {
        this.testsUsed = testsUsed;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public Timestamp getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Timestamp playedAt) {
        this.playedAt = playedAt;
    }
    
    public int getTokensRewarded() { return tokensRewarded; }
    public void setTokensRewarded(int tokensRewarded) { this.tokensRewarded = tokensRewarded; }

    @Override
    public String toString() {
        return "GameRecord{" +
                "id=" + id +
                ", playerName='" + playerName + '\'' +
                ", puzzleId='" + puzzleId + '\'' +
                ", roundsUsed=" + roundsUsed +
                ", testsUsed=" + testsUsed +
                ", secretCode='" + secretCode + '\'' +
                ", won=" + won +
                ", playedAt=" + playedAt +
                '}';
    }
}
