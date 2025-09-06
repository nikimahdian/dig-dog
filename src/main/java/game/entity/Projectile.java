package game.entity;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class Projectile extends Entity {
    
    private final Point2D startPosition;
    private final Point2D targetPosition;
    private final double damage;
    private final Enemy targetEnemy;
    private final Timeline travelTimeline;
    private boolean hasHit;
    
    // JavaFX properties for smooth animation
    private final DoubleProperty animationProgress;
    
    public Projectile(Point2D start, Point2D target, Enemy targetEnemy, double speed, double damage) {
        super(start.getX(), start.getY(), 1);
        this.startPosition = start;
        this.targetPosition = target;
        this.targetEnemy = targetEnemy;
        this.damage = damage;
        this.hasHit = false;
        this.width = 4;
        this.height = 4;
        
        this.animationProgress = new SimpleDoubleProperty(0.0);
        
        // Calculate travel time based on distance and speed
        double totalDistance = start.distance(target);
        double travelTime = totalDistance / speed; // speed is in pixels per second
        
        // Create Timeline for smooth projectile movement
        this.travelTimeline = new Timeline();
        KeyValue progressKeyValue = new KeyValue(animationProgress, 1.0);
        KeyFrame endFrame = new KeyFrame(Duration.seconds(travelTime), progressKeyValue);
        travelTimeline.getKeyFrames().add(endFrame);
        
        // Set up completion handler
        travelTimeline.setOnFinished(e -> onReachTarget());
        
        // Bind position to animation progress
        animationProgress.addListener((obs, oldVal, newVal) -> updatePosition(newVal.doubleValue()));
        
        // Start the animation
        travelTimeline.play();
    }
    
    @Override
    public void update(double deltaTime) {
        // Position updates are handled by JavaFX Timeline animation
        // No manual movement needed here
        if (!alive || hasHit) {
            return;
        }
    }
    
    private void updatePosition(double progress) {
        if (!alive || hasHit) return;
        
        // Interpolate position based on animation progress
        double newX = startPosition.getX() + (targetPosition.getX() - startPosition.getX()) * progress;
        double newY = startPosition.getY() + (targetPosition.getY() - startPosition.getY()) * progress;
        setPosition(newX, newY);
    }
    
    private void onReachTarget() {
        hasHit = true;
        alive = false;
        
        // Deal damage to target if it's still alive and in approximately the same position
        if (targetEnemy != null && targetEnemy.isAlive()) {
            double distanceToTarget = getCenter().distance(targetEnemy.getCenter());
            if (distanceToTarget <= 32) { // Within one tile of expected position
                targetEnemy.takeDamage(damage);
            }
        }
    }
    
    public boolean hasHit() {
        return hasHit;
    }
    
    public Enemy getTargetEnemy() {
        return targetEnemy;
    }
    
    public double getDamage() {
        return damage;
    }
    
    public Point2D getStartPosition() {
        return startPosition;
    }
    
    public Point2D getTargetPosition() {
        return targetPosition;
    }
    
    public double getProgress() {
        return animationProgress.get();
    }
    
    /**
     * Stop the projectile animation and clean up resources.
     * Should be called when the projectile is removed from the game.
     */
    public void cleanup() {
        if (travelTimeline != null) {
            travelTimeline.stop();
        }
        hasHit = true;
        alive = false;
    }
    
    @Override
    public void kill() {
        cleanup();
        super.kill();
    }
}