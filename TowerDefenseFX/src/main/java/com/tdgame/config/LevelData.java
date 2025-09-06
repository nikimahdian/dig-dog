package com.tdgame.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Level configuration data loaded from levels/*.json files.
 */
public class LevelData {
    
    @JsonProperty("name")
    public String name;
    
    @JsonProperty("tileset")
    public String tileset;
    
    @JsonProperty("grid")
    public GridConfig grid;
    
    @JsonProperty("buildSlots")
    public List<SlotPosition> buildSlots;
    
    @JsonProperty("speedBumpSlots")
    public List<SlotPosition> speedBumpSlots;
    
    @JsonProperty("bombSlots")
    public List<SlotPosition> bombSlots;
    
    @JsonProperty("paths")
    public List<PathData> paths;
    
    @JsonProperty("castle")
    public CastleData castle;
    
    @JsonProperty("manualTiles")
    public List<List<ManualTile>> manualTiles;
    
    
    public static class GridConfig {
        @JsonProperty("cols")
        public int cols;
        @JsonProperty("rows")
        public int rows;
        @JsonProperty("tileSize")
        public int tileSize;
    }
    
    public static class SlotPosition {
        @JsonProperty("col")
        public int col;
        @JsonProperty("row")
        public int row;
    }
    
    public static class PathData {
        @JsonProperty("name")
        public String name;
        @JsonProperty("waypoints")
        public List<int[]> waypoints;
    }
    
    public static class CastleData {
        @JsonProperty("col")
        public int col;
        @JsonProperty("row")
        public int row;
        @JsonProperty("hp")
        public int hp;
    }
    
    public static class ManualTile {
        @JsonProperty("tileIndex")
        public int tileIndex;
        @JsonProperty("rotation")
        public double rotation = 0.0;
        
        public ManualTile() {}
        
        public ManualTile(int tileIndex, double rotation) {
            this.tileIndex = tileIndex;
            this.rotation = rotation;
        }
        
        public ManualTile(int tileIndex) {
            this.tileIndex = tileIndex;
            this.rotation = 0.0;
        }
    }
}