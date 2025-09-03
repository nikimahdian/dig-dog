package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import com.tdgame.core.EventBus;
import com.tdgame.util.Math2D;

import java.util.List;
import java.util.ArrayList;

/**
 * Heavy armored enemy tank.
 * Can shoot at nearby defenses while moving.
 */
public class Tank extends Enemy {
    
    private final double dpsVsDefenses;
    private double shootCooldown = 0.0;
    private final double shootInterval = 1.0; // Shoot every second
    private final double attackRange = 2.0; // Range to attack defenses
    
    private List<Tower> nearbyTowers = new ArrayList<>();
    private List<AADefense> nearbyAA = new ArrayList<>();
    
    public Tank(Balance.EnemyStats stats) {
        super(stats.hp, stats.speed, stats.power, stats.spriteIndex);
        this.dpsVsDefenses = stats.dpsVsDefenses != null ? stats.dpsVsDefenses : 0.0;
    }
    
    @Override
    protected void updateSpecific(double deltaTime) {
        shootCooldown -= deltaTime;
        
        if (shootCooldown <= 0) {
            attackNearbyDefenses();
            shootCooldown = shootInterval;
        }
    }
    
    /**
     * Attack towers and AA defenses within range
     */
    private void attackNearbyDefenses() {
        // Attack nearby towers
        for (Tower tower : nearbyTowers) {
            if (Math2D.distance(x, y, tower.getX(), tower.getY()) <= attackRange) {
                int damage = (int)(dpsVsDefenses * shootInterval);
                tower.takeDamage(damage);
                break; // Attack one target per shot
            }
        }
        
        // Attack nearby AA defenses
        for (AADefense aa : nearbyAA) {
            if (Math2D.distance(x, y, aa.getX(), aa.getY()) <= attackRange) {
                int damage = (int)(dpsVsDefenses * shootInterval);
                aa.takeDamage(damage);
                break; // Attack one target per shot
            }
        }
    }
    
    /**
     * Update list of nearby defenses (called by combat system)
     */
    public void updateNearbyDefenses(List<Tower> towers, List<AADefense> aaDefenses) {
        nearbyTowers.clear();
        nearbyAA.clear();
        
        for (Tower tower : towers) {
            if (tower.isAlive() && Math2D.distance(x, y, tower.getX(), tower.getY()) <= attackRange * 1.5) {
                nearbyTowers.add(tower);
            }
        }
        
        for (AADefense aa : aaDefenses) {
            if (aa.isAlive() && Math2D.distance(x, y, aa.getX(), aa.getY()) <= attackRange * 1.5) {
                nearbyAA.add(aa);
            }
        }
    }
    
    @Override
    protected void onTakeDamage(int damage) {
        // Tanks are tougher, maybe different hit effect
    }
    
    @Override
    protected void onDeath() {
        // Tank explosion effect, higher score reward
    }
    
    @Override
    protected void onReachedEnd() {
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(power));
    }
    
    public double getAttackRange() { return attackRange; }
    public double getDpsVsDefenses() { return dpsVsDefenses; }
}