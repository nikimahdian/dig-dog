package com.tdgame.model.systems;

import com.tdgame.config.GameConfig;
import com.tdgame.config.WaveData;
import com.tdgame.config.Balance;
import com.tdgame.model.actors.*;
import com.tdgame.model.grid.GridMap;
import com.tdgame.core.EventBus;
import com.tdgame.util.RNG;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages enemy wave spawning and progression.
 * Spawns enemies according to wave configuration data.
 */
public class WaveManager {
    
    private final GameConfig config;
    private final GridMap gridMap;
    private final List<Enemy> enemies;
    private CombatSystem combatSystem;
    
    private List<WaveData.Wave> waves;
    private int currentWaveIndex = 0;
    private double gameTime = 0.0;
    private boolean allWavesComplete = false;
    
    // Current wave state
    private WaveData.Wave currentWave = null;
    private boolean waveStarted = false;
    private List<WaveData.EnemySpawn> remainingSpawns = new ArrayList<>();
    private double spawnTimer = 0.0;
    private int currentSpawnIndex = 0;
    private int enemiesSpawnedInCurrentGroup = 0;
    
    // Lane alternation for soldiers
    private int nextLane = 0; // 0 or 1
    
    // Aircraft strike tracking
    private boolean aircraftStrikeTriggered = false;
    
    public WaveManager(GameConfig config, GridMap gridMap, List<Enemy> enemies) {
        this.config = config;
        this.gridMap = gridMap;
        this.enemies = enemies;
        this.waves = config.getWaveData().waves;
    }
    
    public void setCombatSystem(CombatSystem combatSystem) {
        this.combatSystem = combatSystem;
    }
    
    public void start() {
        gameTime = 0.0;
        currentWaveIndex = 0;
        allWavesComplete = false;
        
        // Start first wave if available
        if (!waves.isEmpty()) {
            scheduleNextWave();
        }
    }
    
    public void update(double deltaTime) {
        gameTime += deltaTime;
        
        if (allWavesComplete) return;
        
        // Check if it's time to start the next wave
        if (!waveStarted && currentWave != null && gameTime >= currentWave.delay) {
            startCurrentWave();
        }
        
        // Spawn enemies from current wave
        if (waveStarted) {
            updateEnemySpawning(deltaTime);
        }
        
        // Check if current wave is complete (all enemy types spawned)
        if (waveStarted && currentSpawnIndex >= currentWave.enemies.size()) {
            completeCurrentWave();
        }
    }
    
    /**
     * Schedule the next wave to start
     */
    private void scheduleNextWave() {
        if (currentWaveIndex < waves.size()) {
            currentWave = waves.get(currentWaveIndex);
            waveStarted = false;
            aircraftStrikeTriggered = false;
            
            // Reset spawning state
            currentSpawnIndex = 0;
            enemiesSpawnedInCurrentGroup = 0;
            spawnTimer = 0.0;
        }
    }
    
    /**
     * Start spawning the current wave
     */
    private void startCurrentWave() {
        waveStarted = true;
        
        // Copy enemy spawns to remaining list
        remainingSpawns.clear();
        remainingSpawns.addAll(currentWave.enemies);
        
        EventBus.getInstance().publish(new EventBus.WaveStartedEvent(currentWaveIndex + 1));
        
        // Trigger aircraft strike if specified
        if (currentWave.aircraftChance != null && RNG.nextBoolean(currentWave.aircraftChance)) {
            scheduleAircraftStrike();
        }
    }
    
    /**
     * Update enemy spawning within the current wave
     */
    private void updateEnemySpawning(double deltaTime) {
        if (remainingSpawns.isEmpty()) return;
        
        spawnTimer -= deltaTime;
        
        if (spawnTimer <= 0) {
            spawnNextEnemy();
        }
    }
    
