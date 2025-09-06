package com.tdgame.model.actors;

import com.tdgame.util.Math2D;

/**
 * Represents a projectile fired by towers.
 * Handles movement and collision with targets.
 */
public class Projectile {
    
    private double x, y;
    private double targetX, targetY;
    private double speed;
    private int damage;
    private int spriteIndex;
    private Enemy target;
    
    private boolean active = true;
    private boolean hitTarget = false;
    private boolean showHitEffect = false;
    private double hitEffectDuration = 0.0;
    private final double HIT_EFFECT_TIME = 0.3;
    
    public Projectile(double startX, double startY, Enemy target, double speed, int damage, int spriteIndex) {
        this.x = startX;
        this.y = startY;
        this.target = target;
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.speed = speed;
        this.damage = damage;
        this.spriteIndex = spriteIndex;
    }
    
    /**
     * Update projectile movement and check for collision
     */
    public void update(double deltaTime) {
        // Update hit effect
        if (showHitEffect) {
            hitEffectDuration -= deltaTime;
            if (hitEffectDuration <= 0) {
                showHitEffect = false;
                active = false;
            }
            return;
        }
        
        if (!active || hitTarget) return;
        
        // Update target position if target is still alive
        if (target != null && target.isAlive()) {
            targetX = target.getX();
            targetY = target.getY();
        }
        
        // Move towards target
        double distance = Math2D.distance(x, y, targetX, targetY);
        if (distance <= speed * deltaTime || distance < 5.0) {
            // Hit the target
            hitTarget = true;
            onHit();
        } else {
            // Move towards target
            double angle = Math2D.angle(x, y, targetX, targetY);
            x += Math.cos(angle) * speed * deltaTime;
            y += Math.sin(angle) * speed * deltaTime;
        }
    }
    
    /**
     * Handle projectile hitting its target
     */
    private void onHit() {
        if (target != null && target.isAlive()) {
            target.takeDamage(damage);
        }
        
        // Start hit effect
        showHitEffect = true;
        hitEffectDuration = HIT_EFFECT_TIME;
        
        // Move to target position for hit effect
        x = targetX;
        y = targetY;
    }
    
    /**
     * Manually destroy the projectile
     */
    public void destroy() {
        active = false;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSpriteIndex() { 
        if (showHitEffect) {
            return 285; // Hit effect sprite
        }
        return spriteIndex; 
    }
    public boolean isActive() { return active; }
    public boolean hasHitTarget() { return hitTarget; }
    public Enemy getTarget() { return target; }
    public boolean isShowingHitEffect() { return showHitEffect; }
    public double getHitEffectIntensity() { 
        return showHitEffect ? hitEffectDuration / HIT_EFFECT_TIME : 0.0;
    }
}