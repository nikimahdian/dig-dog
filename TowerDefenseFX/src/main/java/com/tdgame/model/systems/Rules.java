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
        
        // Calculate total enemy power at start
        calculateTotalEnemyPower();
        
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
            
            // Trigger victory event
            EventBus.getInstance().publish(new EventBus.GameOverEvent(true));
            
            System.out.println("🎉 VICTORY! All waves defeated!");
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
                
                // Trigger defeat event
                EventBus.getInstance().publish(new EventBus.GameOverEvent(false));
                
                System.out.println("💀 DEFEAT! " + String.format("%.1f%%", leakPercentage * 100) + " enemies reached castle! (Limit: " + String.format("%.1f%%", leakDefeatThreshold * 100) + ")");
            }
        }
    }
    
    /**
     * Handle enemy reaching the castle
     */
    private void onEnemyReachedCastle(EventBus.EnemyReachedCastleEvent event) {
        leakedEnemyPower += event.damage;
        
        System.out.println("Enemy reached castle! Power: " + event.damage + 
                          ", Total leaked: " + leakedEnemyPower + "/" + totalEnemyPower + 
                          " (" + String.format("%.1f%%", getCurrentLeakPercentage() * 100) + ")");
    }
    
    /**
     * Calculate the total power of all enemies that will spawn
     */
    private void calculateTotalEnemyPower() {
        totalEnemyPower = 0;
        
        // Calculate from wave data
        for (var wave : config.getWaveData().waves) {
            for (var spawn : wave.enemies) {
                int enemyPower = getEnemyPower(spawn.type);
                totalEnemyPower += enemyPower * spawn.count;
            }
            
            // Add aircraft power if wave has aircraft chance
            if (wave.aircraftChance != null && wave.aircraftChance > 0) {
                totalEnemyPower += 3; // Aircraft power (estimated)
            }
        }
        
        System.out.println("Total enemy power calculated: " + totalEnemyPower);
    }
    
    /**
     * Get enemy power by type
     */
    private int getEnemyPower(String type) {
        return switch (type.toLowerCase()) {
            case "soldier" -> 1;
            case "soldierfast" -> 1;
            case "soldierheavy" -> 1;
            case "tank" -> 2;  
            case "aircraft" -> 3;
            default -> 1;
        };
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