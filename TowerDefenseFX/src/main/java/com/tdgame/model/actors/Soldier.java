package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import com.tdgame.core.EventBus;

/**
 * Basic enemy soldier unit.
 * Standard ground unit with no special abilities.
 */
public class Soldier extends Enemy {
    
    public Soldier(Balance.EnemyStats stats) {
        super(stats.hp, stats.speed, stats.power, stats.spriteIndex);
    }
    
    @Override
    protected void updateSpecific(double deltaTime) {
        // Soldiers have no special behavior
    }
    
    @Override
    protected void onTakeDamage(int damage) {
        // Visual/audio feedback could go here
    }
    
    @Override
    protected void onDeath() {
        // Death effects, score points, etc.
        // Could emit death event for score/money rewards
    }
    
    @Override
    protected void onReachedEnd() {
        // Enemy reached the castle
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(power));
    }
}