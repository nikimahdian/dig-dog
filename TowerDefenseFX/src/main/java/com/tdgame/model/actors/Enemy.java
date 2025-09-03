package com.tdgame.model.actors;

import com.tdgame.util.Math2D;
import com.tdgame.model.grid.GridMap;

/**
 * Base class for all enemy units.
 * Handles movement along paths, health, and power tracking.
 */
public abstract class Enemy {
    
    protected double x, y;
    protected int maxHp;
    protected int currentHp;
    protected double baseSpeed;
    protected double currentSpeed;
    protected int power;
    protected int spriteIndex;
    
    protected double pathProgress = 0.0; // 0.0 to 1.0 along path
    protected GridMap.Path path;
    protected boolean alive = true;
    protected boolean reachedEnd = false;
    
    // Status effects
    protected double slowMultiplier = 1.0;
    protected double slowDuration = 0.0;
    
    public Enemy(int hp, double speed, int power, int spriteIndex) {
        this.maxHp = hp;
        this.currentHp = hp;
        this.baseSpeed = speed;
        this.currentSpeed = speed;
        this.power = power;
        this.spriteIndex = spriteIndex;
    }
    
    /**
     * Set the path this enemy will follow
     */
    public void setPath(GridMap.Path path) {
        this.path = path;
        if (path != null) {
            Math2D.Point startPos = path.getPositionAt(0.0);
            this.x = startPos.x;
            this.y = startPos.y;
        }
    }
    
    /**
     * Update enemy state
     */
    public void update(double deltaTime) {
        if (!alive || reachedEnd || path == null) return;
        
        // Update status effects
        updateStatusEffects(deltaTime);
        
        // Move along path
        moveAlongPath(deltaTime);
        
        // Check if reached end
        if (pathProgress >= 1.0) {
            reachedEnd = true;
            onReachedEnd();
        }
        
        // Custom update logic for subclasses
        updateSpecific(deltaTime);
    }
    
    /**
     * Move along the assigned path
     */
    protected void moveAlongPath(double deltaTime) {
        if (path == null) return;
        
        double distance = currentSpeed * deltaTime;
        double pathLength = path.getTotalLength();
        
        if (pathLength > 0) {
            pathProgress += distance / pathLength;
            pathProgress = Math.min(1.0, pathProgress);
            
            Math2D.Point newPos = path.getPositionAt(pathProgress);
            this.x = newPos.x;
            this.y = newPos.y;
        }
    }
    
    /**
     * Update status effects like slow
     */
    protected void updateStatusEffects(double deltaTime) {
        if (slowDuration > 0) {
            slowDuration -= deltaTime;
            if (slowDuration <= 0) {
                slowMultiplier = 1.0;
            }
        }
        
        currentSpeed = baseSpeed * slowMultiplier;
    }
    
    /**
     * Take damage
     */
    public void takeDamage(int damage) {
        if (!alive) return;
        
        currentHp -= damage;
        if (currentHp <= 0) {
            currentHp = 0;
            alive = false;
            onDeath();
        }
        
        onTakeDamage(damage);
    }
    
    /**
     * Apply slow effect
     */
    public void applySlow(double multiplier, double duration) {
        this.slowMultiplier = Math.min(this.slowMultiplier, multiplier);
        this.slowDuration = Math.max(this.slowDuration, duration);
    }
    
    /**
     * Check if enemy is in range of a position
     */
    public boolean isInRange(double px, double py, double range) {
        return Math2D.distance(x, y, px, py) <= range;
    }
    
    /**
     * Check if this enemy can be targeted by towers (most can, aircraft cannot)
     */
    public boolean canBeTargetedByTowers() {
        return true;
    }
    
    // Abstract/hook methods for subclasses
    protected abstract void updateSpecific(double deltaTime);
    protected abstract void onTakeDamage(int damage);
    protected abstract void onDeath();
    protected abstract void onReachedEnd();
    
    // Getters and setters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public double getSpeed() { return currentSpeed; }
    public double getBaseSpeed() { return baseSpeed; }
    public int getPower() { return power; }
    public int getSpriteIndex() { return spriteIndex; }
    public boolean isAlive() { return alive; }
    public boolean hasReachedEnd() { return reachedEnd; }
    public double getPathProgress() { return pathProgress; }
    public double getSlowMultiplier() { return slowMultiplier; }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}