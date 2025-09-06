package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import com.tdgame.core.EventBus;
import com.tdgame.util.Math2D;

import java.util.List;
import java.util.ArrayList;

/**
 * Heavy armored enemy tank.
 * Can shoot at nearby enemies while moving with rotation animation.
 */
public class Tank extends Enemy {
    
    private final double dpsVsEnemies;
    private double shootCooldown = 0.0;
    private final double shootInterval = 1.0; // Shoot every second
    private final double attackRange = 5.0; // Range to attack enemies
    
    // Rotation and animation
    private double currentRotation = 0.0; // Current rotation angle in radians
    private double targetRotation = 0.0; // Target rotation angle
    private final double rotationSpeed = 3.0; // Rotation speed in radians/second
    private double turretRotation = 0.0; // Separate turret rotation
    
    // Attack animation
    private double attackFlashDuration = 0.0;
    private final double ATTACK_FLASH_TIME = 0.2;
    
    // Projectile system
    private List<Projectile> firedProjectiles = new ArrayList<>();
    
    private List<Enemy> nearbyEnemies = new ArrayList<>();
    private List<Tower> nearbyTowers = new ArrayList<>();
    private List<AADefense> nearbyAA = new ArrayList<>();
    
    private Enemy currentEnemyTarget = null; // Current enemy being targeted
    private Tower currentTowerTarget = null; // Current tower being targeted
    private AADefense currentAATarget = null; // Current AA being targeted
    
    public Tank(Balance.EnemyStats stats) {
        super(stats.hp, stats.speed, stats.power, stats.spriteIndex);
        this.dpsVsEnemies = stats.dpsVsDefenses != null ? stats.dpsVsDefenses : 25.0; // Damage to other enemies
        
        // Tanks are larger and occupy more space
        this.laneOffset = 0; // Tanks always use center path, no lane offset
    }
    
    @Override
    protected void updateSpecific(double deltaTime) {
        // Update rotation animation
        updateRotation(deltaTime);
        
        // Update turret rotation towards target
        updateTurretRotation(deltaTime);
        
        // Update projectiles
        updateProjectiles(deltaTime);
        
        // Update attack flash animation
        if (attackFlashDuration > 0) {
            attackFlashDuration -= deltaTime;
        }
        
        // Handle shooting
        shootCooldown -= deltaTime;
        if (shootCooldown <= 0) {
            // Priority: Towers > AA Defenses (no self-attack on enemies)
            if (!attackNearbyTowers() && !attackNearbyAA()) {
                // Don't attack other enemies - tanks only attack player structures
            }
            shootCooldown = shootInterval;
        }
    }
    
    /**
     * Update tank body rotation based on movement direction
     */
    private void updateRotation(double deltaTime) {
        if (path != null && pathProgress < 1.0) {
            // Calculate movement direction
            double futureProgress = Math.min(1.0, pathProgress + 0.01);
            Math2D.Point currentPos = path.getPositionAt(pathProgress);
            Math2D.Point futurePos = path.getPositionAt(futureProgress);
            
            if (Math2D.distance(currentPos.x, currentPos.y, futurePos.x, futurePos.y) > 0.1) {
                targetRotation = Math2D.angle(currentPos.x, currentPos.y, futurePos.x, futurePos.y);
                
                // Smooth rotation animation
                double rotationDiff = targetRotation - currentRotation;
                
                // Normalize angle difference to [-π, π]
                while (rotationDiff > Math.PI) rotationDiff -= 2 * Math.PI;
                while (rotationDiff < -Math.PI) rotationDiff += 2 * Math.PI;
                
                if (Math.abs(rotationDiff) > 0.1) {
                    double rotationStep = rotationSpeed * deltaTime;
                    if (Math.abs(rotationDiff) < rotationStep) {
                        currentRotation = targetRotation;
                    } else {
                        currentRotation += Math.signum(rotationDiff) * rotationStep;
                    }
                }
            }
        }
    }
    
