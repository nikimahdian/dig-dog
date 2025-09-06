package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import com.tdgame.core.EventBus;

/**
 * Fast soldier unit (Green/Grey sprite).
 * Lower HP but higher speed - good for rushing.
 */
public class SoldierFast extends Enemy {
    
    public SoldierFast(Balance.EnemyStats stats) {
        super(stats.hp, stats.speed, stats.power, stats.spriteIndex);
    }
    
    @Override
    protected void updateSpecific(double deltaTime) {
        // Fast soldiers have no special behavior
    }
    
    @Override
    protected void onTakeDamage(int damage) {
        // Visual/audio feedback for fast soldier hit
    }
    
    @Override
    protected void onDeath() {
        // Death effects for fast soldier
    }
    
    @Override
    protected void onReachedEnd() {
        // Fast soldier reached the castle
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(power));
    }
}