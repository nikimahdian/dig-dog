package com.tdgame.config;

import com.tdgame.util.Json;

/**
 * Loads and manages game configuration from JSON files.
 * Central access point for all game configuration data.
 */
public class GameConfig {
    private final String levelName;
    private final String difficulty;
    private final Balance balance;
    private final LevelData levelData;
    private final WaveData waveData;
    
    private GameConfig(String levelName, String difficulty) {
        this.levelName = levelName;
        this.difficulty = difficulty;
        
        // Load configuration files
        this.balance = Json.loadFromResource("config/balance.json", Balance.class);
        this.levelData = Json.loadFromResource("levels/" + levelName + ".json", LevelData.class);
        this.waveData = Json.loadFromResource("waves/" + difficulty + ".json", WaveData.class);
    }
    
    public static GameConfig load(String levelName, String difficulty) {
        return new GameConfig(levelName, difficulty);
    }
    
    // Getters
    public String getLevelName() { return levelName; }
    public String getDifficulty() { return difficulty; }
    public Balance getBalance() { return balance; }
    public LevelData getLevelData() { return levelData; }
    public WaveData getWaveData() { return waveData; }
    
    // Convenience accessors
    public int getGridCols() { return levelData.grid.cols; }
    public int getGridRows() { return levelData.grid.rows; }
    public int getTileSize() { return levelData.grid.tileSize; }
    public int getStartingMoney() { return balance.money.start; }
    public int getMoneyIncomePerSec() { return balance.money.incomePerSec; }
    public double getLeakDefeatThreshold() { return balance.rules.leakPctDefeat; }
}