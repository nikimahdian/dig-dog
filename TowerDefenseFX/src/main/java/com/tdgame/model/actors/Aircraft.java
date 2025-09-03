package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import com.tdgame.core.EventBus;

/**
 * Fast-moving aircraft enemy.
 * Triggers row/column strikes that damage player structures.
 */
public class Aircraft extends Enemy {
    
    private boolean hasTriggeredStrike = false;
    
    public Aircraft(Balance.EnemyStats stats) {
        super(0, stats.speed, stats.power, stats.spriteIndex); // Aircraft have no HP, can't be shot by towers
        this.currentHp = 1; // Set to 1 so they can be killed by AA
        this.maxHp = 1;
    }
    
    @Override
    protected void updateSpecific(double deltaTime) {
        // Trigger strike when aircraft reaches middle of path
        if (!hasTriggeredStrike && pathProgress >= 0.5) {
            triggerStrike();
            hasTriggeredStrike = true;
        }
    }
    
    /**
     * Trigger a row/column strike (handled by AircraftStrikeSystem)
     */
    private void triggerStrike() {
        // The AircraftStrikeSystem will handle the actual strike logic
        // This just marks that this aircraft has triggered its strike
    }
    
    @Override
    protected void onTakeDamage(int damage) {
        // Aircraft are fragile - any hit kills them
        currentHp = 0;
        alive = false;
        onDeath();
    }
    
    @Override
    protected void onDeath() {
        // Aircraft shot down - no strike triggered if killed before midpoint
        hasTriggeredStrike = true; // Prevent strike if shot down
    }
    
    @Override
    protected void onReachedEnd() {
        // Aircraft don't damage castle directly, but their power counts for victory conditions
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(0)); // No direct damage
    }
    
    /**
     * Check if this aircraft can be targeted by towers
     */
    @Override
    public boolean canBeTargetedByTowers() {
        return false; // Only AA can target aircraft
    }
    
    /**
     * Check if this aircraft can be targeted by AA defenses
     */
    public boolean canBeTargetedByAA() {
        return true;
    }
    
    public boolean hasTriggeredStrike() {
        return hasTriggeredStrike;
    }
    
    public void setTriggeredStrike(boolean triggered) {
        this.hasTriggeredStrike = triggered;
    }
}