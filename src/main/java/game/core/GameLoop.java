package game.core;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import game.map.MapDefinition;
import game.map.MapRenderer;
import game.map.Route;
import game.ui.HudView;
import game.entity.*;
import game.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;

public class GameLoop extends AnimationTimer {
    
    private final GraphicsContext gc;
    private final MapDefinition mapDefinition;
    private final MapRenderer mapRenderer;
    private final ResourceManager resourceManager;
    private final WaveManager waveManager;
    private final HudView hudView;
    private final Random random = new Random();
    
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Tower> towers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<SpeedBump> speedBumps = new ArrayList<>();
    private final List<Bomb> bombs = new ArrayList<>();
    
    private long lastUpdate;
    private int leakedPower;
    private int totalLevelPower;
    private boolean gameOver;
    private boolean victory;
    
    public GameLoop(GraphicsContext gc, MapDefinition mapDefinition, ResourceManager resourceManager, 
                   WaveManager waveManager, HudView hudView) {
        this.gc = gc;
        this.mapDefinition = mapDefinition;
        this.resourceManager = resourceManager;
        this.waveManager = waveManager;
        this.hudView = hudView;
        this.mapRenderer = new MapRenderer(gc, mapDefinition, resourceManager);
        this.lastUpdate = 0;
        this.leakedPower = 0;
        this.totalLevelPower = 0;
        this.gameOver = false;
        this.victory = false;
    }
    
    @Override
    public void handle(long now) {
        if (lastUpdate == 0) {
            lastUpdate = now;
            return;
        }
        
        double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
        lastUpdate = now;
        
        if (!gameOver && !victory) {
            update(deltaTime);
        }
        
        render();
    }
    
    private void update(double deltaTime) {
        // Update wave manager and spawn enemies
        waveManager.update(deltaTime);
        List<Enemy> newEnemies = waveManager.getSpawnedEnemies();
        enemies.addAll(newEnemies);
        if (!newEnemies.isEmpty()) {
            totalLevelPower += newEnemies.stream().mapToInt(Enemy::getPowerValue).sum();
        }
        
        // Update game entities
        updateEnemies(deltaTime);
        updateTowers(deltaTime);
        updateProjectiles(deltaTime);
        updateGadgets(deltaTime);
        
        // Check game state
        checkGameState();
        
        // Cleanup dead entities
        cleanupEntities();
    }
    
    // Add missing methods that GameLoop needs
    public void addTower(Tower tower) {
        towers.add(tower);
    }
    
    public void addAntiAir(Object aa) {
        // Anti-air systems would be added here
    }
    
    public void addSpeedBump(Point2D position) {
        speedBumps.add(new SpeedBump(position));
    }
    
    public void addBomb(Point2D position) {
        bombs.add(new Bomb(position));
    }
    