    /**
     * Update turret rotation towards current target (tower, AA, or enemy)
     */
    private void updateTurretRotation(double deltaTime) {
        double targetX = 0, targetY = 0;
        boolean hasTarget = false;
        
        // Priority: Tower > AA > Enemy
        if (currentTowerTarget != null && currentTowerTarget.isAlive()) {
            targetX = currentTowerTarget.getX();
            targetY = currentTowerTarget.getY();
            hasTarget = true;
        } else if (currentAATarget != null && currentAATarget.isAlive()) {
            targetX = currentAATarget.getX();
            targetY = currentAATarget.getY();
            hasTarget = true;
        } else if (currentEnemyTarget != null && currentEnemyTarget.isAlive()) {
            targetX = currentEnemyTarget.getX();
            targetY = currentEnemyTarget.getY();
            hasTarget = true;
        }
        
        if (hasTarget) {
            
            double targetTurretRotation = Math2D.angle(x, y, targetX, targetY);
            
            // Smooth turret rotation
            double turretDiff = targetTurretRotation - turretRotation;
            while (turretDiff > Math.PI) turretDiff -= 2 * Math.PI;
            while (turretDiff < -Math.PI) turretDiff += 2 * Math.PI;
            
            if (Math.abs(turretDiff) > 0.05) {
                double turretStep = rotationSpeed * 1.5 * deltaTime; // Turret rotates faster
                if (Math.abs(turretDiff) < turretStep) {
                    turretRotation = targetTurretRotation;
                } else {
                    turretRotation += Math.signum(turretDiff) * turretStep;
                }
            }
        }
    }
    
    /**
     * Update fired projectiles
     */
    private void updateProjectiles(double deltaTime) {
        for (int i = firedProjectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = firedProjectiles.get(i);
            projectile.update(deltaTime);
            
            if (!projectile.isActive() || projectile.hasHitTarget()) {
                firedProjectiles.remove(i);
            }
        }
    }
    
    /**
     * Attack nearby towers (highest priority)
     */
    private boolean attackNearbyTowers() {
        Tower target = findBestTowerTarget();
        
        if (target != null) {
            currentTowerTarget = target;
            fireProjectileAtTower(target);
            return true;
        }
        return false;
    }
    
    /**
     * Attack nearby AA defenses (medium priority)
     */
    private boolean attackNearbyAA() {
        AADefense target = findBestAATarget();
        
        if (target != null) {
            currentAATarget = target;
            fireProjectileAtAA(target);
            return true;
        }
        return false;
    }
    
    /**
     * Attack nearby enemies within range by firing projectiles (lowest priority)
     */
    private boolean attackNearbyEnemies() {
        Enemy target = findBestEnemyTarget();
        
        if (target != null) {
            currentEnemyTarget = target;
            fireProjectileAtEnemy(target);
            return true;
        }
        return false;
    }
    
    /**
     * Find the best enemy target within range (excluding self)
     */
    private Enemy findBestEnemyTarget() {
        Enemy bestTarget = null;
        double closestDistance = Double.MAX_VALUE;
        
        // Check nearby enemies (excluding self)
        for (Enemy enemy : nearbyEnemies) {
            if (!enemy.isAlive() || enemy == this) continue; // Don't target self
            
            double distance = Math2D.distance(x, y, enemy.getX(), enemy.getY());
            double maxRange = attackRange * 64; // Convert tile range to pixels
            
            if (distance <= maxRange && distance < closestDistance) {
                closestDistance = distance;
                bestTarget = enemy;
            }
        }
        
        return bestTarget;
    }
    
    /**
     * Find the best tower target within range
     */
    private Tower findBestTowerTarget() {
        Tower bestTarget = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Tower tower : nearbyTowers) {
            if (!tower.isAlive()) continue;
            
            double distance = Math2D.distance(x, y, tower.getX(), tower.getY());
            double maxRange = attackRange * 64; // Convert tile range to pixels
            
            if (distance <= maxRange && distance < closestDistance) {
                closestDistance = distance;
                bestTarget = tower;
            }
        }
        
