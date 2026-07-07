package com.turing.model;

public class GameConfig {
    private char difficultyChar;
    private int baseReward;
    private int speedBonusThreshold;
    private int speedBonusAmount;

    public GameConfig(char difficultyChar, int baseReward, int speedBonusThreshold, int speedBonusAmount) {
        this.difficultyChar = difficultyChar;
        this.baseReward = baseReward;
        this.speedBonusThreshold = speedBonusThreshold;
        this.speedBonusAmount = speedBonusAmount;
    }

    // Getters & Setters
    public char getDifficultyChar() { return difficultyChar; }
    public int getBaseReward() { return baseReward; }
    public void setBaseReward(int baseReward) { this.baseReward = baseReward; }
    public int getSpeedBonusThreshold() { return speedBonusThreshold; }
    public void setSpeedBonusThreshold(int speedBonusThreshold) { this.speedBonusThreshold = speedBonusThreshold; }
    public int getSpeedBonusAmount() { return speedBonusAmount; }
    public void setSpeedBonusAmount(int speedBonusAmount) { this.speedBonusAmount = speedBonusAmount; }
}