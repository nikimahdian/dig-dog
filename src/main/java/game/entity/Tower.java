package game.entity;

import javafx.geometry.Point2D;
import game.Config;
import java.util.List;

public abstract class Tower extends Entity {
    
    protected double range;
    protected double fireRate;
    protected double fireTimer;
    protected Enemy target;
    protected final int cost;
    
    public Tower(double x, double y, double hp, double range, double fireRate, int cost) {
        super(x, y, hp);
        this.range = range * Config.TILE_SIZE; // Convert tile range to pixel range
        this.fireRate = fireRate;
        this.fireTimer = 0;
        this.target = null;
        this.cost = cost;
        this.width = Config.TILE_SIZE * 0.9;
        this.height = Config.TILE_SIZE * 0.9;
    }
    
    @Override
    public void update(double deltaTime) {
        if (!alive) return;
        
        fireTimer -= deltaTime;
        
        // Update targeting and combat
        updateCombat(deltaTime);
    }
    
    private void updateCombat(double deltaTime) {
        // Check if current target is still valid
        if (target != null && (!target.isAlive() || !isInRange(target, range) || target.hasLeaked())) {
            target = null;
        }
        
        // Fire at target if timer is ready
        if (fireTimer <= 0 && target != null) {
            fireAtTarget(target);
            fireTimer = 1.0 / fireRate;
        }
    }
    
    public void setEnemiesInRange(List<Enemy> enemies) {
        if (target != null && target.isAlive() && isInRange(target, range) && !target.hasLeaked()) {
            return; // Keep current target if still valid
        }
        
        target = findBestTarget(enemies);
    }
    
    protected TargetingStrategy targetingStrategy = TargetingStrategy.CLOSEST_TO_EXIT;
    protected FireBehavior fireBehavior = FireBehavior.INSTANT_HIT;
    
    protected Enemy findBestTarget(List<Enemy> enemies) {
        // Filter enemies to only those in range and can be damaged
        List<Enemy> validTargets = enemies.stream()
            .filter(enemy -> enemy.isAlive() && !enemy.hasLeaked())
            .filter(enemy -> isInRange(enemy, range))
            .filter(enemy -> getDamageAgainst(enemy) > 0)
            .toList();
        
        return targetingStrategy.selectTarget(validTargets, this);
    }
    
    private ProjectileManager projectileManager;
    
    public void setProjectileManager(ProjectileManager projectileManager) {
        this.projectileManager = projectileManager;
    }
    
    protected void fireAtTarget(Enemy target) {
        double damage = getDamageAgainst(target);
        if (damage > 0) {
            // Create projectile with Timeline tweening
            createProjectile(target, damage);
        }
    }
    
    protected abstract double getDamageAgainst(Enemy enemy);
    
    protected void createProjectile(Enemy target, double damage) {
        if (projectileManager != null) {
            // Create projectile from tower center to target center
            Point2D start = getCenter();
            Point2D targetPos = target.getCenter();
            double projectileSpeed = 300.0; // pixels per second
            
            Projectile projectile = new Projectile(start, targetPos, target, projectileSpeed, damage);
            projectileManager.addProjectile(projectile);
        }
    }
    
    // Interface for managing projectiles
    public interface ProjectileManager {
        void addProjectile(Projectile projectile);
    }
    
    public double getRange() {
        return range / Config.TILE_SIZE; // Return range in tiles
    }
    
    public double getFireRate() {
        return fireRate;
    }
    
    public Enemy getTarget() {
        return target;
    }
    
    public int getCost() {
        return cost;
    }
    
    public boolean canTarget(Enemy enemy) {
        return isInRange(enemy, range) && getDamageAgainst(enemy) > 0;
    }
}