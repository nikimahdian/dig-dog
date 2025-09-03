package com.tdgame.view;

import com.tdgame.config.GameConfig;
import com.tdgame.model.grid.GridMap;
import com.tdgame.model.grid.Tile;
import com.tdgame.model.actors.*;
import com.tdgame.model.placeables.*;
import com.tdgame.model.systems.AircraftStrikeSystem;
import com.tdgame.util.Math2D;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.util.List;

/**
 * Main game rendering canvas.
 * Draws the game world including tiles, entities, and effects.
 */
public class GameCanvas {
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final GameConfig config;
    private final SpriteLoader spriteLoader;
    
    // Rendering components
    private GridMap gridMap;
    private List<Enemy> enemies;
    private List<Tower> towers;
    private List<AADefense> aaDefenses;
    private List<Projectile> projectiles;
    private List<SpeedBump> speedBumps;
    private List<Bomb> bombs;
    private List<AircraftStrikeSystem.PendingStrike> pendingStrikes;
    
    public GameCanvas(GameConfig config) {
        this.config = config;
        this.spriteLoader = SpriteLoader.getInstance();
        
        int width = config.getGridCols() * config.getTileSize();
        int height = config.getGridRows() * config.getTileSize();
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
        
        // Preload sprites
        spriteLoader.preloadCommonSprites();
    }
    
    /**
     * Set the game entities to render
     */
    public void setRenderData(GridMap gridMap, 
                             List<Enemy> enemies, 
                             List<Tower> towers, 
                             List<AADefense> aaDefenses,
                             List<Projectile> projectiles,
                             List<SpeedBump> speedBumps,
                             List<Bomb> bombs,
                             List<AircraftStrikeSystem.PendingStrike> pendingStrikes) {
        this.gridMap = gridMap;
        this.enemies = enemies;
        this.towers = towers;
        this.aaDefenses = aaDefenses;
        this.projectiles = projectiles;
        this.speedBumps = speedBumps;
        this.bombs = bombs;
        this.pendingStrikes = pendingStrikes;
    }
    
    public void render() {
        // Clear canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Render in layers
        renderTiles();
        renderPath();
        renderBuildSlots();
        renderPlaceables();
        renderEnemies();
        renderTowers();
        renderProjectiles();
        renderEffects();
        renderUI();
    }
    
    /**
     * Render the tile grid
     */
    private void renderTiles() {
        if (gridMap == null) return;
        
        Tile[][] tiles = gridMap.getTiles();
        double tileSize = config.getTileSize();
        
        for (int row = 0; row < config.getGridRows(); row++) {
            for (int col = 0; col < config.getGridCols(); col++) {
                Tile tile = tiles[row][col];
                if (tile != null) {
                    double x = col * tileSize;
                    double y = row * tileSize;
                    
                    renderSprite(tile.getSpriteIndex(), x, y, tileSize, tileSize);
                }
            }
        }
    }
    
    /**
     * Render the enemy path
     */
    private void renderPath() {
        if (gridMap == null) return;
        
        GridMap.Path mainPath = gridMap.getMainPath();
        if (mainPath == null) return;
        
        // Draw path as a line
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);
        
