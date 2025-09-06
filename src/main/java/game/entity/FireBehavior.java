package game.entity;

@FunctionalInterface
public interface FireBehavior {
    
    /**
     * Execute the firing behavior for a tower.
     * 
     * @param tower The tower that is firing
     * @param target The target enemy
     * @param deltaTime Time since last update
     * @return true if a shot was fired, false otherwise
     */
    boolean fire(Tower tower, Enemy target, double deltaTime);
    
    // Standard firing behavior with projectiles
    FireBehavior PROJECTILE_FIRE = (tower, target, deltaTime) -> {
        if (target == null || !target.isAlive() || target.hasLeaked()) {
            return false;
        }
        
        // Create projectile for visual effect
        tower.createProjectile(target);
        return true;
    };
    
    // Instant hit behavior (no projectile travel time)
    FireBehavior INSTANT_HIT = (tower, target, deltaTime) -> {
        if (target == null || !target.isAlive() || target.hasLeaked()) {
            return false;
        }
        
        double damage = tower.getDamageAgainst(target);
        if (damage > 0) {
            target.takeDamage(damage);
            return true;
        }
        return false;
    };
    
    // Anti-air behavior with hit chance
    FireBehavior AA_CHANCE_BASED = (tower, target, deltaTime) -> {
        if (target == null || !target.isAlive()) {
            return false;
        }
        
        if (tower instanceof AntiAir aa) {
            // Roll for hit based on AA hit chance
            if (Math.random() < aa.getHitChance()) {
                target.kill(); // AA kills planes instantly on hit
                return true;
            }
        }
        return false;
    };
}