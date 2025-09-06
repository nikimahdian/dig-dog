package game.entity;

import game.Config;

public class HeavyTower extends Tower {
    
    public HeavyTower(double x, double y) {
        super(x, y, Config.HEAVY_TOWER_HP, Config.TOWER_RANGE_HEAVY, 
              Config.HEAVY_TOWER_FIRE_RATE, Config.HEAVY_TOWER_COST);
    }
    
    @Override
    protected double getDamageAgainst(Enemy enemy) {
        if (enemy instanceof Soldier) {
            return Config.HEAVY_TOWER_DAMAGE_SOLDIER;
        } else if (enemy instanceof Tank) {
            return Config.HEAVY_TOWER_DAMAGE_TANK;
        } else if (enemy instanceof Plane) {
            return Config.HEAVY_TOWER_DAMAGE_PLANE; // Should be 0
        }
        return 0;
    }
    
    @Override
    protected void onDeath() {
        // Heavy tower destroyed
    }
}