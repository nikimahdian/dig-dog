package com.tdgame.model.actors;

import com.tdgame.util.Math2D;

import java.util.List;

/**
 * Base class for all tower defense structures.
 * Handles targeting, shooting, and health management.
 */
public abstract class Tower {
    protected double x, y;
    protected double range;
    protected int hp;
    protected int maxHp;
    protected double fireRate; // shots per second
    protected int damage;
    protected int spriteIndex;
    
    protected double fireCooldown = 0.0;
    protected Enemy currentTarget = null;
    protected boolean alive = true;
    protected com.tdgame.model.grid.BuildSlot buildSlot = null;
    
    public Tower(double range, int hp, double fireRate, int damage, int spriteIndex) {
        this.range = range;
        this.hp = hp;
        this.maxHp = hp;
        // fireRate is already in shots per second, no conversion needed
        this.fireRate = fireRate;
        this.damage = damage;
        this.spriteIndex = spriteIndex;
    }
    
    /**
     * Update tower state
     */
    public void update(double deltaTime, List<Enemy> enemies) {
        if (!alive) return;
        
        fireCooldown -= deltaTime;
        
        // Acquire target if we don't have one or current target is invalid
        if (currentTarget == null || !isValidTarget(currentTarget)) {
            acquireTarget(enemies);
        }
        
        // Fire at target if ready
        if (currentTarget != null && fireCooldown <= 0) {
            fire(currentTarget);
            fireCooldown = 1.0 / fireRate;
        }
    }
    
    /**
     * Acquire the best target from available enemies
     */
    protected void acquireTarget(List<Enemy> enemies) {
        currentTarget = null;
        double bestPriority = -1;
        
        // Convert tile range to pixel range (64 pixels per tile)
        double pixelRange = range * 64.0;
        
        for (Enemy enemy : enemies) {
            if (!isValidTarget(enemy)) continue;
            
            double distance = Math2D.distance(x, y, enemy.getX(), enemy.getY());
            if (distance <= pixelRange) {
                double priority = calculateTargetPriority(enemy, distance);
                if (priority > bestPriority) {
                    bestPriority = priority;
                    currentTarget = enemy;
                }
            }
        }
    }
    
    /**
     * Check if an enemy is a valid target
     */
    protected boolean isValidTarget(Enemy enemy) {
        return enemy != null && enemy.isAlive() && !enemy.hasReachedEnd() && 
               enemy.canBeTargetedByTowers();
    }
    
    /**
     * Calculate targeting priority for an enemy
     * Higher values = higher priority
     */
    protected double calculateTargetPriority(Enemy enemy, double distance) {
        // Default: prioritize enemies closer to the end of their path
        return enemy.getPathProgress();
    }
    
    /**
     * Fire at the target
     */
    protected abstract void fire(Enemy target);
    
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
     * Called when tower is destroyed
     */
    protected void onDestroyed() {
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
    public double getRange() { return range; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public double getFireRate() { return fireRate; }
    public int getDamage() { return damage; }
    public int getSpriteIndex() { return spriteIndex; }
    public boolean isAlive() { return alive; }
    public Enemy getCurrentTarget() { return currentTarget; }
}