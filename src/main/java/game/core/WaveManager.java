package game.core;

import game.entity.*;
import game.map.MapDefinition;
import game.map.Route;
import game.Config;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveManager {
    
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    
    private final MapDefinition mapDefinition;
    private final Difficulty difficulty;
    private final Random random;
    
    private int currentWave;
    private double waveTimer;
    private boolean waveInProgress;
    private int totalEnemyPower;
    private int spawnedEnemyPower;
    private List<EnemySpawn> currentWaveSpawns;
    private int currentSpawnIndex;
    private double spawnTimer;
    
    private static class EnemySpawn {
        final Class<? extends Enemy> enemyType;
        final Route route;
        final double spawnTime;
        
        EnemySpawn(Class<? extends Enemy> enemyType, Route route, double spawnTime) {
            this.enemyType = enemyType;
            this.route = route;
            this.spawnTime = spawnTime;
        }
    }
    
    public WaveManager(MapDefinition mapDefinition) {
        this(mapDefinition, Difficulty.MEDIUM);
    }
    
    public WaveManager(MapDefinition mapDefinition, Difficulty difficulty) {
        this.mapDefinition = mapDefinition;
        this.difficulty = difficulty;
        this.random = new Random();
        this.currentWave = 0;
        this.waveTimer = 0;
        this.waveInProgress = false;
        this.currentWaveSpawns = new ArrayList<>();
        this.currentSpawnIndex = 0;
        this.spawnTimer = 0;
        
        // Auto-start first wave after initial delay
        this.waveTimer = getWaveDelay() - 3.0; // Start first wave in 3 seconds
    }
    
    public void update(double deltaTime) {
        if (!waveInProgress) {
            // Wait between waves
            waveTimer += deltaTime;
            if (waveTimer >= getWaveDelay()) {
                startNextWave();
            }
        } else {
            // Spawn enemies from current wave
            spawnTimer += deltaTime;
            spawnEnemiesIfReady();
        }
    }
    
    public void startNextWave() {
        if (currentWave >= Config.TOTAL_WAVES) {
            return; // All waves completed
        }
        
        currentWave++;
        waveInProgress = true;
        waveTimer = 0;
        spawnTimer = 0;
        currentSpawnIndex = 0;
        
        generateWaveSpawns();
        calculateTotalPower();
    }
    
    private void generateWaveSpawns() {
        currentWaveSpawns.clear();
        List<Route> routes = mapDefinition.getRoutes();
        
        // Base enemy counts, scaled by wave and difficulty
        int baseEnemyCount = 8 + currentWave * 2;
        double difficultyMultiplier = getDifficultyMultiplier();
        int totalEnemies = (int) (baseEnemyCount * difficultyMultiplier);
        
        // Distribute enemies across routes with proper spacing
        // "alternate waves between A & B or interleave squads across both"
        double currentTime = 0;
        for (int i = 0; i < totalEnemies; i++) {
            // Interleave squads across both routes - every other enemy uses different route
            Route route = routes.get(i % routes.size()); // Alternate: Route A, Route B, Route A, Route B...
            
            // Enemy type distribution (mostly soldiers, some tanks, occasional planes)
            Class<? extends Enemy> enemyType;
            int roll = random.nextInt(100);
            
            if (roll < 70) {
                enemyType = Soldier.class;
            } else if (roll < 95) {
                enemyType = Tank.class;
            } else {
                enemyType = Soldier.class; // Planes are handled separately
            }
            
            // Tank spacing: tanks occupy lane like 2 soldiers, so double the spacing
            double spawnDelay;
            if (enemyType == Tank.class) {
                spawnDelay = 3.0; // 3 seconds for tanks (double soldier spacing)
            } else {
                spawnDelay = 1.5; // 1.5 seconds for soldiers
            }
            
            currentWaveSpawns.add(new EnemySpawn(enemyType, route, currentTime));
            currentTime += spawnDelay;
        }
        
        // Add some planes based on difficulty and wave
        if (currentWave >= 2) {
            int planeCount = difficulty == Difficulty.HARD ? 2 : 1;
            for (int i = 0; i < planeCount; i++) {
                // Planes don't use routes - they fly across the screen
                double planeSpawnTime = 10 + i * 8; // Planes spawn later in the wave
                currentWaveSpawns.add(new EnemySpawn(Plane.class, null, planeSpawnTime));
            }
        }
        
        // Sort spawns by time
        currentWaveSpawns.sort((a, b) -> Double.compare(a.spawnTime, b.spawnTime));
    }
    
    private void spawnEnemiesIfReady() {
        while (currentSpawnIndex < currentWaveSpawns.size()) {
            EnemySpawn spawn = currentWaveSpawns.get(currentSpawnIndex);
            
            if (spawnTimer >= spawn.spawnTime) {
                Enemy enemy = createEnemy(spawn);
                if (enemy != null) {
                    spawnedEnemyPower += enemy.getPowerValue();
                    // The actual spawning would be handled by the game loop
                    onEnemySpawned(enemy);
                }
                currentSpawnIndex++;
            } else {
                break; // Wait for the right time
            }
        }
        
        // Check if wave is complete
        if (currentSpawnIndex >= currentWaveSpawns.size()) {
            waveInProgress = false;
            waveTimer = 0; // Start delay for next wave
        }
    }
    
    private Enemy createEnemy(EnemySpawn spawn) {
        try {
            if (spawn.enemyType == Soldier.class) {
                Soldier soldier = new Soldier(spawn.route);
                applyDifficultyModifiers(soldier);
                return soldier;
                
            } else if (spawn.enemyType == Tank.class) {
                Tank tank = new Tank(spawn.route);
                applyDifficultyModifiers(tank);
                return tank;
                
            } else if (spawn.enemyType == Plane.class) {
                // Planes fly straight across screen - can come from any direction
                Point2D start, end;
                int direction = random.nextInt(4); // 0=left-right, 1=right-left, 2=top-bottom, 3=bottom-top
                
                switch (direction) {
                    case 0: // Left to right
                        start = new Point2D(-50, random.nextDouble() * Config.MAP_HEIGHT);
                        end = new Point2D(Config.MAP_WIDTH + 50, random.nextDouble() * Config.MAP_HEIGHT);
                        break;
                    case 1: // Right to left
                        start = new Point2D(Config.MAP_WIDTH + 50, random.nextDouble() * Config.MAP_HEIGHT);
                        end = new Point2D(-50, random.nextDouble() * Config.MAP_HEIGHT);
                        break;
                    case 2: // Top to bottom
                        start = new Point2D(random.nextDouble() * Config.MAP_WIDTH, -50);
                        end = new Point2D(random.nextDouble() * Config.MAP_WIDTH, Config.MAP_HEIGHT + 50);
                        break;
                    default: // Bottom to top
                        start = new Point2D(random.nextDouble() * Config.MAP_WIDTH, Config.MAP_HEIGHT + 50);
                        end = new Point2D(random.nextDouble() * Config.MAP_WIDTH, -50);
                        break;
                }
                
                Plane plane = new Plane(start, end);
                return plane; // Planes don't need difficulty modifiers applied the same way
            }
        } catch (Exception e) {
            System.err.println("Error creating enemy: " + e.getMessage());
        }
        
        return null;
    }
    
    private void applyDifficultyModifiers(Enemy enemy) {
        double baseMaxHp = enemy.getMaxHp();
        
        // Difficulty modifiers
        double difficultyHpMultiplier = 1.0;
        if (difficulty == Difficulty.EASY) {
            difficultyHpMultiplier = 1.0 + Config.EASY_HP_BONUS; // +20% base HP
        } else if (difficulty == Difficulty.HARD) {
            difficultyHpMultiplier = Config.HARD_HP_MULTIPLIER; // +25% HP
            // Hard also affects speed - enemies move 25% faster
            // This would require modifying the enemy's speed property directly
        }
        
        // Wave scaling - increase HP by 10% per wave (cumulative)
        double waveHpMultiplier = 1.0 + ((currentWave - 1) * 0.1);
        
        // Apply total multiplier
        double totalMultiplier = difficultyHpMultiplier * waveHpMultiplier;
        double newMaxHp = baseMaxHp * totalMultiplier;
        double hpIncrease = newMaxHp - baseMaxHp;
        
        if (hpIncrease > 0) {
            enemy.heal(hpIncrease);
            // Also increase max HP so healing works correctly
            enemy.maxHp = newMaxHp;
        }
    }
    
    private void calculateTotalPower() {
        totalEnemyPower = 0;
        spawnedEnemyPower = 0;
        
        for (EnemySpawn spawn : currentWaveSpawns) {
            if (spawn.enemyType == Soldier.class) {
                totalEnemyPower += Config.SOLDIER_POWER;
            } else if (spawn.enemyType == Tank.class) {
                totalEnemyPower += Config.TANK_POWER;
            } else if (spawn.enemyType == Plane.class) {
                totalEnemyPower += Config.PLANE_POWER;
            }
        }
    }
    
    private double getDifficultyMultiplier() {
        return switch (difficulty) {
            case EASY -> 0.8;
            case MEDIUM -> 1.0;
            case HARD -> 1.3;
        };
    }
    
    private double getWaveDelay() {
        return 5.0; // 5 seconds between waves
    }
    
    @FunctionalInterface
    public interface EnemySpawnCallback {
        void onEnemySpawned(Enemy enemy);
    }
    
    public EnemySpawnCallback onEnemySpawned;
    
    protected void onEnemySpawned(Enemy enemy) {
        if (onEnemySpawned != null) {
            onEnemySpawned.onEnemySpawned(enemy);
        }
    }
    
    public int getCurrentWave() {
        return currentWave;
    }
    
    public int getTotalWaves() {
        return Config.TOTAL_WAVES;
    }
    
    public boolean isWaveInProgress() {
        return waveInProgress;
    }
    
    public double getTimeToNextWave() {
        return waveInProgress ? 0 : Math.max(0, getWaveDelay() - waveTimer);
    }
    
    public int getRemainingSpawns() {
        return waveInProgress ? currentWaveSpawns.size() - currentSpawnIndex : 0;
    }
    
    public int getTotalEnemyPower() {
        return totalEnemyPower;
    }
    
    public boolean allWavesComplete() {
        return currentWave >= Config.TOTAL_WAVES && !waveInProgress;
    }
    
    // Track spawned enemies for the game loop
    private final List<Enemy> spawnedEnemies = new ArrayList<>();
    
    public List<Enemy> getSpawnedEnemies() {
        List<Enemy> result = new ArrayList<>(spawnedEnemies);
        spawnedEnemies.clear();
        return result;
    }
    
    @Override
    protected void onEnemySpawned(Enemy enemy) {
        spawnedEnemies.add(enemy);
        if (onEnemySpawned != null) {
            onEnemySpawned.onEnemySpawned(enemy);
        }
    }
    
    public boolean isComplete() {
        return allWavesComplete();
    }
}