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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
    
    // Camera system
    private double zoomLevel = 1.0;
    private final double MIN_ZOOM = 0.5;
    private final double MAX_ZOOM = 2.0;
    private final double ZOOM_STEP = 0.1;
    
    // Pan system
    private double cameraX = 0.0;
    private double cameraY = 0.0;
    private final double PAN_SPEED = 50.0;
    
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
        
        // Bigger canvas for the new 14x10 grid
        int width = config.getGridCols() * config.getTileSize();
        int height = config.getGridRows() * config.getTileSize();
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
        
        // Enhanced canvas styling
        canvas.setStyle("""
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 2, 2);
            -fx-border-color: #34495e;
            -fx-border-width: 3;
            -fx-border-radius: 8;
        """);
        
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
        // Clear canvas with gradient background
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Gradient background
        gc.setFill(javafx.scene.paint.LinearGradient.valueOf("linear-gradient(to bottom right, #2c3e50, #34495e, #1abc9c)"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Apply camera transformation (zoom + pan)
        gc.save();
        
        // Center zoom around middle of canvas
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        
        gc.translate(centerX, centerY);
        gc.scale(zoomLevel, zoomLevel);
        gc.translate(-centerX + cameraX, -centerY + cameraY);
        
        // Render in layers
        renderTiles();
        // Only render path if not using manual tiles
        if (config.getLevelData().manualTiles == null || 
            config.getLevelData().manualTiles.isEmpty()) {
            renderPath();
        }
        renderBuildSlots();
        renderPlaceables();
        // Render path glow on top of everything if using manual tiles
        if (config.getLevelData().manualTiles != null && 
            !config.getLevelData().manualTiles.isEmpty()) {
            renderPathGlow();
        }
        renderEnemies();
        renderTowers();
        renderProjectiles();
        renderEffects();
        renderUI();
        
        // Restore zoom transformation
        gc.restore();
        
        // Render zoom UI
        renderZoomUI();
    }
    
    /**
     * Render the tile grid with beautiful terrain
     */
    private void renderTiles() {
        if (gridMap == null) return;
        
        Tile[][] tiles = gridMap.getTiles();
        double tileSize = config.getTileSize();
        
        // Check if manual tiles are defined
        if (config.getLevelData().manualTiles != null && 
            !config.getLevelData().manualTiles.isEmpty()) {
            // Use manual tile mapping
            renderManualTiles();
        } else {
            // Use dynamic tile generation
            renderDynamicTiles(tileSize);
        }
    }
    
    /**
     * Render tiles using manual tile mapping from JSON
     */
    private void renderManualTiles() {
        double tileSize = config.getTileSize();
        var manualTiles = config.getLevelData().manualTiles;
        
        for (int row = 0; row < manualTiles.size() && row < config.getGridRows(); row++) {
            var rowTiles = manualTiles.get(row);
            for (int col = 0; col < rowTiles.size() && col < config.getGridCols(); col++) {
                var manualTile = rowTiles.get(col);
                if (manualTile != null) {
                    double x = col * tileSize;
                    double y = row * tileSize;
                    renderSpriteWithRotation(manualTile.tileIndex, x, y, tileSize, tileSize, manualTile.rotation);
                }
            }
        }
    }
    
    /**
     * Render tiles using dynamic generation (original behavior)
     */
    private void renderDynamicTiles(double tileSize) {
        // First pass: render base terrain
        for (int row = 0; row < config.getGridRows(); row++) {
            for (int col = 0; col < config.getGridCols(); col++) {
                double x = col * tileSize;
                double y = row * tileSize;
                
                // Add variety to grass tiles
                int grassVariant = getGrassVariant(col, row);
                renderSprite(grassVariant, x, y, tileSize, tileSize);
            }
        }
        
        // Second pass: render terrain features
        renderTerrainFeatures();
        
        // Third pass: render decorative elements
        renderDecorativeElements();
    }
    
    /**
     * Get grass variant for position (adds natural variety)
     */
    private int getGrassVariant(int col, int row) {
        return 119;
    }
    
    /**
     * Render terrain features like sand pads and rocky areas based on level
     */
    private void renderTerrainFeatures() {
        double tileSize = config.getTileSize();
        String levelName = config.getLevelData().name;
        
        if (levelName.contains("Classic")) {
            // Classic Arena - balanced terrain
            renderSandPad(8, 0, 6, 2); // Top right sand area
            renderSandPad(0, 8, 5, 2); // Bottom left sand area
            renderRockyTerrain(12, 0, 2, 10); // Right edge cliffs
        } else if (levelName.contains("Desert")) {
            // Desert Storm - lots of sand
            renderSandPad(0, 0, 16, 2);   // Top desert
            renderSandPad(0, 10, 16, 2);  // Bottom desert
            renderSandPad(12, 2, 4, 8);   // Right side sand
            renderRockyTerrain(0, 2, 2, 8); // Left cliffs
        } else if (levelName.contains("Mountain")) {
            // Mountain Pass - rocky terrain
            renderRockyTerrain(0, 0, 4, 14);   // Left mountain
            renderRockyTerrain(8, 0, 4, 14);   // Right mountain
            renderSandPad(4, 12, 4, 2);       // Valley bottom
        }
    }
    
    /**
     * Render sand pad area
     */
    private void renderSandPad(int startCol, int startRow, int width, int height) {
        double tileSize = config.getTileSize();
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double x = (startCol + col) * tileSize;
                double y = (startRow + row) * tileSize;
                
                // Main sand fill
                renderSprite(29, x, y, tileSize, tileSize);
                
                // Add sand edges for borders (simplified)
                if (row == 0 || row == height-1 || col == 0 || col == width-1) {
                    // Add subtle sand edge effect
                    int edgeVariant = (col + row) % 2 == 0 ? 28 : 30;
                    renderSprite(edgeVariant, x, y, tileSize, tileSize);
                }
            }
        }
    }
    
    /**
     * Render rocky terrain areas
     */
    private void renderRockyTerrain(int startCol, int startRow, int width, int height) {
        double tileSize = config.getTileSize();
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (startCol + col >= config.getGridCols() || startRow + row >= config.getGridRows()) continue;
                
                double x = (startCol + col) * tileSize;
                double y = (startRow + row) * tileSize;
                
                // Rocky terrain variants
                int[] rockyTiles = {13, 14, 37, 34};
                int variant = ((col + row) * 3) % rockyTiles.length;
                renderSprite(rockyTiles[variant], x, y, tileSize, tileSize);
            }
        }
    }
    
    /**
     * Render decorative elements based on level theme
     */
    private void renderDecorativeElements() {
        double tileSize = config.getTileSize();
        String levelName = config.getLevelData().name;
        
        if (levelName.contains("Classic")) {
            // Classic Arena decorations
            renderLevelDecorations(new int[][]{{1,1}, {4,1}, {7,1}, {12,1}, {1,4}, {5,4}, {12,4}, {1,7}}, 
                                  new int[][]{{6,1,60}, {11,3,62}, {3,8,64}});
        } else if (levelName.contains("Desert")) {
            // Desert Storm decorations
            renderLevelDecorations(new int[][]{{2,3}, {6,3}, {10,3}, {14,3}, {2,8}, {6,8}, {10,8}, {14,8}, {1,1}, {15,1}, {1,11}, {15,11}}, 
                                  new int[][]{{3,2,61}, {8,2,63}, {13,2,65}, {3,9,67}, {8,9,60}, {13,9,62}});
        } else if (levelName.contains("Mountain")) {
            // Mountain Pass decorations
            renderLevelDecorations(new int[][]{{1,2}, {2,4}, {3,6}, {1,8}, {2,10}, {9,2}, {10,4}, {11,6}, {9,8}, {10,10}}, 
                                  new int[][]{{0,1,64}, {11,1,66}, {0,12,68}, {11,12,60}});
        }
    }
    
    /**
     * Render decorations for a specific level
     */
    private void renderLevelDecorations(int[][] rockPositions, int[][] crateData) {
        double tileSize = config.getTileSize();
        
        // Render rocks (sprite 59)
        for (int[] pos : rockPositions) {
            if (pos[0] < config.getGridCols() && pos[1] < config.getGridRows()) {
                renderSprite(59, pos[0] * tileSize + 16, pos[1] * tileSize + 16, 32, 32);
            }
        }
        
        // Render crates (various sprites 60-68)
        for (int[] crate : crateData) {
            if (crate[0] < config.getGridCols() && crate[1] < config.getGridRows()) {
                renderSprite(crate[2], crate[0] * tileSize + 8, crate[1] * tileSize + 8, 48, 48);
            }
        }
    }
    
    /**
     * Render the beautiful dirt path
     */
    private void renderPath() {
        if (gridMap == null) return;
        
        GridMap.Path mainPath = gridMap.getMainPath();
        if (mainPath == null) return;
        
        double tileSize = config.getTileSize();
        List<Math2D.Point> waypoints = mainPath.getWaypoints();
        
        // Render beautiful dirt path with proper tiles
        for (int i = 0; i < waypoints.size(); i++) {
            Math2D.Point waypoint = waypoints.get(i);
            int col = (int)(waypoint.x / tileSize);
            int row = (int)(waypoint.y / tileSize);
            
            double x = col * tileSize;
            double y = row * tileSize;
            
            // Determine path tile based on direction
            int pathTile = getPathTileForPosition(i, waypoints, col, row);
            renderSprite(pathTile, x, y, tileSize, tileSize);
            
            // Add rounded edges for path borders (optional enhancement)
            if (isPathCorner(i, waypoints)) {
                double rotation = getCornerRotation(i, waypoints);
                renderSpriteWithRotation(46, x, y, tileSize, tileSize, rotation);
            }
        }
        
        // Add subtle path glow effect
        gc.setStroke(Color.SADDLEBROWN.deriveColor(0, 1, 1, 0.3));
        gc.setLineWidth(8);
        
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
     * Render only the path glow line (for manual tiles)
     */
    private void renderPathGlow() {
        if (gridMap == null) return;
        
        GridMap.Path mainPath = gridMap.getMainPath();
        if (mainPath == null) return;
        
        List<Math2D.Point> waypoints = mainPath.getWaypoints();
        
        // Add subtle path glow effect
        gc.setStroke(Color.SADDLEBROWN.deriveColor(0, 1, 1, 0.3));
        gc.setLineWidth(8);
        
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
     * Check if a path tile is on the edge (next to grass)
     */
    private boolean isPathEdge(int col, int row, List<Math2D.Point> waypoints) {
        // Simple check - if it's at the border or corners of path
        return (col == 0 || col == config.getGridCols()-1 || row == 0 || row == config.getGridRows()-1);
    }
    
    /**
     * Get path tile for position based on direction
     */
    private int getPathTileForPosition(int index, List<Math2D.Point> waypoints, int col, int row) {
        Math2D.Point curr = waypoints.get(index);
        
        if (index == 0) {
            // Start position - check direction to next
            Math2D.Point next = waypoints.get(1);
            if (Math.abs(next.x - curr.x) > Math.abs(next.y - curr.y)) {
                // Horizontal start
                return row == 0 ? 1 : 47;
            } else {
                // Vertical start
                return 55;
            }
        }
        
        if (index == waypoints.size() - 1) {
            // End position - check direction from previous
            Math2D.Point prev = waypoints.get(index - 1);
            if (Math.abs(curr.x - prev.x) > Math.abs(curr.y - prev.y)) {
                // Horizontal end
                return row == 0 ? 1 : 47;
            } else {
                // Vertical end
                return 55;
            }
        }
        
        // Middle positions
        Math2D.Point prev = waypoints.get(index - 1);
        Math2D.Point next = waypoints.get(index + 1);
        
        // Check if horizontal or vertical
        boolean isHorizontal = Math.abs(curr.x - prev.x) > Math.abs(curr.y - prev.y) || 
                              Math.abs(next.x - curr.x) > Math.abs(next.y - curr.y);
        
        if (isHorizontal) {
            // Check if touching top edge (row 0)
            if (row == 0) {
                return 1; // Top edge horizontal path
            } else {
                return 47; // Regular horizontal path
            }
        } else {
            return 55; // Vertical path
        }
    }
    
    /**
     * Check if position is a path corner
     */
    private boolean isPathCorner(int index, List<Math2D.Point> waypoints) {
        if (index == 0 || index >= waypoints.size() - 1) return false;
        
        Math2D.Point prev = waypoints.get(index - 1);
        Math2D.Point curr = waypoints.get(index);
        Math2D.Point next = waypoints.get(index + 1);
        
        // Check if direction changes (corner)
        double dx1 = curr.x - prev.x;
        double dy1 = curr.y - prev.y;
        double dx2 = next.x - curr.x;
        double dy2 = next.y - curr.y;
        
        return Math.abs(dx1 * dx2 + dy1 * dy2) < 0.1; // Perpendicular = corner
    }
    
    /**
     * Get corner tile for path
     */
    /**
     * Get rotation angle for corner tile
     */
    private double getCornerRotation(int index, List<Math2D.Point> waypoints) {
        Math2D.Point prev = waypoints.get(index - 1);
        Math2D.Point curr = waypoints.get(index);
        Math2D.Point next = waypoints.get(index + 1);
        
        double dx1 = curr.x - prev.x;
        double dy1 = curr.y - prev.y;
        double dx2 = next.x - curr.x;
        double dy2 = next.y - curr.y;
        
        // Determine corner rotation based on direction change
        if (dx1 > 0 && dy2 > 0) {
            return 270; // Right to Down corner
        } else if (dx1 > 0 && dy2 < 0) {
            return 180; // Right to Up corner
        } else if (dx1 < 0 && dy2 > 0) {
            return 0; // Left to Down corner (no rotation)
        } else if (dx1 < 0 && dy2 < 0) {
            return 90; // Left to Up corner
        } else if (dy1 > 0 && dx2 > 0) {
            return 270; // Down to Right corner
        } else if (dy1 > 0 && dx2 < 0) {
            return 0; // Down to Left corner
        } else if (dy1 < 0 && dx2 > 0) {
            return 180; // Up to Right corner
        } else if (dy1 < 0 && dx2 < 0) {
            return 90; // Up to Left corner
        }
        
        return 0; // Default no rotation
    }
    
    /**
     * Get slot tile based on level
     */
    private int getBuildSlotTile() {
        String levelName = config.getLevelData().name;
        if (levelName.contains("Elite")) {
            return 39; // level1
        } else if (levelName.contains("Desert")) {
            return 108; // level2
        } else if (levelName.contains("Mountain")) {
            return 39; // level3 (default)
        }
        return 39; // default
    }
    
    private int getSpeedBumpSlotTile() {
        return 67; // Same for all levels
    }
    
    private int getBombSlotTile() {
        return 68; // Same for all levels
    }
    
    private int getOccupiedSlotTile() {
        String levelName = config.getLevelData().name;
        if (levelName.contains("Elite")) {
            return 119; // level1 - grass
        } else if (levelName.contains("Desert")) {
            return 29; // level2 - sand
        } else if (levelName.contains("Mountain")) {
            return 119; // level3 - grass
        }
        return 119; // default
    }
    
    /**
     * Render beautiful build slots with proper sprites
     */
    private void renderBuildSlots() {
        if (gridMap == null) return;
        
        double tileSize = config.getTileSize();
        
        // Build slots for towers - level-specific tiles
        for (var slot : gridMap.getBuildSlots()) {
            double x = slot.getCol() * tileSize;
            double y = slot.getRow() * tileSize;
            
            if (!slot.isOccupied()) {
                renderSprite(getBuildSlotTile(), x, y, tileSize, tileSize);
            } else {
                // Use level-appropriate background tile when occupied
                renderSprite(getOccupiedSlotTile(), x, y, tileSize, tileSize);
            }
        }
        
        // Speed bump slots - level-specific tiles
        for (var slot : gridMap.getSpeedBumpSlots()) {
            if (!slot.isOccupied()) {
                double x = slot.getCol() * tileSize;
                double y = slot.getRow() * tileSize;
                renderSprite(getSpeedBumpSlotTile(), x, y, tileSize, tileSize);
            }
        }
        
        // Bomb slots - level-specific tiles
        for (var slot : gridMap.getBombSlots()) {
            if (!slot.isOccupied()) {
                double x = slot.getCol() * tileSize;
                double y = slot.getRow() * tileSize;
                renderSprite(getBombSlotTile(), x, y, tileSize, tileSize);
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
            // Render aircraft even if destroying (for animation)
            boolean shouldRender = enemy.isAlive();
            if (enemy instanceof Aircraft aircraft && aircraft.isDestroying()) {
                shouldRender = true;
            }
            
            if (!shouldRender) continue;
            
            // Special rendering for different enemy types
            if (enemy instanceof Tank tank) {
                renderRotatedTank(tank);
            } else if (enemy instanceof Aircraft aircraft) {
                renderAircraft(aircraft);
            } else {
                // Draw regular enemy sprite with appropriate size
                int size = (enemy instanceof Tank) ? 64 : 40; // Tanks are 64x64, soldiers are 40x40
                renderSprite(enemy.getSpriteIndex(), 
                           enemy.getX() - size/2, enemy.getY() - size/2, size, size);
            }
            
            // Draw health bar
            renderHealthBar(enemy.getX(), enemy.getY() - 35, 
                          enemy.getCurrentHp(), enemy.getMaxHp());
            
            // Draw slow effect
            if (enemy.getSlowMultiplier() < 1.0) {
                gc.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.3));
                gc.fillOval(enemy.getX() - 20, enemy.getY() - 20, 40, 40);
            }
            
            // Draw attack range for tanks with current target
            if (enemy instanceof Tank tank && tank.getCurrentEnemyTarget() != null) {
                gc.setStroke(Color.RED.deriveColor(0, 1, 1, 0.3));
                gc.setLineWidth(1);
                double range = tank.getAttackRange() * config.getTileSize();
                gc.strokeOval(tank.getX() - range, tank.getY() - range, range * 2, range * 2);
            }
        }
    }
    
    /**
     * Render a tank with rotation animation
     */
    private void renderRotatedTank(Tank tank) {
        double x = tank.getX();
        double y = tank.getY();
        double bodyRotation = tank.getCurrentRotation();
        double turretRotation = tank.getTurretRotation();
        
        gc.save();
        
        // Render tank body with rotation (larger size)
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(bodyRotation)); // Adjust for sprite orientation
        renderSprite(tank.getSpriteIndex(), -32, -32, 64, 64); // 2x size for tanks
        gc.restore();
        
        // Render turret with separate rotation (if different sprite available)
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(turretRotation));
        
        // Draw turret cannon with attack animation
        if (tank.isAttacking()) {
            // Attack flash effect
            double intensity = tank.getAttackFlashIntensity();
            gc.setStroke(Color.ORANGE.deriveColor(0, 1, 1, intensity));
            gc.setLineWidth(5 + 3 * intensity);
            gc.strokeLine(0, 0, 25, 0);
            
            // Muzzle flash
            gc.setFill(Color.YELLOW.deriveColor(0, 1, 1, intensity * 0.8));
            gc.fillOval(20 - 4 * intensity, -4 * intensity, 8 * intensity, 8 * intensity);
        } else {
            gc.setStroke(Color.DARKRED);
            gc.setLineWidth(3);
            gc.strokeLine(0, 0, 20, 0);
        }
        
        gc.restore();
        
        // Draw targeting lines to current targets
        if (tank.getCurrentTowerTarget() != null) {
            Tower target = tank.getCurrentTowerTarget();
            if (target.isAlive()) {
                drawTargetingLine(x, y, target.getX(), target.getY(), Color.RED, "TOWER");
            }
        } else if (tank.getCurrentAATarget() != null) {
            AADefense target = tank.getCurrentAATarget();
            if (target.isAlive()) {
                drawTargetingLine(x, y, target.getX(), target.getY(), Color.PURPLE, "AA");
            }
        } else if (tank.getCurrentEnemyTarget() != null) {
            Enemy target = tank.getCurrentEnemyTarget();
            if (target.isAlive()) {
                drawTargetingLine(x, y, target.getX(), target.getY(), Color.ORANGE, "ENEMY");
            }
        }
    }
    
    /**
     * Draw targeting line with label
     */
    private void drawTargetingLine(double fromX, double fromY, double toX, double toY, Color color, String label) {
        // Targeting line
        gc.setStroke(color.deriveColor(0, 1, 1, 0.7));
        gc.setLineWidth(3);
        gc.strokeLine(fromX, fromY, toX, toY);
        
        // Crosshair on target
        gc.setStroke(color);
        gc.setLineWidth(3);
        gc.strokeLine(toX - 15, toY, toX + 15, toY);
        gc.strokeLine(toX, toY - 15, toX, toY + 15);
        
        // Label
        gc.setFill(color);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText(label, toX - 10, toY - 20);
    }
    
    /**
     * Render aircraft with flight path and strike preview
     */
    private void renderAircraft(Aircraft aircraft) {
        double x = aircraft.getX();
        double y = aircraft.getY();
        
        if (aircraft.isDestroying()) {
            // Destruction animation
            double progress = aircraft.getDestructionProgress();
            double alpha = aircraft.getFadeAlpha();
            
            gc.save();
            gc.setGlobalAlpha(alpha);
            
            // Spinning animation
            gc.translate(x, y);
            gc.rotate(progress * 720); // 2 full rotations during destruction
            
            // Aircraft sprite with larger size
            renderSprite(aircraft.getSpriteIndex(), -32, -32, 64, 64);
            
            // Explosion effect particles
            if (progress < 0.5) {
                gc.setFill(Color.ORANGE.deriveColor(0, 1, 1, alpha * 0.7));
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    double dist = progress * 40;
                    double px = Math.cos(angle) * dist;
                    double py = Math.sin(angle) * dist;
                    gc.fillOval(px - 3, py - 3, 6, 6);
                }
            }
            
            gc.restore();
        } else {
            // Normal aircraft rendering with rotation based on flight direction
            gc.save();
            gc.translate(x, y);
            
            // Rotate aircraft based on strike direction
            if (!aircraft.isRowStrike()) {
                // Column strike - aircraft flies vertically down, rotate 90 degrees
                gc.rotate(90);
            }
            
            // Aircraft sprite with larger size
            renderSprite(aircraft.getSpriteIndex(), -32, -32, 64, 64);
            
            gc.restore();
            
            // Aircraft shadow
            gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.3));
            gc.fillOval(x - 20, y - 20, 40, 40);
        
            // Draw strike warning line
            drawStrikeWarning(aircraft);
            
            // Aircraft trail effect
            gc.setStroke(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
            gc.setLineWidth(2);
            // Draw trail behind aircraft (adjusted for direction)
            double trailX, trailY;
            if (aircraft.isRowStrike()) {
                // Horizontal flight - trail behind
                trailX = x - 40;
                trailY = y;
            } else {
                // Vertical flight - trail above
                trailX = x;
                trailY = y - 40;
            }
            gc.strokeLine(trailX, trailY, x, y);
        }
    }
    
    /**
     * Draw strike warning line for aircraft (only before strike)
     */
    private void drawStrikeWarning(Aircraft aircraft) {
        // Only show warning line if aircraft hasn't struck yet
        if (!aircraft.hasTriggeredStrike()) {
            gc.setStroke(Color.RED.deriveColor(0, 1, 1, 0.6));
            gc.setLineWidth(3);
            gc.setLineDashes(8, 8);
            
            // Draw strike line across entire map
            if (aircraft.getSpriteIndex() == 271) { // Aircraft sprite
                // Draw warning line for the strike path
                gc.strokeLine(0, aircraft.getY(), config.getGridCols() * config.getTileSize(), aircraft.getY());
                
                // Add warning text
                gc.setFill(Color.RED);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                gc.fillText("⚠️ INCOMING AIRSTRIKE", 
                          config.getGridCols() * config.getTileSize() / 2 - 80, 
                          aircraft.getY() - 15);
            }
            
            gc.setLineDashes(null); // Reset line dashes
        }
    }
    
    /**
     * Render a regular tower with sprite rotation animation
     */
    private void renderRotatedTower(Tower tower, double rotation, Color cannonColor) {
        double x = tower.getX();
        double y = tower.getY();
        
        // Check if tower is firing
        boolean isFiring = false;
        double fireIntensity = 0.0;
        
        if (tower instanceof FastTower fastTower) {
            isFiring = fastTower.isFireFlashing();
            fireIntensity = fastTower.getFireFlashIntensity();
        } else if (tower instanceof PowerTower powerTower) {
            isFiring = powerTower.isFireFlashing();
            fireIntensity = powerTower.getFireFlashIntensity();
        }
        
        // Render rotating tower sprite
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(rotation) + 90); // Adjust for sprite orientation
        
        // Add glow effect when firing
        if (isFiring) {
            gc.setGlobalAlpha(1.0 + fireIntensity * 0.5); // Brighter when firing
        }
        
        // Render the tower sprite rotated
        renderSprite(tower.getSpriteIndex(), -24, -24, 48, 48);
        
        // Add muzzle flash effect
        if (isFiring) {
            gc.setFill(Color.YELLOW.deriveColor(0, 1, 1, fireIntensity));
            gc.fillOval(18, -4, 8 * fireIntensity, 8 * fireIntensity);
        }
        
        gc.restore();
        
        // Reset alpha
        gc.setGlobalAlpha(1.0);
    }
    
    /**
     * Render a tank tower with rotation animation
     */
    private void renderRotatedTankTower(TankTower tankTower) {
        double x = tankTower.getX();
        double y = tankTower.getY();
        double bodyRotation = tankTower.getCurrentRotation();
        double turretRotation = tankTower.getTurretRotation();
        
        gc.save();
        
        // Render tank tower body
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(bodyRotation));
        renderSprite(tankTower.getSpriteIndex(), -24, -24, 48, 48);
        gc.restore();
        
        // Render turret with separate rotation
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(turretRotation));
        
        // Draw turret cannon to show direction
        if (tankTower.isFireFlashing()) {
            // Firing animation - bright flash at cannon tip
            double intensity = tankTower.getFireFlashIntensity();
            gc.setStroke(Color.ORANGE.deriveColor(0, 1, 1, intensity));
            gc.setLineWidth(6 * intensity);
            gc.strokeLine(0, 0, 30, 0);
            
            // Muzzle flash effect
            gc.setFill(Color.YELLOW.deriveColor(0, 1, 1, intensity * 0.8));
            gc.fillOval(25 - 3 * intensity, -3 * intensity, 6 * intensity, 6 * intensity);
        } else {
            gc.setStroke(Color.DARKGREEN);
            gc.setLineWidth(4);
            gc.strokeLine(0, 0, 25, 0);
        }
        
        gc.restore();
        
        // Draw targeting line to current enemy target
        if (tankTower.getCurrentTarget() != null) {
            Enemy target = tankTower.getCurrentTarget();
            if (target.isAlive()) {
                double targetX = target.getX();
                double targetY = target.getY();
                
                gc.setStroke(Color.GREEN.deriveColor(0, 1, 1, 0.6));
                gc.setLineWidth(2);
                gc.strokeLine(x, y, targetX, targetY);
                
                // Draw crosshair on target
                gc.setStroke(Color.GREEN);
                gc.setLineWidth(2);
                gc.strokeLine(targetX - 10, targetY, targetX + 10, targetY);
                gc.strokeLine(targetX, targetY - 10, targetX, targetY + 10);
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
                
                // Special rendering for different tower types with rotation
                if (tower instanceof TankTower tankTower) {
                    renderRotatedTankTower(tankTower);
                } else if (tower instanceof FastTower fastTower) {
                    renderRotatedTower(fastTower, fastTower.getCurrentRotation(), Color.LIGHTGREEN);
                } else if (tower instanceof PowerTower powerTower) {
                    renderRotatedTower(powerTower, powerTower.getCurrentRotation(), Color.ORANGE);
                } else {
                    // Fallback regular tower rendering
                    renderSprite(tower.getSpriteIndex(), 
                               tower.getX() - 24, tower.getY() - 24, 48, 48);
                }
                
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
            
            if (projectile.isShowingHitEffect()) {
                // Render hit effect with animation
                double intensity = projectile.getHitEffectIntensity();
                gc.save();
                gc.translate(projectile.getX(), projectile.getY());
                
                // Pulsating effect
                double scale = 1.0 + (1.0 - intensity) * 1.5;
                gc.scale(scale, scale);
                
                // Fading alpha
                gc.setGlobalAlpha(intensity);
                
                renderSprite(projectile.getSpriteIndex(), -16, -16, 32, 32);
                
                gc.restore();
            } else {
                // Regular projectile
                int size = (projectile.getSpriteIndex() == 274) ? 12 : 8; // Tank shells bigger
                renderSprite(projectile.getSpriteIndex(), 
                           projectile.getX() - size, projectile.getY() - size, size * 2, size * 2);
            }
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
        renderSpriteWithRotation(spriteIndex, x, y, width, height, 0);
    }
    
    /**
     * Render a sprite with rotation
     */
    private void renderSpriteWithRotation(int spriteIndex, double x, double y, double width, double height, double rotationDegrees) {
        Image sprite = spriteLoader.getSprite(spriteIndex);
        if (sprite != null) {
            if (rotationDegrees != 0) {
                gc.save();
                gc.translate(x + width/2, y + height/2);
                gc.rotate(rotationDegrees);
                gc.drawImage(sprite, -width/2, -height/2, width, height);
                gc.restore();
            } else {
                gc.drawImage(sprite, x, y, width, height);
            }
        } else {
            // Debug: sprite not found
            System.out.println("Sprite " + spriteIndex + " not found!");
            // Draw a red rectangle as placeholder
            gc.setFill(javafx.scene.paint.Color.RED);
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
    
    /**
     * Zoom in
     */
    public void zoomIn() {
        zoomLevel = Math.min(MAX_ZOOM, zoomLevel + ZOOM_STEP);
    }
    
    /**
     * Zoom out
     */
    public void zoomOut() {
        zoomLevel = Math.max(MIN_ZOOM, zoomLevel - ZOOM_STEP);
    }
    
    /**
     * Reset zoom to default
     */
    public void resetZoom() {
        zoomLevel = 1.0;
    }
    
    /**
     * Handle mouse wheel zoom at center
     */
    public void handleZoom(double deltaY) {
        if (deltaY < 0) {
            zoomIn();
        } else {
            zoomOut();
        }
    }
    
    /**
     * Pan camera left
     */
    public void panLeft() {
        cameraX -= PAN_SPEED;
    }
    
    /**
     * Pan camera right
     */
    public void panRight() {
        cameraX += PAN_SPEED;
    }
    
    /**
     * Pan camera up
     */
    public void panUp() {
        cameraY -= PAN_SPEED;
    }
    
    /**
     * Pan camera down
     */
    public void panDown() {
        cameraY += PAN_SPEED;
    }
    
    /**
     * Reset camera position
     */
    public void resetCamera() {
        cameraX = 0.0;
        cameraY = 0.0;
        zoomLevel = 1.0;
    }
    
    /**
     * Render camera UI indicator
     */
    private void renderZoomUI() {
        // Camera info background
        gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.7));
        gc.fillRoundRect(5, canvas.getHeight() - 80, 200, 70, 8, 8);
        
        // Zoom level indicator
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText(String.format("🔍 Zoom: %.0f%%", zoomLevel * 100), 10, canvas.getHeight() - 55);
        
        // Camera position
        gc.setFill(Color.LIGHTBLUE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        gc.fillText(String.format("📍 Pos: (%.0f, %.0f)", cameraX, cameraY), 10, canvas.getHeight() - 40);
        
        // Controls hint
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        gc.fillText("🖱️ Wheel: Zoom | ⌨️ Arrows: Pan | R: Reset", 10, canvas.getHeight() - 25);
        gc.fillText("➕➖: Zoom | WASD: Pan", 10, canvas.getHeight() - 10);
    }
    
    /**
     * Get world coordinates from screen coordinates (accounting for zoom and pan)
     */
    public double[] screenToWorld(double screenX, double screenY) {
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        
        // Reverse the transformation
        double worldX = ((screenX - centerX) / zoomLevel) + centerX - cameraX;
        double worldY = ((screenY - centerY) / zoomLevel) + centerY - cameraY;
        
        return new double[]{worldX, worldY};
    }
    
    /**
     * Get current zoom level
     */
    public double getZoomLevel() {
        return zoomLevel;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}