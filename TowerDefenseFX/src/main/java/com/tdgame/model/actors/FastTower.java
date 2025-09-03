package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import java.util.List;
import java.util.ArrayList;

/**
 * Fast-firing tower with lower damage and HP.
 * Good against swarms of weak enemies.
 */
public class FastTower extends Tower {
    
    private List<Projectile> projectiles = new ArrayList<>();
    private double projectileSpeed;
    
    public FastTower(Balance.TowerStats stats, int spriteIndex, double projectileSpeed) {
        super(stats.range, stats.hp, stats.rpm / 60.0, stats.damage, spriteIndex);
        this.projectileSpeed = projectileSpeed;
    }
    
    @Override
    protected void fire(Enemy target) {
        if (target == null) return;
        
        // Create and fire projectile
        Projectile projectile = new Projectile(x, y, target, projectileSpeed, damage, 280);
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
        return 1.0 - (distance / range) + enemy.getPathProgress() * 0.5;
    }
    
    public List<Projectile> getProjectiles() {
        return projectiles;
    }
}