    /**
     * Spawn the next enemy in the current wave
     */
    private void spawnNextEnemy() {
        if (currentSpawnIndex >= remainingSpawns.size()) return;
        
        WaveData.EnemySpawn spawn = remainingSpawns.get(currentSpawnIndex);
        
        // Create enemy based on type
        Enemy enemy = createEnemy(spawn.type);
        if (enemy != null) {
            enemy.setPath(gridMap.getMainPath());
            
            // Set lane for soldiers (alternate lanes), tanks use center
            if (enemy instanceof Soldier || enemy instanceof SoldierFast || enemy instanceof SoldierHeavy) {
                enemy.setLane(nextLane);
                nextLane = 1 - nextLane; // Alternate between 0 and 1
            } else if (enemy instanceof Tank) {
                enemy.setLane(0); // Tanks always center (lane offset = 0)
            }
            // Aircraft don't use paths
            
            enemies.add(enemy);
        }
        
        enemiesSpawnedInCurrentGroup++;
        
        // Check if we've spawned all enemies of this type
        if (enemiesSpawnedInCurrentGroup >= spawn.count) {
            currentSpawnIndex++;
            enemiesSpawnedInCurrentGroup = 0;
            
            // Set timer for next enemy group (or next enemy in current group)
            if (currentSpawnIndex < remainingSpawns.size()) {
                spawnTimer = remainingSpawns.get(currentSpawnIndex).gap;
            }
        } else {
            // Set timer for next enemy in current group
            spawnTimer = spawn.gap;
        }
    }
    
    /**
     * Create an enemy of the specified type
     */
    private Enemy createEnemy(String type) {
        Balance.EnemiesConfig enemies = config.getBalance().enemies;
        
        return switch (type.toLowerCase()) {
            case "soldier" -> new Soldier(enemies.soldier);
            case "soldierfast" -> new SoldierFast(enemies.soldierFast);
            case "soldierheavy" -> new SoldierHeavy(enemies.soldierHeavy);
            case "tank" -> new Tank(enemies.tank);
            case "aircraft" -> new Aircraft(enemies.aircraft);
            default -> {
                System.err.println("Unknown enemy type: " + type);
                yield null;
            }
        };
    }
    
    /**
     * Schedule an aircraft strike for this wave
     */
    private void scheduleAircraftStrike() {
        if (aircraftStrikeTriggered) return;
        
        // Create aircraft enemy
        Enemy aircraftEnemy = createEnemy("aircraft");
        if (aircraftEnemy instanceof Aircraft aircraft) {
            aircraft.setCombatSystem(combatSystem, config);
            enemies.add(aircraft);
            aircraftStrikeTriggered = true;
            
        }
    }
    
    /**
     * Complete the current wave and schedule the next one
     */
    private void completeCurrentWave() {
        waveStarted = false;
        currentWaveIndex++;
        
        System.out.println("Wave " + currentWaveIndex + " completed! Total waves: " + waves.size());
        
        if (currentWaveIndex < waves.size()) {
            scheduleNextWave();
        } else {
            allWavesComplete = true;
            System.out.println("🎉 All waves completed! Victory should trigger.");
        }
    }
    
    /**
     * Get total number of waves
     */
    public int getTotalWaves() {
        return waves.size();
    }
    
    /**
     * Get current wave number (1-indexed)
     */
    public int getCurrentWaveNumber() {
        // Don't show wave number higher than total waves
        int waveNumber = currentWaveIndex + 1;
        return Math.min(waveNumber, waves.size());
    }
    
    /**
     * Check if all waves are complete
     */
    public boolean areAllWavesComplete() {
        return allWavesComplete;
    }
    
    /**
     * Get remaining enemies in current wave
     */
    public int getRemainingEnemiesInWave() {
        if (!waveStarted || currentWave == null) return 0;
        
        int remaining = 0;
        for (int i = currentSpawnIndex; i < remainingSpawns.size(); i++) {
            WaveData.EnemySpawn spawn = remainingSpawns.get(i);
            if (i == currentSpawnIndex) {
                remaining += spawn.count - enemiesSpawnedInCurrentGroup;
            } else {
                remaining += spawn.count;
            }
        }
        return remaining;
    }
    
    /**
     * Check if there are live enemies on the map
     */
    public boolean hasLiveEnemies() {
        return enemies.stream().anyMatch(Enemy::isAlive);
    }
}