package com.tdgame.model.placeables;

import com.tdgame.model.actors.Enemy;
import com.tdgame.util.Math2D;

import java.util.List;

/**
 * One-time explosive that triggers when the first enemy gets within range.
 * Deals damage to all enemies within the explosion radius.
 */
public class Bomb {
    
    private final double x, y;
    private final int damage;
    private final double radius;
    private final int spriteIndex;
    
    private boolean armed = true;
    private boolean exploded = false;
    private double explosionTimer = 0.0;
    private final double explosionDuration = 0.5; // Visual explosion lasts 0.5 seconds
    
    public Bomb(double x, double y, int damage, double radius, int spriteIndex) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.radius = radius;
        this.spriteIndex = spriteIndex;
    }
    
    /**
     * Update bomb state and check for triggers
     */
    public void update(double deltaTime, List<Enemy> enemies) {
        if (exploded) {
            // Handle explosion visual timer
            explosionTimer += deltaTime;
            return;
        }
        
        if (!armed) return;
        
        // Check for enemies in trigger range
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && !enemy.hasReachedEnd()) {
                double distance = Math2D.distance(x, y, enemy.getX(), enemy.getY());
                double triggerRange = radius * 64; // Convert tile radius to pixels
                if (distance <= triggerRange) {
                    explode(enemies);
                    break;
                }
            }
        }
    }
    
    /**
     * Trigger the bomb explosion
     */
    private void explode(List<Enemy> enemies) {
        if (!armed || exploded) return;
        
        armed = false;
        exploded = true;
        
        // Deal damage to all enemies within explosion radius
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && !enemy.hasReachedEnd()) {
                double distance = Math2D.distance(x, y, enemy.getX(), enemy.getY());
                double explosionRange = radius * 64 * 1.2; // Larger explosion range to cover lanes
                if (distance <= explosionRange) {
                    enemy.takeDamage(damage);
                }
            }
        }
    }
    
    /**
     * Manually trigger the bomb (for testing or other mechanics)
     */
    public void trigger(List<Enemy> enemies) {
        explode(enemies);
    }
    
    /**
     * Check if the explosion visual effect is complete
     */
    public boolean isExplosionComplete() {
        return exploded && explosionTimer >= explosionDuration;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getDamage() { return damage; }
    public double getRadius() { return radius; }
    public int getSpriteIndex() { return spriteIndex; }
    public boolean isArmed() { return armed; }
    public boolean hasExploded() { return exploded; }
    public double getExplosionTimer() { return explosionTimer; }
    public double getExplosionProgress() { 
        return exploded ? Math.min(1.0, explosionTimer / explosionDuration) : 0.0; 
    }
}