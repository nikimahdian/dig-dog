package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import java.util.List;
import java.util.ArrayList;

/**
 * High-damage, slow-firing tower.
 * Good against heavily armored enemies.
 */
public class PowerTower extends Tower {
    
    private List<Projectile> projectiles = new ArrayList<>();
    private double projectileSpeed;
    
    public PowerTower(Balance.TowerStats stats, int spriteIndex, double projectileSpeed) {
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
        // Power towers prefer high-HP enemies to make use of their high damage
        double hpRatio = (double)enemy.getCurrentHp() / enemy.getMaxHp();
        return enemy.getPathProgress() + hpRatio * 0.5;
    }
    
    public List<Projectile> getProjectiles() {
        return projectiles;
    }
}