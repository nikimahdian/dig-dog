package game.entity;

import java.util.List;

@FunctionalInterface
public interface TargetingStrategy {
    
    /**
     * Select the best target from a list of enemies within range.
     * 
     * @param enemies List of enemies to consider
     * @param tower The tower doing the targeting
     * @return The best enemy to target, or null if no suitable target
     */
    Enemy selectTarget(List<Enemy> enemies, Tower tower);
    
    // Predefined targeting strategies
    TargetingStrategy CLOSEST_TO_EXIT = (enemies, tower) -> {
        Enemy bestTarget = null;
        double bestScore = Double.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive() || enemy.hasLeaked()) continue;
            if (!tower.canTarget(enemy)) continue;
            
            // Primary: closest to exit (smallest distance to end)
            // Secondary: lowest HP as tiebreaker
            double score = enemy.getDistanceToEnd() + (enemy.getCurrentHp() / 10000.0);
            
            if (score < bestScore) {
                bestScore = score;
                bestTarget = enemy;
            }
        }
        
        return bestTarget;
    };
    
    TargetingStrategy LOWEST_HP = (enemies, tower) -> {
        Enemy bestTarget = null;
        double lowestHp = Double.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive() || enemy.hasLeaked()) continue;
            if (!tower.canTarget(enemy)) continue;
            
            if (enemy.getCurrentHp() < lowestHp) {
                lowestHp = enemy.getCurrentHp();
                bestTarget = enemy;
            }
        }
        
        return bestTarget;
    };
    
    TargetingStrategy CLOSEST = (enemies, tower) -> {
        Enemy bestTarget = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive() || enemy.hasLeaked()) continue;
            if (!tower.canTarget(enemy)) continue;
            
            double distance = tower.getDistanceTo(enemy);
            if (distance < closestDistance) {
                closestDistance = distance;
                bestTarget = enemy;
            }
        }
        
        return bestTarget;
    };
}