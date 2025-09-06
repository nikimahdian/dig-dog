package game.entity;

import game.map.Route;
import game.Config;

public class Soldier extends Enemy {
    
    public Soldier(Route route) {
        super(route, Config.SOLDIER_HP, Config.SOLDIER_SPEED, Config.SOLDIER_POWER);
        this.width = Config.TILE_SIZE * 0.6;
        this.height = Config.TILE_SIZE * 0.6;
    }
    
    @Override
    protected void onDeath() {
        // Soldiers don't have special death behavior
    }
    
    @Override
    protected void onLeak() {
        // Soldiers leak their power value to the castle
    }
}