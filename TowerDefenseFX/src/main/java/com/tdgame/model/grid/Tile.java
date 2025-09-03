package com.tdgame.model.grid;

/**
 * Represents a single tile in the game grid.
 * Contains tile type, sprite information, and gameplay properties.
 */
public class Tile {
    
    public enum TileType {
        GROUND,     // Normal walkable/buildable terrain
        PATH,       // Enemy movement path
        BLOCKED,    // Impassable terrain
        CASTLE      // Castle/destination tile
    }
    
    private final TileType type;
    private final int spriteIndex;
    private final boolean walkable;
    private final boolean buildable;
    
    public Tile(TileType type, int spriteIndex) {
        this.type = type;
        this.spriteIndex = spriteIndex;
        
        // Set properties based on type
        this.walkable = (type == TileType.GROUND || type == TileType.PATH || type == TileType.CASTLE);
        this.buildable = (type == TileType.GROUND);
    }
    
    public TileType getType() {
        return type;
    }
    
    public int getSpriteIndex() {
        return spriteIndex;
    }
    
    public boolean isWalkable() {
        return walkable;
    }
    
    public boolean isBuildable() {
        return buildable;
    }
    
    public boolean isPath() {
        return type == TileType.PATH;
    }
    
    @Override
    public String toString() {
        return String.format("Tile{type=%s, sprite=%d}", type, spriteIndex);
    }
}