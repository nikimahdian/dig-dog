package com.tdgame.model.grid;

import com.tdgame.config.GameConfig;
import com.tdgame.config.LevelData;
import com.tdgame.util.Math2D;
import com.tdgame.util.RNG;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents the game grid containing tiles, build slots, paths, and collision detection.
 * Manages the spatial layout of the game world.
 */
public class GridMap {
    
    private final int cols;
    private final int rows;
    private final double tileSize;
    private final Tile[][] tiles;
    private final List<BuildSlot> buildSlots;
    private final List<BuildSlot> speedBumpSlots;
    private final List<BuildSlot> bombSlots;
    private final List<Path> paths;
    private final Math2D.Point castlePosition;
    
    public GridMap(GameConfig config) {
        LevelData levelData = config.getLevelData();
        this.cols = levelData.grid.cols;
        this.rows = levelData.grid.rows;
        this.tileSize = levelData.grid.tileSize;
        
        // Initialize tiles with procedural generation
        this.tiles = generateTiles(config);
        
        // Create build slots
        this.buildSlots = new ArrayList<>();
        for (LevelData.SlotPosition slot : levelData.buildSlots) {
            buildSlots.add(new BuildSlot(slot.col, slot.row, tileSize));
        }
        
        // Create speed bump slots
        this.speedBumpSlots = new ArrayList<>();
        for (LevelData.SlotPosition slot : levelData.speedBumpSlots) {
            speedBumpSlots.add(new BuildSlot(slot.col, slot.row, tileSize));
        }
        
        // Create bomb slots
        this.bombSlots = new ArrayList<>();
        for (LevelData.SlotPosition slot : levelData.bombSlots) {
            bombSlots.add(new BuildSlot(slot.col, slot.row, tileSize));
        }
        
        // Create paths
        this.paths = new ArrayList<>();
        for (LevelData.PathData pathData : levelData.paths) {
            List<Math2D.Point> waypoints = new ArrayList<>();
            for (int[] wp : pathData.waypoints) {
                waypoints.add(gridToWorld(wp[0], wp[1]));
            }
            paths.add(new Path(pathData.name, waypoints));
        }
        
        // Castle position
        this.castlePosition = gridToWorld(levelData.castle.col, levelData.castle.row);
    }
    