        List<Math2D.Point> waypoints = mainPath.getWaypoints();
        if (waypoints.size() > 1) {
            gc.beginPath();
            Math2D.Point first = waypoints.get(0);
            gc.moveTo(first.x, first.y);
            
            for (int i = 1; i < waypoints.size(); i++) {
                Math2D.Point point = waypoints.get(i);
                gc.lineTo(point.x, point.y);
            }
            
            gc.stroke();
        }
    }
    
    /**
     * Render build slots
     */
    private void renderBuildSlots() {
        if (gridMap == null) return;
        
        gc.setStroke(Color.LIGHTGREEN);
        gc.setLineWidth(1);
        double tileSize = config.getTileSize();
        
        // Build slots for towers
        for (var slot : gridMap.getBuildSlots()) {
            if (!slot.isOccupied()) {
                double x = slot.getCol() * tileSize;
                double y = slot.getRow() * tileSize;
                gc.strokeRect(x, y, tileSize, tileSize);
            }
        }
        
        // Speed bump slots
        gc.setStroke(Color.ORANGE);
        for (var slot : gridMap.getSpeedBumpSlots()) {
            if (!slot.isOccupied()) {
                double x = slot.getCol() * tileSize;
                double y = slot.getRow() * tileSize;
                gc.strokeOval(x + 5, y + 5, tileSize - 10, tileSize - 10);
            }
        }
        
        // Bomb slots
        gc.setStroke(Color.RED);
        for (var slot : gridMap.getBombSlots()) {
            if (!slot.isOccupied()) {
                double x = slot.getCol() * tileSize;
                double y = slot.getRow() * tileSize;
                gc.strokeRect(x + 5, y + 5, tileSize - 10, tileSize - 10);
            }
        }
    }
    
    /**
     * Render placeable items
     */
    private void renderPlaceables() {
        if (speedBumps != null) {
            for (SpeedBump speedBump : speedBumps) {
                if (speedBump.isActive()) {
                    renderSprite(speedBump.getSpriteIndex(), 
                               speedBump.getX() - 24, speedBump.getY() - 24, 48, 48);
                    
                    // Draw duration indicator
                    double progress = speedBump.getRemainingPercent();
                    gc.setFill(Color.ORANGE);
                    gc.fillRect(speedBump.getX() - 20, speedBump.getY() + 30, 40 * progress, 4);
                }
            }
        }
        
        if (bombs != null) {
            for (Bomb bomb : bombs) {
                if (bomb.isArmed()) {
                    renderSprite(bomb.getSpriteIndex(), 
                               bomb.getX() - 24, bomb.getY() - 24, 48, 48);
                } else if (bomb.hasExploded() && !bomb.isExplosionComplete()) {
                    // Draw explosion effect
                    double progress = bomb.getExplosionProgress();
                    double radius = bomb.getRadius() * progress * config.getTileSize();
                    
                    gc.setFill(Color.ORANGE.deriveColor(0, 1, 1, 1 - progress));
                    gc.fillOval(bomb.getX() - radius, bomb.getY() - radius, 
                               radius * 2, radius * 2);
                }
            }
        }
    }
    
    /**
     * Render enemies
     */
    private void renderEnemies() {
        if (enemies == null) return;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            
            // Draw enemy sprite
            renderSprite(enemy.getSpriteIndex(), 
                       enemy.getX() - 24, enemy.getY() - 24, 48, 48);
            
            // Draw health bar
            renderHealthBar(enemy.getX(), enemy.getY() - 35, 
                          enemy.getCurrentHp(), enemy.getMaxHp());
            
            // Draw slow effect
            if (enemy.getSlowMultiplier() < 1.0) {
                gc.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.3));
                gc.fillOval(enemy.getX() - 20, enemy.getY() - 20, 40, 40);
            }
        }
    }
    
    /**
     * Render towers and AA defenses
     */
    private void renderTowers() {
        if (towers != null) {
            for (Tower tower : towers) {
                if (!tower.isAlive()) continue;
                
                renderSprite(tower.getSpriteIndex(), 
                           tower.getX() - 24, tower.getY() - 24, 48, 48);
                
                // Draw health bar
                renderHealthBar(tower.getX(), tower.getY() - 35, 
                              tower.getHp(), tower.getMaxHp());
                
                // Draw range indicator when tower has target
                if (tower.getCurrentTarget() != null) {
                    gc.setStroke(Color.GREEN.deriveColor(0, 1, 1, 0.3));
                    gc.setLineWidth(1);
                    double range = tower.getRange() * config.getTileSize();
                    gc.strokeOval(tower.getX() - range, tower.getY() - range, 
                                 range * 2, range * 2);
                }
            }
        }
        
        if (aaDefenses != null) {
            for (AADefense aa : aaDefenses) {
                if (!aa.isAlive()) continue;
                
                renderSprite(aa.getSpriteIndex(), 
                           aa.getX() - 24, aa.getY() - 24, 48, 48);
                
                renderHealthBar(aa.getX(), aa.getY() - 35, 
                              aa.getHp(), aa.getMaxHp());
                
                // Draw AA range
                if (aa.getCurrentTarget() != null) {
                    gc.setStroke(Color.PURPLE.deriveColor(0, 1, 1, 0.3));
                    gc.setLineWidth(1);
                    double range = aa.getRange() * config.getTileSize();
                    gc.strokeOval(aa.getX() - range, aa.getY() - range, 
                                 range * 2, range * 2);
                }
            }
        }
    }
    
    /**
     * Render projectiles
     */
    private void renderProjectiles() {
        if (projectiles == null) return;
        
        for (Projectile projectile : projectiles) {
            if (!projectile.isActive()) continue;
            
            renderSprite(projectile.getSpriteIndex(), 
                       projectile.getX() - 8, projectile.getY() - 8, 16, 16);
        }
    }
    
    /**
     * Render special effects
     */
    private void renderEffects() {
        // Render aircraft strike warnings
        if (pendingStrikes != null) {
            for (var strike : pendingStrikes) {
                double alpha = Math.sin(System.currentTimeMillis() * 0.01) * 0.5 + 0.5;
                gc.setFill(Color.RED.deriveColor(0, 1, 1, alpha * 0.5));
                
                if (strike.getType() == AircraftStrikeSystem.StrikeType.ROW) {
                    double y = strike.getIndex() * config.getTileSize();
                    gc.fillRect(0, y, canvas.getWidth(), config.getTileSize());
                } else {
                    double x = strike.getIndex() * config.getTileSize();
                    gc.fillRect(x, 0, config.getTileSize(), canvas.getHeight());
                }
            }
        }
    }
    
    /**
     * Render UI overlays
     */
    private void renderUI() {
        // Grid lines (optional)
        gc.setStroke(Color.GRAY.deriveColor(0, 1, 1, 0.1));
        gc.setLineWidth(1);
        
        double tileSize = config.getTileSize();
        
        // Vertical lines
        for (int col = 0; col <= config.getGridCols(); col++) {
            double x = col * tileSize;
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }
        
        // Horizontal lines
        for (int row = 0; row <= config.getGridRows(); row++) {
            double y = row * tileSize;
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }
    }
    
    /**
     * Render a sprite at the given position
     */
    private void renderSprite(int spriteIndex, double x, double y, double width, double height) {
        Image sprite = spriteLoader.getSprite(spriteIndex);
        if (sprite != null) {
            gc.drawImage(sprite, x, y, width, height);
        } else {
            // Draw placeholder rectangle
            gc.setFill(Color.MAGENTA);
            gc.fillRect(x, y, width, height);
        }
    }
    
    /**
     * Render a health bar
     */
    private void renderHealthBar(double x, double y, int currentHp, int maxHp) {
        if (currentHp >= maxHp) return; // Don't show full health bars
        
        double barWidth = 30;
        double barHeight = 4;
        double healthPercent = (double) currentHp / maxHp;
        
        // Background
        gc.setFill(Color.RED);
        gc.fillRect(x - barWidth/2, y, barWidth, barHeight);
        
        // Health
        gc.setFill(Color.GREEN);
        gc.fillRect(x - barWidth/2, y, barWidth * healthPercent, barHeight);
        
        // Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x - barWidth/2, y, barWidth, barHeight);
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
}