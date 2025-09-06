package game.entity;

import javafx.geometry.Point2D;

@FunctionalInterface
public interface MoveBehavior {
    
    /**
     * Update the position of an entity based on its movement behavior.
     * 
     * @param entity The entity to move
     * @param deltaTime Time since last update in seconds
     */
    void move(Entity entity, double deltaTime);
    
    // Route-following movement for ground enemies
    MoveBehavior ROUTE_FOLLOWING = (entity, deltaTime) -> {
        if (entity instanceof Enemy enemy && !enemy.hasLeaked() && enemy.isAlive()) {
            // This is handled in the Enemy.update() method
            // Movement along route waypoints with smooth interpolation
        }
    };
    
    // Straight-line movement for planes
    MoveBehavior STRAIGHT_LINE = (entity, deltaTime) -> {
        if (entity instanceof Plane plane && plane.isAlive()) {
            // This is handled in the Plane.update() method  
            // Movement from start to end position in straight line
        }
    };
    
    // Stationary behavior for towers
    MoveBehavior STATIONARY = (entity, deltaTime) -> {
        // Towers don't move
    };
    
    // Projectile movement towards target
    MoveBehavior PROJECTILE_TRAVEL = (entity, deltaTime) -> {
        if (entity instanceof Projectile projectile && projectile.isAlive()) {
            // This is handled in the Projectile.update() method
            // Movement from start position to target position
        }
    };
    
    // Custom movement with velocity
    static MoveBehavior createVelocityBased(Point2D velocity) {
        return (entity, deltaTime) -> {
            if (entity.isAlive()) {
                double newX = entity.getX() + velocity.getX() * deltaTime;
                double newY = entity.getY() + velocity.getY() * deltaTime;
                entity.setPosition(newX, newY);
            }
        };
    }
    
    // Circular movement around a center point
    static MoveBehavior createCircularMovement(Point2D center, double radius, double angularSpeed) {
        return (entity, deltaTime) -> {
            if (entity.isAlive()) {
                // This would require storing current angle in entity state
                // For now, this is a placeholder for potential circular movement patterns
            }
        };
    }
}