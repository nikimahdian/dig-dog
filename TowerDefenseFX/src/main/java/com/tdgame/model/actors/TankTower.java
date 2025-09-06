package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import com.tdgame.util.Math2D;

import java.util.List;
import java.util.ArrayList;

/**
 * Tank tower - a defensive turret that can rotate and fire projectiles at enemies
 */
public class TankTower extends Tower {
    
    // Rotation and animation
    private double currentRotation = 0.0; // Current rotation angle in radians  
    private double targetRotation = 0.0; // Target rotation angle
    private final double rotationSpeed = 4.0; // Rotation speed in radians/second
    private double turretRotation = 0.0; // Separate turret rotation
    
    // Projectile system
    private List<Projectile> firedProjectiles = new ArrayList<>();
    
    // Firing animation
    private double fireFlashDuration = 0.0;
    private final double FIRE_FLASH_TIME = 0.15; // Duration of firing flash effect
    
    public TankTower(Balance.TowerStats stats) {
        super(stats.range, stats.hp, stats.rpm / 60.0, stats.damage, stats.spriteIndex);
    }
    
    @Override
    public void update(double deltaTime, List<Enemy> enemies) {
        if (!alive) return;
        
        // Update rotation animations
        updateRotation(deltaTime);
        updateTurretRotation(deltaTime);
        
        // Update projectiles
        updateProjectiles(deltaTime);
        
        // Update fire flash animation
        if (fireFlashDuration > 0) {
            fireFlashDuration -= deltaTime;
        }
        
        // Call parent update for targeting and firing
        super.update(deltaTime, enemies);
    }
    
    /**
     * Update tank body rotation (optional - can stay fixed)
     */
    private void updateRotation(double deltaTime) {
        // For now, keep tank body fixed, only turret rotates
    }
    
    /**
     * Update turret rotation towards current target
     */
    private void updateTurretRotation(double deltaTime) {
        if (currentTarget != null && currentTarget.isAlive()) {
            double targetX = currentTarget.getX();
            double targetY = currentTarget.getY();
            
            targetRotation = Math2D.angle(x, y, targetX, targetY);
            
            // Smooth turret rotation
            double rotationDiff = targetRotation - turretRotation;
            
            // Normalize angle difference to [-π, π]
            while (rotationDiff > Math.PI) rotationDiff -= 2 * Math.PI;
            while (rotationDiff < -Math.PI) rotationDiff += 2 * Math.PI;
            
            if (Math.abs(rotationDiff) > 0.05) {
                double rotationStep = rotationSpeed * deltaTime;
                if (Math.abs(rotationDiff) < rotationStep) {
                    turretRotation = targetRotation;
                } else {
                    turretRotation += Math.signum(rotationDiff) * rotationStep;
                }
            }
        }
    }
    
    /**
     * Update fired projectiles
     */
    public void updateProjectiles(double deltaTime) {
        for (int i = firedProjectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = firedProjectiles.get(i);
            projectile.update(deltaTime);
            
            if (!projectile.isActive() || projectile.hasHitTarget()) {
                firedProjectiles.remove(i);
            }
        }
    }
    
    @Override
    protected void fire(Enemy target) {
        if (target == null || !target.isAlive()) return;
        
        // Start fire flash animation
        fireFlashDuration = FIRE_FLASH_TIME;
        
        // Create projectile targeting the enemy
        double projectileSpeed = 300.0; // pixels per second
        int projectileSpriteIndex = 274; // Tank shell sprite (different from regular towers)
        
        Projectile projectile = new Projectile(x, y, target, projectileSpeed, damage, projectileSpriteIndex);
        firedProjectiles.add(projectile);
    }
    
    @Override
    protected void onDestroyed() {
        // Tank explosion effect
        firedProjectiles.clear();
    }
    
    // Getters for animation and rendering
    public double getCurrentRotation() { return currentRotation; }
    public double getTurretRotation() { return turretRotation; }
    public List<Projectile> getFiredProjectiles() { return firedProjectiles; }
    public boolean isFireFlashing() { return fireFlashDuration > 0; }
    public double getFireFlashIntensity() { 
        return fireFlashDuration > 0 ? fireFlashDuration / FIRE_FLASH_TIME : 0.0; 
    }
}