package com.tdgame.model.systems;

import com.tdgame.config.GameConfig;
import com.tdgame.model.actors.*;
import com.tdgame.model.placeables.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages all combat interactions between towers, enemies, and projectiles.
 * Coordinates targeting, damage dealing, and special effects.
 */
public class CombatSystem {
    
    private final GameConfig config;
    private final List<Tower> towers;
    private final List<AADefense> aaDefenses;
    private final List<Enemy> enemies;
    private final List<Projectile> projectiles;
    private final List<SpeedBump> speedBumps;
    private final List<Bomb> bombs;
    
    public CombatSystem(GameConfig config) {
        this.config = config;
        this.towers = new ArrayList<>();
        this.aaDefenses = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.speedBumps = new ArrayList<>();
        this.bombs = new ArrayList<>();
    }
    
    public void update(double deltaTime) {
        // Update all enemies
        updateEnemies(deltaTime);
        
        // Update tower combat
        updateTowers(deltaTime);
        
        // Update AA defenses
        updateAADefenses(deltaTime);
        
        // Update projectiles
        updateProjectiles(deltaTime);
        
        // Update placeables
        updatePlaceables(deltaTime);
        
        // Update tank attacks on defenses
        updateTankAttacks(deltaTime);
        
        // Clean up dead entities
        cleanupDeadEntities();
    }
    
    /**
     * Update all enemies
     */
    private void updateEnemies(double deltaTime) {
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);
        }
    }
    
    /**
     * Update tower targeting and firing
     */
    private void updateTowers(double deltaTime) {
        for (Tower tower : towers) {
            if (!tower.isAlive()) continue;
            
            tower.update(deltaTime, enemies);
            
            // Update tower-specific projectiles
            if (tower instanceof FastTower fastTower) {
                // Don't clear - let tower manage its own projectiles
                List<Projectile> towerProjectiles = fastTower.getProjectiles();
                for (Projectile projectile : towerProjectiles) {
                    if (!projectiles.contains(projectile)) {
                        projectiles.add(projectile);
                    }
                }
            } else if (tower instanceof PowerTower powerTower) {
                // Don't clear - let tower manage its own projectiles  
                List<Projectile> towerProjectiles = powerTower.getProjectiles();
                for (Projectile projectile : towerProjectiles) {
                    if (!projectiles.contains(projectile)) {
                        projectiles.add(projectile);
                    }
                }
            } else if (tower instanceof TankTower tankTower) {
                // Update and collect tank tower projectiles like other towers
                List<Projectile> tankProjectiles = tankTower.getFiredProjectiles();
                for (Projectile projectile : tankProjectiles) {
                    if (!projectiles.contains(projectile)) {
                        projectiles.add(projectile);
                    }
                }
            }
        }
    }
    
    /**
     * Update AA defense targeting
     */
    private void updateAADefenses(double deltaTime) {
        for (AADefense aa : aaDefenses) {
            if (!aa.isAlive()) continue;
            aa.update(deltaTime, enemies);
        }
    }
    
    /**
     * Update all projectiles
     */
    private void updateProjectiles(double deltaTime) {
        for (Projectile projectile : projectiles) {
            projectile.update(deltaTime);
        }
    }
    
    /**
     * Update speed bumps and bombs
     */
    private void updatePlaceables(double deltaTime) {
        for (SpeedBump speedBump : speedBumps) {
            speedBump.update(deltaTime, enemies);
        }
        
        for (Bomb bomb : bombs) {
            bomb.update(deltaTime, enemies);
        }
    }
    
    /**
     * Update tank attacks on towers, AA defenses, and other enemies
     */
    private void updateTankAttacks(double deltaTime) {
        for (Enemy enemy : enemies) {
            if (enemy instanceof Tank tank && enemy.isAlive()) {
                // Update nearby targets for tanks
                tank.updateNearbyEnemies(enemies);
                tank.updateNearbyTowers(towers);
                tank.updateNearbyAA(aaDefenses);
                
                // Add tank projectiles to main projectile list for rendering
                for (Projectile tankProjectile : tank.getFiredProjectiles()) {
                    if (!projectiles.contains(tankProjectile)) {
                        projectiles.add(tankProjectile);
                    }
                }
            }
        }
    }
    
    /**
     * Clean up dead entities
     */
    private void cleanupDeadEntities() {
        // Remove dead enemies
        enemies.removeIf(enemy -> !enemy.isAlive());
        
        // Remove dead towers
        towers.removeIf(tower -> !tower.isAlive());
        
        // Remove dead AA defenses
        aaDefenses.removeIf(aa -> !aa.isAlive());
        
        // Remove inactive projectiles
        projectiles.removeIf(projectile -> !projectile.isActive());
        
        // Remove expired speed bumps
        speedBumps.removeIf(speedBump -> !speedBump.isActive());
        
        // Remove exploded bombs
        bombs.removeIf(bomb -> bomb.isExplosionComplete());
    }
    
    /**
     * Add a tower to the combat system
     */
    public void addTower(Tower tower) {
        towers.add(tower);
    }
    
    /**
     * Add an AA defense to the combat system
     */
    public void addAADefense(AADefense aaDefense) {
        aaDefenses.add(aaDefense);
    }
    
    /**
     * Add an enemy to the combat system
     */
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }
    
    /**
     * Add a speed bump to the combat system
     */
    public void addSpeedBump(SpeedBump speedBump) {
        speedBumps.add(speedBump);
    }
    
    /**
     * Add a bomb to the combat system
     */
    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
    }
    
    // Getters for systems that need access
    public List<Tower> getTowers() { return towers; }
    public List<AADefense> getAADefenses() { return aaDefenses; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<SpeedBump> getSpeedBumps() { return speedBumps; }
    public List<Bomb> getBombs() { return bombs; }
}