package com.tdgame.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Wave configuration data loaded from waves/*.json files.
 */
public class WaveData {
    
    @JsonProperty("waves")
    public List<Wave> waves;
    
    public static class Wave {
        @JsonProperty("delay")
        public double delay;
        
        @JsonProperty("enemies")
        public List<EnemySpawn> enemies;
        
        @JsonProperty("aircraftChance")
        public Double aircraftChance; // Optional field
    }
    
    public static class EnemySpawn {
        @JsonProperty("type")
        public String type;
        
        @JsonProperty("count")
        public int count;
        
        @JsonProperty("gap")
        public double gap;
    }
}