package com.tdgame.model.placeables;

import com.tdgame.model.actors.Enemy;
import com.tdgame.util.Math2D;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Speed bump that slows enemies walking over it.
 * Has a limited duration and affects enemies within its tile.
 */
public class SpeedBump {
    
    private final double x, y;
    private final double slowMultiplier;
    private final double duration;
    private final double effectRadius;
    private final int spriteIndex;
    
    private double remainingTime;
    private boolean active = true;
    private Set<Enemy> affectedEnemies = new HashSet<>();
    
    public SpeedBump(double x, double y, double slowMultiplier, double duration, int spriteIndex) {
        this.x = x;
        this.y = y;
        this.slowMultiplier = slowMultiplier;
        this.duration = duration;
        this.remainingTime = duration;
        this.spriteIndex = spriteIndex;
        this.effectRadius = 48; // Larger radius to cover both lanes
    }
    
    /**
     * Update speed bump state and apply effects to nearby enemies
     */
    public void update(double deltaTime, List<Enemy> enemies) {
        if (!active) return;
        
        // Countdown duration
        remainingTime -= deltaTime;
        if (remainingTime <= 0) {
            active = false;
            return;
        }
        
        // Apply slow effect to enemies in range
        affectedEnemies.clear();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && !enemy.hasReachedEnd()) {
                double distance = Math2D.distance(x, y, enemy.getX(), enemy.getY());
                if (distance <= effectRadius) {
                    enemy.applySlow(slowMultiplier, 0.5); // Brief effect that refreshes while on bump
                    affectedEnemies.add(enemy);
                }
            }
        }
    }
    
    /**
     * Get remaining time as percentage (0.0 to 1.0)
     */
    public double getRemainingPercent() {
        return active ? (remainingTime / duration) : 0.0;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSlowMultiplier() { return slowMultiplier; }
    public double getRemainingTime() { return remainingTime; }
    public double getDuration() { return duration; }
    public int getSpriteIndex() { return spriteIndex; }
    public boolean isActive() { return active; }
    public Set<Enemy> getAffectedEnemies() { return affectedEnemies; }
}