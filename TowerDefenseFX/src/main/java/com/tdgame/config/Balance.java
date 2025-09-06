package com.tdgame.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Game balance configuration loaded from balance.json.
 * Contains all numeric tunables for gameplay.
 */
public class Balance {
    
    @JsonProperty("money")
    public MoneyConfig money;
    
    @JsonProperty("towers")
    public TowersConfig towers;
    
    @JsonProperty("aa")
    public AAConfig aa;
    
    @JsonProperty("placeables")
    public PlaceablesConfig placeables;
    
    @JsonProperty("enemies")
    public EnemiesConfig enemies;
    
    @JsonProperty("projectiles")
    public ProjectilesConfig projectiles;
    
    @JsonProperty("rules")
    public RulesConfig rules;
    
    @JsonProperty("sprites")
    public SpritesConfig sprites;
    
    public static class MoneyConfig {
        @JsonProperty("start")
        public int start;
        @JsonProperty("incomePerSec")
        public int incomePerSec;
    }
    
    public static class TowersConfig {
        @JsonProperty("fast")
        public TowerStats fast;
        @JsonProperty("power")
        public TowerStats power;
        @JsonProperty("tank")
        public TowerStats tank;
    }
    
    public static class TowerStats {
        @JsonProperty("cost")
        public int cost;
        @JsonProperty("range")
        public double range;
        @JsonProperty("rpm")
        public int rpm;
        @JsonProperty("fireRate")
        public Double fireRate;
        @JsonProperty("damage")
        public int damage;
        @JsonProperty("hp")
        public int hp;
        @JsonProperty("spriteIndex")
        public Integer spriteIndex;
    }
    
    public static class AAConfig {
        @JsonProperty("aa60")
        public AAStats aa60;
        @JsonProperty("aa80")
        public AAStats aa80;
    }
    
    public static class AAStats {
        @JsonProperty("cost")
        public int cost;
        @JsonProperty("hitChance")
        public double hitChance;
        @JsonProperty("range")
        public Double range;
        @JsonProperty("hp")
        public Integer hp;
        @JsonProperty("spriteIndex")
        public Integer spriteIndex;
    }
    
    public static class PlaceablesConfig {
        @JsonProperty("speedBump")
        public PlaceableStats speedBump;
        @JsonProperty("bomb")
        public PlaceableStats bomb;
    }
    
    public static class PlaceableStats {
        @JsonProperty("cost")
        public int cost;
        @JsonProperty("slowPct")
        public Double slowPct;
        @JsonProperty("durationSec")
        public Double durationSec;
        @JsonProperty("damage")
        public Integer damage;
        @JsonProperty("radius")
        public Double radius;
        @JsonProperty("spriteIndex")
        public Integer spriteIndex;
    }
    
    public static class EnemiesConfig {
        @JsonProperty("soldier")
        public EnemyStats soldier;
        @JsonProperty("soldierFast")
        public EnemyStats soldierFast;
        @JsonProperty("soldierHeavy")
        public EnemyStats soldierHeavy;
        @JsonProperty("tank")
        public EnemyStats tank;
        @JsonProperty("aircraft")
        public EnemyStats aircraft;
    }
    
    public static class EnemyStats {
        @JsonProperty("hp")
        public Integer hp;
        @JsonProperty("speed")
        public double speed;
        @JsonProperty("power")
        public int power;
        @JsonProperty("dpsVsDefenses")
        public Integer dpsVsDefenses;
        @JsonProperty("range")
        public Integer range;
        @JsonProperty("fireRate")
        public Double fireRate;
        @JsonProperty("spriteIndex")
        public int spriteIndex;
    }
    
    public static class ProjectilesConfig {
        @JsonProperty("towerShot")
        public ProjectileStats towerShot;
        @JsonProperty("bullet")
        public ProjectileStats bullet;
        @JsonProperty("tankShell")
        public ProjectileStats tankShell;
        @JsonProperty("aa60")
        public ProjectileStats aa60;
        @JsonProperty("aa80")
        public ProjectileStats aa80;
    }
    
    public static class ProjectileStats {
        @JsonProperty("speed")
        public double speed;
        @JsonProperty("spriteIndex")
        public int spriteIndex;
    }
    
    public static class RulesConfig {
        @JsonProperty("leakPctDefeat")
        public double leakPctDefeat;
    }
    
    public static class SpritesConfig {
        @JsonProperty("ground")
        public int[] ground;
        @JsonProperty("path")
        public int[] path;
        @JsonProperty("sand")
        public int[] sand;
        @JsonProperty("buildSlot")
        public int[] buildSlot;
        @JsonProperty("speedSlot")
        public int[] speedSlot;
        @JsonProperty("bombSlot")
        public int[] bombSlot;
        @JsonProperty("rock")
        public int[] rock;
        @JsonProperty("blocked")
        public int[] blocked;
        @JsonProperty("castle")
        public int[] castle;
        @JsonProperty("towers")
        public TowerSprites towers;
        @JsonProperty("aa")
        public AASprites aa;
        @JsonProperty("placeables")
        public PlaceableSprites placeables;
    }
    
    public static class TowerSprites {
        @JsonProperty("fast")
        public int fast;
        @JsonProperty("power")
        public int power;
        @JsonProperty("tank")
        public int tank;
    }
    
    public static class AASprites {
        @JsonProperty("aa60")
        public int aa60;
        @JsonProperty("aa80")
        public int aa80;
    }
    
    public static class PlaceableSprites {
        @JsonProperty("speedBump")
        public int speedBump;
        @JsonProperty("bomb")
        public int bomb;
    }
}