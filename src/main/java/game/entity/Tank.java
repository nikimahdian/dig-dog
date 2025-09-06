package game.entity;

import game.map.Route;
import game.Config;
import javafx.geometry.Point2D;
import java.util.List;

public class Tank extends Enemy {
    
    private double fireTimer;
    private Tower targetTower;
    
    public Tank(Route route) {
        super(route, Config.TANK_HP, Config.TANK_SPEED, Config.TANK_POWER);
        // Tanks are larger to represent their 2-soldier spacing requirement
        this.width = Config.TILE_SIZE * 1.0;
        this.height = Config.TILE_SIZE * 1.0;
        this.fireTimer = 0;
        this.targetTower = null;
    }
    
    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        
        if (!alive) return;
        
        updateCombat(deltaTime);
    }
    
    private void updateCombat(double deltaTime) {
        fireTimer -= deltaTime;
        
        if (fireTimer <= 0) {
            // Try to find and shoot at towers
            findAndShootTower();
            fireTimer = 1.0 / Config.TANK_FIRE_RATE;
        }
    }
    
    private void findAndShootTower() {
        // This method would need access to the game's tower list
        // For now, we'll leave it as a stub that can be called by the game loop
    }
    
    public void setTowersInRange(List<Tower> towers) {
        targetTower = null;
        double range = Config.TANK_RANGE * Config.TILE_SIZE;
        
        // Find closest tower in range
        double closestDistance = Double.MAX_VALUE;
        for (Tower tower : towers) {
            if (!tower.isAlive()) continue;
            
            double distance = getDistanceTo(tower);
            if (distance <= range && distance < closestDistance) {
                closestDistance = distance;
                targetTower = tower;
            }
        }
        
        // Shoot at target if found
        if (targetTower != null) {
            shootAt(targetTower);
        }
    }
    
    private void shootAt(Tower target) {
        // Deal damage to the tower
        target.takeDamage(Config.TANK_DAMAGE);
    }
    
    @Override
    protected void onDeath() {
        // Tanks don't have special death behavior beyond their power value
    }
    
    @Override
    protected void onLeak() {
        // Tanks leak their power value to the castle
    }
    
    public Tower getTargetTower() {
        return targetTower;
    }
    
    public boolean isInCombat() {
        return targetTower != null && targetTower.isAlive();
    }
}