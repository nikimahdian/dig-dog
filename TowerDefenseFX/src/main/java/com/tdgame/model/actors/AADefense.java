package com.tdgame.model.actors;

import com.tdgame.util.Math2D;
import com.tdgame.util.RNG;

import java.util.List;

/**
 * Anti-aircraft defense system.
 * Can only target aircraft with a hit chance percentage.
 */
public class AADefense {
    private double x, y;
    private double hitChance;
    private double range;
    private int hp;
    private int maxHp;
    private int spriteIndex;
    
    private double fireCooldown = 0.0;
    private final double fireRate = 1.0; // 1 shot per second
    private Aircraft currentTarget = null;
    private boolean alive = true;
    private com.tdgame.model.grid.BuildSlot buildSlot = null;
    
    public AADefense(double hitChance, double range, int hp, int spriteIndex) {
        this.hitChance = hitChance;
        this.range = range;
        this.hp = hp;
        this.maxHp = hp;
        this.spriteIndex = spriteIndex;
    }
    
    /**
     * Update AA defense state
     */
    public void update(double deltaTime, List<Enemy> enemies) {
        if (!alive) return;
        
        fireCooldown -= deltaTime;
        
        // Find aircraft targets
        if (currentTarget == null || !isValidTarget(currentTarget)) {
            acquireTarget(enemies);
        }
        
        // Fire at aircraft if ready
        if (currentTarget != null && fireCooldown <= 0) {
            fire(currentTarget);
            fireCooldown = 1.0 / fireRate;
        }
    }
    
    /**
     * Acquire aircraft target
     */
    private void acquireTarget(List<Enemy> enemies) {
        currentTarget = null;
        double bestPriority = -1;
        
        for (Enemy enemy : enemies) {
            if (enemy instanceof Aircraft && isValidTarget((Aircraft)enemy)) {
                double distance = Math2D.distance(x, y, enemy.getX(), enemy.getY());
                double pixelRange = range * 64; // Convert tile range to pixels
                if (distance <= pixelRange) {
                    double priority = enemy.getPathProgress();
                    if (priority > bestPriority) {
                        bestPriority = priority;
                        currentTarget = (Aircraft)enemy;
                    }
                }
            }
        }
    }
    
    /**
     * Check if aircraft is a valid target
     */
    private boolean isValidTarget(Aircraft aircraft) {
        return aircraft != null && aircraft.isAlive() && !aircraft.hasReachedEnd();
    }
    
    /**
     * Fire at aircraft with hit chance
     */
    private void fire(Aircraft target) {
        if (target == null) return;
        
        System.out.println("🎯 AA firing at Aircraft! Hit chance: " + String.format("%.0f%%", hitChance * 100));
        
        // Roll for hit chance
        if (RNG.nextBoolean(hitChance)) {
            target.takeDamage(999); // AA weapons are lethal to aircraft
            System.out.println("💥 AA HIT! Aircraft destroyed!");
        } else {
            System.out.println("❌ AA MISS! Aircraft continues...");
        }
    }
    
    /**
     * Take damage (from tank attacks)
     */
    public void takeDamage(int damage) {
        if (!alive) return;
        
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            alive = false;
            onDestroyed();
        }
    }
    
    /**
     * Called when AA defense is destroyed
     */
    private void onDestroyed() {
        // Clear the build slot so it becomes available again
        if (buildSlot != null) {
            buildSlot.clearSlot();
        }
    }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void setBuildSlot(com.tdgame.model.grid.BuildSlot buildSlot) {
        this.buildSlot = buildSlot;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getHitChance() { return hitChance; }
    public double getRange() { return range; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getSpriteIndex() { return spriteIndex; }
    public boolean isAlive() { return alive; }
    public Aircraft getCurrentTarget() { return currentTarget; }
}