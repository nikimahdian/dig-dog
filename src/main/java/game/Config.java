package game;

import java.util.Map;

public final class Config {
    
    // Map dimensions
    public static final int TILE_SIZE = 64;
    public static final int GRID_W = 11;
    public static final int GRID_H = 9;
    public static final int MAP_WIDTH = GRID_W * TILE_SIZE;
    public static final int MAP_HEIGHT = GRID_H * TILE_SIZE;
    
    // Economy
    public static final int STARTING_MONEY = 100;
    public static final double MONEY_TICK_SEC = 0.5;
    public static final double MONEY_PER_TICK = 5;
    
    // Fast Tower stats
    public static final int FAST_TOWER_COST = 50;
    public static final int FAST_TOWER_HP = 100;
    public static final double FAST_TOWER_RANGE = 3.0;
    public static final double FAST_TOWER_FIRE_RATE = 4.0;
    public static final int FAST_TOWER_DAMAGE_SOLDIER = 8;
    public static final int FAST_TOWER_DAMAGE_TANK = 4;
    public static final int FAST_TOWER_DAMAGE_PLANE = 0;
    
    // Heavy Tower stats
    public static final int HEAVY_TOWER_COST = 80;
    public static final int HEAVY_TOWER_HP = 220;
    public static final double HEAVY_TOWER_RANGE = 4.0;
    public static final double HEAVY_TOWER_FIRE_RATE = 1.0;
    public static final int HEAVY_TOWER_DAMAGE_SOLDIER = 20;
    public static final int HEAVY_TOWER_DAMAGE_TANK = 12;
    public static final int HEAVY_TOWER_DAMAGE_PLANE = 0;
    
    // Anti-Air stats
    public static final int AA_CHEAP_COST = 90;
    public static final double AA_CHEAP_HIT_CHANCE = 0.6;
    public static final int AA_EXPENSIVE_COST = 140;
    public static final double AA_EXPENSIVE_HIT_CHANCE = 0.8;
    
    // Enemy stats
    public static final int SOLDIER_HP = 40;
    public static final double SOLDIER_SPEED = 1.0;
    public static final int SOLDIER_POWER = 1;
    
    public static final int TANK_HP = 260;
    public static final double TANK_SPEED = 0.6;
    public static final int TANK_POWER = 2;
    public static final double TANK_RANGE = 3.0;
    public static final double TANK_FIRE_RATE = 1.0;
    public static final int TANK_DAMAGE = 12;
    
    public static final double PLANE_SPEED = 3.0;
    public static final int PLANE_POWER = 3;
    
    // Gadgets
    public static final int SPEED_BUMP_COST = 25;
    public static final double SPEED_BUMP_FACTOR = 0.5;
    public static final double SPEED_BUMP_LIFETIME_SEC = 12.0;
    
    public static final int BOMB_COST = 40;
    
    // Game rules
    public static final double LEAK_DEFEAT_RATIO = 0.10;
    public static final int TOTAL_WAVES = 6;
    public static final double WAVE_HP_SCALING = 0.10; // +10% per wave
    
    // Difficulty modifiers
    public static final double EASY_HP_BONUS = 0.20;
    public static final double HARD_HP_MULTIPLIER = 1.25;
    
    // Missing tower range constants
    public static final double TOWER_RANGE_FAST = FAST_TOWER_RANGE;
    public static final double TOWER_RANGE_HEAVY = HEAVY_TOWER_RANGE;
    
    // UI
    public static final int HUD_HEIGHT = 100;
    public static final int WINDOW_WIDTH = MAP_WIDTH;
    public static final int WINDOW_HEIGHT = MAP_HEIGHT + HUD_HEIGHT;
    
    // Asset paths mapping from the new prompt specification
    public static final Map<String, String> ASSET_MAP = Map.ofEntries(
        // Tiles
        Map.entry("grass", "Default size/towerDefense_tile074.png"),
        Map.entry("dirtH", "Default size/towerDefense_tile004.png"),
        Map.entry("dirtV", "Default size/towerDefense_tile009.png"),
        Map.entry("corner", "Default size/towerDefense_tile005.png"),
        Map.entry("sand", "Default size/towerDefense_tile029.png"),
        Map.entry("slot.build", "Default size/towerDefense_tile039.png"),
        Map.entry("slot.speed", "Default size/towerDefense_tile040.png"),
        Map.entry("slot.bomb", "Default size/towerDefense_tile092.png"),
        Map.entry("rock", "Default size/towerDefense_tile059.png"),
        
        // Towers
        Map.entry("tower.fast", "Default size/towerDefense_tile249.png"),
        Map.entry("tower.heavy", "Default size/towerDefense_tile250.png"),
        Map.entry("tower.aa60", "Default size/towerDefense_tile193.png"),
        Map.entry("tower.aa80", "Default size/towerDefense_tile194.png"),
        
        // Enemies
        Map.entry("enemy.soldier", "Default size/towerDefense_tile247.png"),
        Map.entry("enemy.tank", "Default size/towerDefense_tile269.png"),
        Map.entry("enemy.plane", "Default size/towerDefense_tile271.png"),
        
        // Friendlies (bonus)
        Map.entry("ally.tank", "Default size/towerDefense_tile268.png"),
        Map.entry("ally.plane", "Default size/towerDefense_tile270.png"),
        
        // Projectiles / FX
        Map.entry("proj.bullet", "Default size/towerDefense_tile272.png"),
        Map.entry("proj.tank", "Default size/towerDefense_tile274.png"),
        Map.entry("proj.aa60", "Default size/towerDefense_tile251.png"),
        Map.entry("proj.aa80", "Default size/towerDefense_tile252.png"),
        Map.entry("fx.hit.small", "Default size/towerDefense_tile285.png"),
        Map.entry("fx.hit.med", "Default size/towerDefense_tile286.png")
    );
    
    private Config() {
        // Utility class - no instantiation
    }
}