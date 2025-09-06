package com.tdgame.controller;

import com.tdgame.config.GameConfig;
import com.tdgame.config.Balance;
import com.tdgame.model.actors.*;
import com.tdgame.model.placeables.*;
import com.tdgame.model.grid.BuildSlot;
import com.tdgame.model.systems.EconomyManager;
import com.tdgame.model.systems.CombatSystem;

/**
 * Manages the building menu and construction of towers, AA defenses, and placeables.
 * Handles cost validation and placement logic.
 */
public class BuildMenuController {
    
    public enum BuildOption {
        FAST_TOWER,
        POWER_TOWER,
        TANK_TOWER,
        AA_60,
        AA_80,
        SPEED_BUMP,
        BOMB
    }
    
    private final GameConfig config;
    private final EconomyManager economyManager;
    private final CombatSystem combatSystem;
    
    public BuildMenuController(GameConfig config, EconomyManager economyManager, CombatSystem combatSystem) {
        this.config = config;
        this.economyManager = economyManager;
        this.combatSystem = combatSystem;
    }
    
    /**
     * Attempt to build a structure at the given slot
     */
    public boolean tryBuild(BuildOption option, BuildSlot slot) {
        if (slot == null || slot.isOccupied()) {
            return false;
        }
        
        int cost = getBuildCost(option);
        if (!economyManager.canAfford(cost)) {
            return false;
        }
        
        // Deduct cost
        if (!economyManager.spendMoney(cost)) {
            return false;
        }
        
        // Build the structure
        switch (option) {
            case FAST_TOWER -> buildFastTower(slot);
            case POWER_TOWER -> buildPowerTower(slot);
            case TANK_TOWER -> buildTankTower(slot);
            case AA_60 -> buildAA60(slot);
            case AA_80 -> buildAA80(slot);
            case SPEED_BUMP -> buildSpeedBump(slot);
            case BOMB -> buildBomb(slot);
        }
        
        return true;
    }
    
    /**
     * Build a fast tower
     */
    private void buildFastTower(BuildSlot slot) {
        Balance.TowerStats stats = config.getBalance().towers.fast;
        int spriteIndex = config.getBalance().sprites.towers.fast;
        double projectileSpeed = config.getBalance().projectiles.towerShot.speed;
        
        FastTower tower = new FastTower(stats, spriteIndex, projectileSpeed);
        tower.setPosition(slot.getWorldX(), slot.getWorldY());
        slot.placeTower(tower);
        combatSystem.addTower(tower);
    }
    
    /**
     * Build a power tower
     */
    private void buildPowerTower(BuildSlot slot) {
        Balance.TowerStats stats = config.getBalance().towers.power;
        int spriteIndex = config.getBalance().sprites.towers.power;
        double projectileSpeed = config.getBalance().projectiles.towerShot.speed;
        
        PowerTower tower = new PowerTower(stats, spriteIndex, projectileSpeed);
        tower.setPosition(slot.getWorldX(), slot.getWorldY());
        slot.placeTower(tower);
        combatSystem.addTower(tower);
    }
    
    /**
     * Build a tank tower
     */
    private void buildTankTower(BuildSlot slot) {
        Balance.TowerStats stats = config.getBalance().towers.tank;
        stats.spriteIndex = config.getBalance().sprites.towers.tank;
        
        TankTower tower = new TankTower(stats);
        tower.setPosition(slot.getWorldX(), slot.getWorldY());
        slot.placeTower(tower);
        combatSystem.addTower(tower);
    }
    
    /**
     * Build AA 60% defense
     */
    private void buildAA60(BuildSlot slot) {
        Balance.AAStats stats = config.getBalance().aa.aa60;
        int spriteIndex = 205; // AA 60% sprite
        
        AADefense aa = new AADefense(stats.hitChance, stats.range, stats.hp, spriteIndex);
        aa.setPosition(slot.getWorldX(), slot.getWorldY());
        slot.placeAADefense(aa);
        combatSystem.addAADefense(aa);
    }
    
    /**
     * Build AA 80% defense
     */
    private void buildAA80(BuildSlot slot) {
        Balance.AAStats stats = config.getBalance().aa.aa80;
        int spriteIndex = 206; // AA 80% sprite
        
        AADefense aa = new AADefense(stats.hitChance, stats.range, stats.hp, spriteIndex);
        aa.setPosition(slot.getWorldX(), slot.getWorldY());
        slot.placeAADefense(aa);
        combatSystem.addAADefense(aa);
    }
    
    /**
     * Build speed bump
     */
    private void buildSpeedBump(BuildSlot slot) {
        Balance.PlaceableStats stats = config.getBalance().placeables.speedBump;
        int spriteIndex = config.getBalance().sprites.placeables.speedBump;
        
        SpeedBump speedBump = new SpeedBump(
            slot.getWorldX(), slot.getWorldY(),
            stats.slowPct, stats.durationSec, spriteIndex
        );
        combatSystem.addSpeedBump(speedBump);
        // Don't mark slot as occupied for placeables - they can be placed anywhere valid
    }
    
    /**
     * Build bomb
     */
    private void buildBomb(BuildSlot slot) {
        Balance.PlaceableStats stats = config.getBalance().placeables.bomb;
        int spriteIndex = config.getBalance().sprites.placeables.bomb;
        
        Bomb bomb = new Bomb(
            slot.getWorldX(), slot.getWorldY(),
            stats.damage, stats.radius, spriteIndex
        );
        combatSystem.addBomb(bomb);
        // Don't mark slot as occupied for placeables
    }
    
    /**
     * Get the cost of a build option
     */
    public int getBuildCost(BuildOption option) {
        return switch (option) {
            case FAST_TOWER -> config.getBalance().towers.fast.cost;
            case POWER_TOWER -> config.getBalance().towers.power.cost;
            case TANK_TOWER -> config.getBalance().towers.tank.cost;
            case AA_60 -> config.getBalance().aa.aa60.cost;
            case AA_80 -> config.getBalance().aa.aa80.cost;
            case SPEED_BUMP -> config.getBalance().placeables.speedBump.cost;
            case BOMB -> config.getBalance().placeables.bomb.cost;
        };
    }
    
    /**
     * Check if player can afford a build option
     */
    public boolean canAfford(BuildOption option) {
        return economyManager.canAfford(getBuildCost(option));
    }
    
    /**
     * Get description of a build option
     */
    public String getDescription(BuildOption option) {
        return switch (option) {
            case FAST_TOWER -> "Fast Tower - High rate of fire, lower damage";
            case POWER_TOWER -> "Power Tower - High damage, slower rate of fire";
            case TANK_TOWER -> "Tank Tower - Heavy armor, rotating turret, high damage";
            case AA_60 -> "AA Defense - 60% hit chance against aircraft";
            case AA_80 -> "AA Defense - 80% hit chance against aircraft";
            case SPEED_BUMP -> "Speed Bump - Slows enemies for limited time";
            case BOMB -> "Bomb - One-time explosion damage";
        };
    }
}