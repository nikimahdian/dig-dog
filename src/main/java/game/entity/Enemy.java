package game.entity;

import javafx.geometry.Point2D;
import game.map.Route;
import game.Config;

public abstract class Enemy extends Entity {
    
    protected final Route route;
    protected int currentWaypointIndex;
    protected double progressToNextWaypoint;
    protected double speed;
    protected int powerValue;
    protected boolean leaked;
    protected double slowFactor;
    protected double slowEndTime;
    
    public Enemy(Route route, double hp, double speed, int powerValue) {
        super(0, 0, hp);  // Position will be set based on route
        this.route = route;
        this.currentWaypointIndex = 0;
        this.progressToNextWaypoint = 0;
        this.speed = speed;
        this.powerValue = powerValue;
        this.leaked = false;
        this.slowFactor = 1.0;
        this.slowEndTime = 0;
        
        // Set initial position at route start
        if (route.getWaypointCount() > 0) {
            Point2D startPixel = route.getPixelPosition(0, 0);
            if (startPixel != null) {
                setPosition(startPixel.getX() - width / 2, startPixel.getY() - height / 2);
            }
        }
    }
    
    @Override
    public void update(double deltaTime) {
        if (!alive || leaked) return;
        
        updateMovement(deltaTime);
        updateSlowEffect(deltaTime);
    }
    
    private void updateMovement(double deltaTime) {
        if (currentWaypointIndex >= route.getWaypointCount() - 1) {
            // Reached the end - leak to castle
            leaked = true;
            onLeak();
            return;
        }
        
        double effectiveSpeed = speed * slowFactor;
        double distanceToMove = effectiveSpeed * Config.TILE_SIZE * deltaTime;
        
        // Calculate distance between current and next waypoint
        Point2D currentWaypoint = route.getWaypoint(currentWaypointIndex);
        Point2D nextWaypoint = route.getWaypoint(currentWaypointIndex + 1);
        
        if (currentWaypoint != null && nextWaypoint != null) {
            double segmentLength = currentWaypoint.distance(nextWaypoint) * Config.TILE_SIZE;
            double progressIncrement = distanceToMove / segmentLength;
            
            progressToNextWaypoint += progressIncrement;
            
            if (progressToNextWaypoint >= 1.0) {
                // Move to next waypoint
                currentWaypointIndex++;
                progressToNextWaypoint = 0.0;
                
                if (currentWaypointIndex >= route.getWaypointCount() - 1) {
                    // Reached the end
                    leaked = true;
                    onLeak();
                    return;
                }
            }
            
            // Update position based on current progress
            Point2D pixelPos = route.getPixelPosition(currentWaypointIndex, progressToNextWaypoint);
            if (pixelPos != null) {
                setPosition(pixelPos.getX() - width / 2, pixelPos.getY() - height / 2);
            }
        }
    }
    
    private void updateSlowEffect(double deltaTime) {
        if (slowEndTime > 0) {
            slowEndTime -= deltaTime;
            if (slowEndTime <= 0) {
                slowFactor = 1.0; // Remove slow effect
            }
        }
    }
    
    public void applySlow(double factor, double duration) {
        this.slowFactor = factor;
        this.slowEndTime = duration;
    }
    
    protected void onLeak() {
        // Override in subclasses for leak behavior
    }
    
    public Route getRoute() {
        return route;
    }
    
    public int getCurrentWaypointIndex() {
        return currentWaypointIndex;
    }
    
    public double getProgressToNextWaypoint() {
        return progressToNextWaypoint;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public int getPowerValue() {
        return powerValue;
    }
    
    public boolean hasLeaked() {
        return leaked;
    }
    
    public double getDistanceToEnd() {
        if (leaked || !alive) return 0;
        
        double distance = 0;
        
        // Distance to complete current segment
        if (currentWaypointIndex < route.getWaypointCount() - 1) {
            Point2D current = route.getWaypoint(currentWaypointIndex);
            Point2D next = route.getWaypoint(currentWaypointIndex + 1);
            if (current != null && next != null) {
                double segmentLength = current.distance(next);
                distance += segmentLength * (1.0 - progressToNextWaypoint);
            }
        }
        
        // Distance for remaining segments
        for (int i = currentWaypointIndex + 1; i < route.getWaypointCount() - 1; i++) {
            Point2D waypoint = route.getWaypoint(i);
            Point2D nextWaypoint = route.getWaypoint(i + 1);
            if (waypoint != null && nextWaypoint != null) {
                distance += waypoint.distance(nextWaypoint);
            }
        }
        
        return distance;
    }
    
    public Point2D getCurrentTilePosition() {
        return new Point2D(
            Math.floor((getX() + width / 2) / Config.TILE_SIZE),
            Math.floor((getY() + height / 2) / Config.TILE_SIZE)
        );
    }
}