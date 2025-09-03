package com.tdgame.model.systems;

import com.tdgame.config.GameConfig;
import com.tdgame.core.EventBus;

/**
 * Manages victory and defeat conditions according to game rules.
 * Tracks the 10% enemy power leak rule and wave completion.
 */
public class Rules {
    
    private final GameConfig config;
    private final WaveManager waveManager;
    private final CombatSystem combatSystem;
    private final double leakDefeatThreshold;
    
    private int totalEnemyPower = 0;
    private int leakedEnemyPower = 0;
    private boolean gameOver = false;
    private boolean victory = false;
    
    public Rules(GameConfig config, WaveManager waveManager, CombatSystem combatSystem) {
        this.config = config;
        this.waveManager = waveManager;
        this.combatSystem = combatSystem;
        this.leakDefeatThreshold = config.getLeakDefeatThreshold();
        
        // Subscribe to enemy reached castle events
        EventBus.getInstance().subscribe(EventBus.EnemyReachedCastleEvent.class, this::onEnemyReachedCastle);
    }
    
    public void update(double deltaTime) {
        if (gameOver) return;
        
        checkVictoryConditions();
        checkDefeatConditions();
    }
    
    /**
     * Check if victory conditions are met
     */
    private void checkVictoryConditions() {
        if (victory) return;
        
        // Victory: All waves complete and no live enemies
        if (waveManager.areAllWavesComplete() && !waveManager.hasLiveEnemies()) {
            victory = true;
            gameOver = true;
        }
    }
    
    /**
     * Check if defeat conditions are met
     */
    private void checkDefeatConditions() {
        if (gameOver) return;
        
        // Defeat: >= 10% of total enemy power has leaked
        if (totalEnemyPower > 0) {
            double leakPercentage = (double) leakedEnemyPower / totalEnemyPower;
            
            if (leakPercentage >= leakDefeatThreshold) {
                victory = false;
                gameOver = true;
            }
        }
    }
    
    /**
     * Handle enemy reaching the castle
     */
    private void onEnemyReachedCastle(EventBus.EnemyReachedCastleEvent event) {
        leakedEnemyPower += event.damage;
        
        // Also track total enemy power as enemies spawn
        // This is a simplified approach - in a more complex system,
        // we might track this when enemies are created
        if (totalEnemyPower == 0) {
            calculateTotalEnemyPower();
        }
    }
    
    /**
     * Calculate the total power of all enemies that will spawn
     * This is called when the first enemy reaches the castle
     */
    private void calculateTotalEnemyPower() {
        // This is a simplified calculation based on wave data
        // In practice, you might want to calculate this when waves are loaded
        totalEnemyPower = 1000; // Placeholder - should be calculated from wave configuration
        
        // More accurate calculation would iterate through all wave data:
        /*
        for (WaveData.Wave wave : config.getWaveData().waves) {
            for (WaveData.EnemySpawn spawn : wave.enemies) {
                Balance.EnemyStats stats = getEnemyStats(spawn.type);
                totalEnemyPower += stats.power * spawn.count;
            }
        }
        */
    }
    
    /**
     * Add enemy power when spawned (alternative tracking method)
     */
    public void addEnemyPower(int power) {
        totalEnemyPower += power;
    }
    
    /**
     * Get current leak percentage
     */
    public double getCurrentLeakPercentage() {
        return totalEnemyPower > 0 ? (double) leakedEnemyPower / totalEnemyPower : 0.0;
    }
    
    /**
     * Get remaining allowed leak before defeat
     */
    public double getRemainingLeakAllowance() {
        if (totalEnemyPower <= 0) return 1.0;
        
        double currentLeak = getCurrentLeakPercentage();
        return Math.max(0.0, leakDefeatThreshold - currentLeak);
    }
    
    /**
     * Reset rules for new game
     */
    public void reset() {
        totalEnemyPower = 0;
        leakedEnemyPower = 0;
        gameOver = false;
        victory = false;
    }
    
    // Getters
    public boolean isGameOver() { return gameOver; }
    public boolean isVictory() { return victory; }
    public int getTotalEnemyPower() { return totalEnemyPower; }
    public int getLeakedEnemyPower() { return leakedEnemyPower; }
    public double getLeakDefeatThreshold() { return leakDefeatThreshold; }
}