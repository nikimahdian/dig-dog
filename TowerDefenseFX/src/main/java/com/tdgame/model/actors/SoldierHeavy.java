package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import com.tdgame.core.EventBus;

/**
 * Heavy soldier unit (Blue/Red sprite).
 * Higher HP but slower speed - good for tanking.
 */
public class SoldierHeavy extends Enemy {
    
    public SoldierHeavy(Balance.EnemyStats stats) {
        super(stats.hp, stats.speed, stats.power, stats.spriteIndex);
    }
    
    @Override
    protected void updateSpecific(double deltaTime) {
        // Heavy soldiers have no special behavior
    }
    
    @Override
    protected void onTakeDamage(int damage) {
        // Visual/audio feedback for heavy soldier hit
    }
    
    @Override
    protected void onDeath() {
        // Death effects for heavy soldier
    }
    
    @Override
    protected void onReachedEnd() {
        // Heavy soldier reached the castle
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(power));
    }
}