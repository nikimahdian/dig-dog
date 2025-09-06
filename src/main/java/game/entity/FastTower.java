package game.entity;

import game.Config;

public class FastTower extends Tower {
    
    public FastTower(double x, double y) {
        super(x, y, Config.FAST_TOWER_HP, Config.TOWER_RANGE_FAST, 
              Config.FAST_TOWER_FIRE_RATE, Config.FAST_TOWER_COST);
    }
    
    @Override
    protected double getDamageAgainst(Enemy enemy) {
        if (enemy instanceof Soldier) {
            return Config.FAST_TOWER_DAMAGE_SOLDIER;
        } else if (enemy instanceof Tank) {
            return Config.FAST_TOWER_DAMAGE_TANK;
        } else if (enemy instanceof Plane) {
            return Config.FAST_TOWER_DAMAGE_PLANE; // Should be 0
        }
        return 0;
    }
    
    @Override
    protected void onDeath() {
        // Fast tower destroyed
    }
}