    private void updateEnemies(double deltaTime) {
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);
            if (enemy.hasLeaked()) {
                leakedPower += enemy.getPowerValue();
            }
        }
    }
    
    private void updateTowers(double deltaTime) {
        for (Tower tower : towers) {
            tower.update(deltaTime);
            
            // Find targets and shoot
            if (tower.canShoot()) {
                Enemy target = findBestTarget(tower);
                if (target != null) {
                    tower.shootAt(target, projectiles);
                }
            }
        }
    }
    
    private Enemy findBestTarget(Tower tower) {
        Enemy bestTarget = null;
        double closestToExit = Double.MAX_VALUE;
        double lowestHp = Double.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive() || enemy.hasLeaked()) continue;
            if (!tower.canTarget(enemy)) continue;
            
            double distance = tower.getCenter().distance(enemy.getCenter());
            if (distance > tower.getRange() * Config.TILE_SIZE) continue;
            
            double distanceToExit = enemy.getDistanceToEnd();
            if (distanceToExit < closestToExit || 
                (distanceToExit == closestToExit && enemy.getCurrentHp() < lowestHp)) {
                closestToExit = distanceToExit;
                lowestHp = enemy.getCurrentHp();
                bestTarget = enemy;
            }
        }
        
        return bestTarget;
    }
    
    private void updateProjectiles(double deltaTime) {
        for (Projectile projectile : projectiles) {
            projectile.update(deltaTime);
            
            if (projectile.hasReachedTarget()) {
                projectile.dealDamage();
            }
        }
    }
    
    private void updateGadgets(double deltaTime) {
        // Update speed bumps
        Iterator<SpeedBump> speedBumpIt = speedBumps.iterator();
        while (speedBumpIt.hasNext()) {
            SpeedBump speedBump = speedBumpIt.next();
            speedBump.update(deltaTime);
            
            if (!speedBump.isActive()) {
                speedBumpIt.remove();
                continue;
            }
            
            // Apply slow to enemies on this tile
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && !enemy.hasLeaked()) {
                    Point2D enemyTile = enemy.getCurrentTilePosition();
                    if (speedBump.containsPoint(enemyTile)) {
                        enemy.applySlow(Config.SPEED_BUMP_FACTOR, 0.1);
                    }
                }
            }
        }
        
        // Update bombs
        Iterator<Bomb> bombIt = bombs.iterator();
        while (bombIt.hasNext()) {
            Bomb bomb = bombIt.next();
            
            if (!bomb.isTriggered()) {
                // Check for enemy collision
                for (Enemy enemy : enemies) {
                    if (enemy.isAlive() && !enemy.hasLeaked()) {
                        Point2D enemyTile = enemy.getCurrentTilePosition();
                        if (bomb.containsPoint(enemyTile)) {
                            bomb.trigger(enemies);
                            break;
                        }
                    }
                }
            }
            
            if (bomb.isTriggered()) {
                bombIt.remove();
            }
        }
    }
    
    private void checkGameState() {
        // Check defeat condition
        if (totalLevelPower > 0) {
            double leakRatio = (double) leakedPower / totalLevelPower;
            if (leakRatio >= Config.LEAK_DEFEAT_RATIO) {
                gameOver = true;
                return;
            }
        }
        
        // Check victory condition
        if (waveManager.isComplete() && enemies.stream().noneMatch(Enemy::isAlive)) {
            victory = true;
        }
    }
    
    private void cleanupEntities() {
        enemies.removeIf(e -> !e.isAlive() && e.hasLeaked());
        towers.removeIf(t -> !t.isAlive());
        projectiles.removeIf(p -> !p.isAlive());
    }
    
    private void render() {
        // Render map
        mapRenderer.render();
        
        // Render entities
        renderEnemies();
        renderTowers();
        renderProjectiles();
        renderGadgets();
        
        // Render UI info
        renderGameInfo();
    }
    
    private void renderEnemies() {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            
            String spriteKey = enemy instanceof Soldier ? "enemy.soldier" : "enemy.tank";
            var sprite = resourceManager.getSprite(spriteKey);
            
            if (sprite != null) {
                gc.drawImage(sprite, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
            } else {
                gc.setFill(enemy instanceof Soldier ? Color.BLUE : Color.DARKGREEN);
                gc.fillRect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
            }
            
            // Health bar
            if (enemy.getHpRatio() < 1.0) {
                renderHealthBar(enemy);
            }
        }
    }
    
    private void renderTowers() {
        for (Tower tower : towers) {
            if (!tower.isAlive()) continue;
            
            String spriteKey = tower instanceof FastTower ? "tower.fast" : "tower.heavy";
            var sprite = resourceManager.getSprite(spriteKey);
            
            if (sprite != null) {
                gc.drawImage(sprite, tower.getX(), tower.getY(), tower.getWidth(), tower.getHeight());
            } else {
                gc.setFill(Color.GRAY);
                gc.fillRect(tower.getX(), tower.getY(), tower.getWidth(), tower.getHeight());
            }
            
            if (tower.getHpRatio() < 1.0) {
                renderHealthBar(tower);
            }
        }
    }
    
    private void renderProjectiles() {
        for (Projectile projectile : projectiles) {
            if (!projectile.isAlive()) continue;
            
            var sprite = resourceManager.getSprite("proj.bullet");
            if (sprite != null) {
                gc.drawImage(sprite, projectile.getX(), projectile.getY());
            } else {
                gc.setFill(Color.YELLOW);
                gc.fillOval(projectile.getX(), projectile.getY(), 4, 4);
            }
        }
    }
    
    private void renderGadgets() {
        for (SpeedBump speedBump : speedBumps) {
            gc.setFill(Color.YELLOW);
            Point2D pos = speedBump.getPosition();
            gc.fillOval(pos.getX(), pos.getY(), Config.TILE_SIZE, Config.TILE_SIZE);
        }
    }
    
    private void renderHealthBar(Entity entity) {
        double barWidth = entity.getWidth();
        double barHeight = 4;
        double x = entity.getX();
        double y = entity.getY() - barHeight - 2;
        
        gc.setFill(Color.RED);
        gc.fillRect(x, y, barWidth, barHeight);
        
        gc.setFill(Color.GREEN);
        gc.fillRect(x, y, barWidth * entity.getHpRatio(), barHeight);
    }
    
    private void renderGameInfo() {
        if (gameOver) {
            gc.setFill(Color.RED);
            gc.fillText("GAME OVER - Too many enemies leaked!", 200, 300);
        } else if (victory) {
            gc.setFill(Color.GOLD);
            gc.fillText("VICTORY - All waves defeated!", 200, 300);
        }
        
        // Update HUD
        if (hudView != null) {
            hudView.updateWave(waveManager.getCurrentWave(), Config.TOTAL_WAVES);
            hudView.updateLeakage(getLeakPercentage());
        }
    }
    
    public void handleBuildSlotClick(int tileX, int tileY) {
        // Simple tower building - cycle through Fast -> Heavy -> AA
        double pixelX = tileX * Config.TILE_SIZE;
        double pixelY = tileY * Config.TILE_SIZE;
        
        if (resourceManager.canAfford(Config.FAST_TOWER_COST)) {
            if (resourceManager.spendMoney(Config.FAST_TOWER_COST)) {
                towers.add(new FastTower(pixelX, pixelY));
            }
        }
    }
    
    public void handleSpeedBumpSlotClick(int tileX, int tileY) {
        if (resourceManager.canAfford(Config.SPEED_BUMP_COST)) {
            if (resourceManager.spendMoney(Config.SPEED_BUMP_COST)) {
                Point2D pos = new Point2D(tileX, tileY);
                speedBumps.add(new SpeedBump(pos));
            }
        }
    }
    
    public void handleBombSlotClick(int tileX, int tileY) {
        if (resourceManager.canAfford(Config.BOMB_COST)) {
            if (resourceManager.spendMoney(Config.BOMB_COST)) {
                Point2D pos = new Point2D(tileX, tileY);
                bombs.add(new Bomb(pos));
            }
        }
    }
    
    public double getLeakPercentage() {
        return totalLevelPower > 0 ? (double) leakedPower / totalLevelPower : 0.0;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public boolean isVictory() {
        return victory;
    }
    
    // Simple gadget classes
    private static class SpeedBump {
        private final Point2D tilePos;
        private double lifetime;
        
        SpeedBump(Point2D tilePos) {
            this.tilePos = tilePos;
            this.lifetime = Config.SPEED_BUMP_LIFETIME_SEC;
        }
        
        void update(double deltaTime) {
            lifetime -= deltaTime;
        }
        
        boolean isActive() {
            return lifetime > 0;
        }
        
        boolean containsPoint(Point2D point) {
            return tilePos.equals(point);
        }
        
        Point2D getPosition() {
            return new Point2D(tilePos.getX() * Config.TILE_SIZE, tilePos.getY() * Config.TILE_SIZE);
        }
    }
    
    private static class Bomb {
        private final Point2D tilePos;
        private boolean triggered;
        
        Bomb(Point2D tilePos) {
            this.tilePos = tilePos;
            this.triggered = false;
        }
        
        boolean containsPoint(Point2D point) {
            return tilePos.equals(point);
        }
        
        void trigger(List<Enemy> enemies) {
            triggered = true;
            // Simple area damage
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && !enemy.hasLeaked()) {
                    Point2D enemyTile = enemy.getCurrentTilePosition();
                    if (tilePos.distance(enemyTile) <= 1.5) {
                        enemy.takeDamage(50); // Fixed bomb damage
                    }
                }
            }
        }
        
        boolean isTriggered() {
            return triggered;
        }
    }
}