        return bestTarget;
    }
    
    /**
     * Find the best AA target within range
     */
    private AADefense findBestAATarget() {
        AADefense bestTarget = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (AADefense aa : nearbyAA) {
            if (!aa.isAlive()) continue;
            
            double distance = Math2D.distance(x, y, aa.getX(), aa.getY());
            double maxRange = attackRange * 64; // Convert tile range to pixels
            
            if (distance <= maxRange && distance < closestDistance) {
                closestDistance = distance;
                bestTarget = aa;
            }
        }
        
        return bestTarget;
    }
    
    /**
     * Fire a projectile at a tower target
     */
    private void fireProjectileAtTower(Tower target) {
        if (target == null || !target.isAlive()) return;
        
        // Create projectile targeting the tower
        int damage = (int)(dpsVsEnemies * shootInterval);
        double projectileSpeed = 250.0; // pixels per second
        int projectileSpriteIndex = 274; // Tank shell sprite
        
        // Start attack animation
        attackFlashDuration = ATTACK_FLASH_TIME;
        
        // Direct damage to tower
        target.takeDamage(damage);
        
        System.out.println("Tank attacking tower! Damage: " + damage);
    }
    
    /**
     * Fire a projectile at an AA defense target  
     */
    private void fireProjectileAtAA(AADefense target) {
        if (target == null || !target.isAlive()) return;
        
        // Create projectile targeting the AA
        int damage = (int)(dpsVsEnemies * shootInterval);
        double projectileSpeed = 250.0; // pixels per second
        int projectileSpriteIndex = 274; // Tank shell sprite
        
        // Start attack animation
        attackFlashDuration = ATTACK_FLASH_TIME;
        
        // Direct damage to AA defense
        target.takeDamage(damage);
        
        System.out.println("Tank attacking AA defense! Damage: " + damage);
    }
    
    /**
     * Fire a projectile at an enemy target
     */
    private void fireProjectileAtEnemy(Enemy target) {
        if (target == null || !target.isAlive()) return;
        
        // Create projectile directly targeting the enemy
        int damage = (int)(dpsVsEnemies * shootInterval);
        double projectileSpeed = 250.0; // pixels per second
        int projectileSpriteIndex = 274; // Tank shell sprite
        
        Projectile projectile = new Projectile(x, y, target, projectileSpeed, damage, projectileSpriteIndex);
        firedProjectiles.add(projectile);
    }
    
    /**
     * Update list of nearby enemies (called by combat system)
     */
    public void updateNearbyEnemies(List<Enemy> enemies) {
        nearbyEnemies.clear();
        
        for (Enemy enemy : enemies) {
            if (enemy != this && enemy.isAlive() && 
                Math2D.distance(x, y, enemy.getX(), enemy.getY()) <= attackRange * 64 * 1.5) { // Detection range slightly larger
                nearbyEnemies.add(enemy);
            }
        }
    }
    
    /**
     * Update list of nearby towers (called by combat system)
     */
    public void updateNearbyTowers(List<Tower> towers) {
        nearbyTowers.clear();
        
        for (Tower tower : towers) {
            if (tower.isAlive() && 
                Math2D.distance(x, y, tower.getX(), tower.getY()) <= attackRange * 64 * 1.5) {
                nearbyTowers.add(tower);
            }
        }
    }
    
    /**
     * Update list of nearby AA defenses (called by combat system)
     */
    public void updateNearbyAA(List<AADefense> aaDefenses) {
        nearbyAA.clear();
        
        for (AADefense aa : aaDefenses) {
            if (aa.isAlive() && 
                Math2D.distance(x, y, aa.getX(), aa.getY()) <= attackRange * 64 * 1.5) {
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
        // Clear current target when tank dies
        currentEnemyTarget = null;
        firedProjectiles.clear();
    }
    
    @Override
    protected void onReachedEnd() {
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(power));
    }
    
    // Getters for animation and rendering
    public double getCurrentRotation() { return currentRotation; }
    public double getTurretRotation() { return turretRotation; }
    public List<Projectile> getFiredProjectiles() { return new ArrayList<>(firedProjectiles); }
    public Enemy getCurrentEnemyTarget() { return currentEnemyTarget; }
    public Tower getCurrentTowerTarget() { return currentTowerTarget; }
    public AADefense getCurrentAATarget() { return currentAATarget; }
    public boolean isAttacking() { return attackFlashDuration > 0; }
    public double getAttackFlashIntensity() { 
        return attackFlashDuration > 0 ? attackFlashDuration / ATTACK_FLASH_TIME : 0.0; 
    }
    
    public double getAttackRange() { return attackRange; }
    public double getDpsVsEnemies() { return dpsVsEnemies; }
}