    /**
     * Generate tiles procedurally based on paths and configuration
     */
    private Tile[][] generateTiles(GameConfig config) {
        Tile[][] grid = new Tile[rows][cols];
        LevelData levelData = config.getLevelData();
        
        // Initialize all as ground tiles
        int[] groundSprites = config.getBalance().sprites.ground;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int spriteIndex = RNG.choice(groundSprites);
                grid[row][col] = new Tile(Tile.TileType.GROUND, spriteIndex);
            }
        }
        
        // Set path tiles
        int[] pathSprites = config.getBalance().sprites.path;
        for (LevelData.PathData pathData : levelData.paths) {
            for (int[] wp : pathData.waypoints) {
                int col = wp[0];
                int row = wp[1];
                if (isValidGridPosition(col, row)) {
                    int spriteIndex = RNG.choice(pathSprites);
                    grid[row][col] = new Tile(Tile.TileType.PATH, spriteIndex);
                }
            }
            
            // Fill in path between waypoints
            for (int i = 0; i < pathData.waypoints.size() - 1; i++) {
                int[] from = pathData.waypoints.get(i);
                int[] to = pathData.waypoints.get(i + 1);
                fillPathBetween(grid, from[0], from[1], to[0], to[1], pathSprites);
            }
        }
        
        // Set castle tile
        int castleSprite = config.getBalance().sprites.castle[0];
        if (isValidGridPosition(levelData.castle.col, levelData.castle.row)) {
            grid[levelData.castle.row][levelData.castle.col] = 
                new Tile(Tile.TileType.CASTLE, castleSprite);
        }
        
        return grid;
    }
    
    /**
     * Fill path tiles between two points
     */
    private void fillPathBetween(Tile[][] grid, int x1, int y1, int x2, int y2, int[] pathSprites) {
        // Simple line drawing for path
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int x = x1;
        int y = y1;
        int n = 1 + dx + dy;
        int x_inc = (x2 > x1) ? 1 : -1;
        int y_inc = (y2 > y1) ? 1 : -1;
        int error = dx - dy;
        
        dx *= 2;
        dy *= 2;
        
        for (; n > 0; --n) {
            if (isValidGridPosition(x, y)) {
                int spriteIndex = RNG.choice(pathSprites);
                grid[y][x] = new Tile(Tile.TileType.PATH, spriteIndex);
            }
            
            if (error > 0) {
                x += x_inc;
                error -= dy;
            } else {
                y += y_inc;
                error += dx;
            }
        }
    }
    
    /**
     * Convert grid coordinates to world coordinates
     */
    public Math2D.Point gridToWorld(int col, int row) {
        return new Math2D.Point(col * tileSize + tileSize / 2, row * tileSize + tileSize / 2);
    }
    
    /**
     * Convert world coordinates to grid coordinates
     */
    public Math2D.Point worldToGrid(double x, double y) {
        return new Math2D.Point((int)(x / tileSize), (int)(y / tileSize));
    }
    
    /**
     * Check if grid position is valid
     */
    public boolean isValidGridPosition(int col, int row) {
        return col >= 0 && col < cols && row >= 0 && row < rows;
    }
    
    /**
     * Get tile at grid position
     */
    public Tile getTile(int col, int row) {
        if (isValidGridPosition(col, row)) {
            return tiles[row][col];
        }
        return null;
    }
    
    /**
     * Get tile at world position
     */
    public Tile getTileAtWorld(double x, double y) {
        Math2D.Point grid = worldToGrid(x, y);
        return getTile((int)grid.x, (int)grid.y);
    }
    
    /**
     * Find the closest build slot to a world position
     */
    public BuildSlot findBuildSlotAt(double x, double y, double tolerance) {
        for (BuildSlot slot : buildSlots) {
            if (Math2D.distance(x, y, slot.getWorldX(), slot.getWorldY()) <= tolerance) {
                return slot;
            }
        }
        return null;
    }
    
    /**
     * Find speed bump slot at position
     */
    public BuildSlot findSpeedBumpSlotAt(double x, double y, double tolerance) {
        for (BuildSlot slot : speedBumpSlots) {
            if (Math2D.distance(x, y, slot.getWorldX(), slot.getWorldY()) <= tolerance) {
                return slot;
            }
        }
        return null;
    }
    
    /**
     * Find bomb slot at position
     */
    public BuildSlot findBombSlotAt(double x, double y, double tolerance) {
        for (BuildSlot slot : bombSlots) {
            if (Math2D.distance(x, y, slot.getWorldX(), slot.getWorldY()) <= tolerance) {
                return slot;
            }
        }
        return null;
    }
    
    /**
     * Get the main enemy path (assumes first path is main)
     */
    public Path getMainPath() {
        return paths.isEmpty() ? null : paths.get(0);
    }
    
    // Getters
    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public double getTileSize() { return tileSize; }
    public List<BuildSlot> getBuildSlots() { return buildSlots; }
    public List<BuildSlot> getSpeedBumpSlots() { return speedBumpSlots; }
    public List<BuildSlot> getBombSlots() { return bombSlots; }
    public List<Path> getPaths() { return paths; }
    public Math2D.Point getCastlePosition() { return castlePosition; }
    public Tile[][] getTiles() { return tiles; }
    
    /**
     * Inner class representing a path through the map
     */
    public static class Path {
        private final String name;
        private final List<Math2D.Point> waypoints;
        
        public Path(String name, List<Math2D.Point> waypoints) {
            this.name = name;
            this.waypoints = waypoints;
        }
        
        public String getName() { return name; }
        public List<Math2D.Point> getWaypoints() { return waypoints; }
        
        /**
         * Get position along path at given progress (0.0 to 1.0)
         */
        public Math2D.Point getPositionAt(double progress) {
            if (waypoints.isEmpty()) return new Math2D.Point(0, 0);
            if (progress <= 0) return waypoints.get(0);
            if (progress >= 1) return waypoints.get(waypoints.size() - 1);
            
            double totalLength = getTotalLength();
            double targetDistance = progress * totalLength;
            double currentDistance = 0;
            
            for (int i = 0; i < waypoints.size() - 1; i++) {
                Math2D.Point from = waypoints.get(i);
                Math2D.Point to = waypoints.get(i + 1);
                double segmentLength = from.distanceTo(to);
                
                if (currentDistance + segmentLength >= targetDistance) {
                    double segmentProgress = (targetDistance - currentDistance) / segmentLength;
                    return new Math2D.Point(
                        Math2D.lerp(from.x, to.x, segmentProgress),
                        Math2D.lerp(from.y, to.y, segmentProgress)
                    );
                }
                
                currentDistance += segmentLength;
            }
            
            return waypoints.get(waypoints.size() - 1);
        }
        
        /**
         * Get total path length
         */
        public double getTotalLength() {
            double length = 0;
            for (int i = 0; i < waypoints.size() - 1; i++) {
                length += waypoints.get(i).distanceTo(waypoints.get(i + 1));
            }
            return length;
        }
    }
}