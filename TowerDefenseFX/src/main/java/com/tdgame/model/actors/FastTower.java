package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import com.tdgame.util.Math2D;
import java.util.List;
import java.util.ArrayList;

/**
 * Fast-firing tower with lower damage and HP.
 * Good against swarms of weak enemies.
 */
public class FastTower extends Tower {
    
    private List<Projectile> projectiles = new ArrayList<>();
    private double projectileSpeed;
    
    // Rotation animation
    private double currentRotation = 0.0; // Current rotation angle in radians
    private double targetRotation = 0.0; // Target rotation angle
    private final double rotationSpeed = 5.0; // Fast rotation speed
    
    // Firing animation
    private double fireFlashDuration = 0.0;
    private final double FIRE_FLASH_TIME = 0.1; // Quick flash
    
    public FastTower(Balance.TowerStats stats, int spriteIndex, double projectileSpeed) {
        super(stats.range, stats.hp, stats.rpm / 60.0, stats.damage, spriteIndex);
        this.projectileSpeed = projectileSpeed;
    }
    
    @Override
    public void update(double deltaTime, List<Enemy> enemies) {
        if (!alive) return;
        
        // Update rotation animation
        updateRotation(deltaTime);
        
        // Update fire flash animation
        if (fireFlashDuration > 0) {
            fireFlashDuration -= deltaTime;
        }
        
        // Call parent update for targeting and firing
        super.update(deltaTime, enemies);
    }
    
    /**
     * Update rotation towards current target
     */
    private void updateRotation(double deltaTime) {
        if (currentTarget != null && currentTarget.isAlive()) {
            double targetX = currentTarget.getX();
            double targetY = currentTarget.getY();
            
            targetRotation = Math2D.angle(x, y, targetX, targetY);
            
            // Smooth rotation animation
            double rotationDiff = targetRotation - currentRotation;
            
            // Normalize angle difference to [-π, π]
            while (rotationDiff > Math.PI) rotationDiff -= 2 * Math.PI;
            while (rotationDiff < -Math.PI) rotationDiff += 2 * Math.PI;
            
            if (Math.abs(rotationDiff) > 0.05) {
                double rotationStep = rotationSpeed * deltaTime;
                if (Math.abs(rotationDiff) < rotationStep) {
                    currentRotation = targetRotation;
                } else {
                    currentRotation += Math.signum(rotationDiff) * rotationStep;
                }
            }
        }
    }
    
    @Override
    protected void fire(Enemy target) {
        if (target == null) return;
        
        // Start fire flash animation
        fireFlashDuration = FIRE_FLASH_TIME;
        
        // Create and fire projectile with correct sprite
        int projectileSprite = 272; // Tower projectile sprite
        Projectile projectile = new Projectile(x, y, target, projectileSpeed, damage, projectileSprite);
        projectiles.add(projectile);
    }
    
    /**
     * Update projectiles (called by combat system)
     */
    public void updateProjectiles(double deltaTime) {
        projectiles.removeIf(p -> {
            p.update(deltaTime);
            return !p.isActive();
        });
    }
    
    @Override
    protected double calculateTargetPriority(Enemy enemy, double distance) {
        // Fast towers prefer closer targets to maximize their rate of fire
        return enemy.getPathProgress() + (1.0 - distance / (range * 64)) * 0.5;
    }
    
    public List<Projectile> getProjectiles() {
        return projectiles;
    }
    
    // Getters for animation
    public double getCurrentRotation() { return currentRotation; }
    public boolean isFireFlashing() { return fireFlashDuration > 0; }
    public double getFireFlashIntensity() { 
        return fireFlashDuration > 0 ? fireFlashDuration / FIRE_FLASH_TIME : 0.0; 
    }
}