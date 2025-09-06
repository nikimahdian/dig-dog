package com.tdgame.model.actors;

import com.tdgame.util.Math2D;
import com.tdgame.model.grid.GridMap;

/**
 * Base class for all enemy units.
 * Handles movement along paths, health, and power tracking.
 */
public abstract class Enemy {
    
    protected double x, y;
    protected int maxHp;
    protected int currentHp;
    protected double baseSpeed;
    protected double currentSpeed;
    protected int power;
    protected int spriteIndex;
    
    protected double pathProgress = 0.0; // 0.0 to 1.0 along path
    protected GridMap.Path path;
    protected boolean alive = true;
    protected boolean reachedEnd = false;
    
    // Lane system for dual-lane movement
    protected int lane = 0; // 0 = top lane, 1 = bottom lane  
    protected double laneOffset = 16; // Pixels offset from center path
    
    // Status effects
    protected double slowMultiplier = 1.0;
    protected double slowDuration = 0.0;
    
    public Enemy(int hp, double speed, int power, int spriteIndex) {
        this.maxHp = hp;
        this.currentHp = hp;
        this.baseSpeed = speed;
        this.currentSpeed = speed;
        this.power = power;
        this.spriteIndex = spriteIndex;
    }
    
    /**
     * Set the path this enemy will follow
     */
    public void setPath(GridMap.Path path) {
        this.path = path;
        if (path != null) {
            Math2D.Point startPos = path.getPositionAt(0.0);
            this.x = startPos.x;
            this.y = startPos.y;
            
            // Apply lane offset
            applyLaneOffset();
        }
    }
    
    /**
     * Set which lane this enemy uses (0=top, 1=bottom)
     */
    public void setLane(int lane) {
        this.lane = lane;
        if (path != null) {
            applyLaneOffset();
        }
    }
    
    /**
     * Apply lane offset to current position
     */
    private void applyLaneOffset() {
        if (path == null) return;
        
        // Calculate perpendicular offset based on lane
        double offsetDistance = (lane == 0) ? -laneOffset : laneOffset;
        
        // Get current position and next position to determine direction
        Math2D.Point currentPos = path.getPositionAt(pathProgress);
        Math2D.Point nextPos = path.getPositionAt(Math.min(1.0, pathProgress + 0.01));
        
        if (Math2D.distance(currentPos.x, currentPos.y, nextPos.x, nextPos.y) > 0.1) {
            // Calculate perpendicular direction
            double pathAngle = Math2D.angle(currentPos.x, currentPos.y, nextPos.x, nextPos.y);
            double perpAngle = pathAngle + Math.PI / 2; // 90 degrees perpendicular
            
            // Apply offset
            this.x = currentPos.x + Math.cos(perpAngle) * offsetDistance;
            this.y = currentPos.y + Math.sin(perpAngle) * offsetDistance;
        }
    }
    
    /**
     * Update enemy state
     */
    public void update(double deltaTime) {
        if (!alive || reachedEnd || path == null) return;
        
        // Update status effects
        updateStatusEffects(deltaTime);
        
        // Move along path
        moveAlongPath(deltaTime);
        
        // Check if reached end
        if (pathProgress >= 1.0) {
            reachedEnd = true;
            onReachedEnd();
        }
        
        // Custom update logic for subclasses
        updateSpecific(deltaTime);
    }
    
    /**
     * Move along the assigned path with lane offset
     */
    protected void moveAlongPath(double deltaTime) {
        if (path == null) return;
        
        // Apply slow effect to current speed
        currentSpeed = baseSpeed * slowMultiplier;
        
        // Smoother movement calculation
        double pixelsPerSecond = currentSpeed * 64; // Convert tiles/sec to pixels/sec
        double distance = pixelsPerSecond * deltaTime;
        double pathLength = path.getTotalLength();
        
        if (pathLength > 0) {
            double progressIncrement = distance / pathLength;
            pathProgress += progressIncrement;
            pathProgress = Math.min(1.0, Math.max(0.0, pathProgress));
            
            // Get center path position
            Math2D.Point centerPos = path.getPositionAt(pathProgress);
            
            // Apply lane offset to get actual position
            applyLaneOffsetToPosition(centerPos);
        }
    }
    
    /**
     * Apply lane offset to a specific position with smooth corner handling
     */
    private void applyLaneOffsetToPosition(Math2D.Point centerPos) {
        // Calculate perpendicular offset based on lane
        double offsetDistance = (lane == 0) ? -laneOffset : laneOffset;
        
        // Get path direction using a larger lookahead for stability
        Math2D.Point prevPos = path.getPositionAt(Math.max(0.0, pathProgress - 0.02));
        Math2D.Point nextPos = path.getPositionAt(Math.min(1.0, pathProgress + 0.02));
        
        double pathDistance = Math2D.distance(prevPos.x, prevPos.y, nextPos.x, nextPos.y);
        
        if (pathDistance > 1.0) {
            // Calculate smooth path direction
            double pathAngle = Math2D.angle(prevPos.x, prevPos.y, nextPos.x, nextPos.y);
            double perpAngle = pathAngle + Math.PI / 2; // 90 degrees perpendicular
            
            // Reduce offset on sharp turns for smoother movement
            double turnSharpness = calculateTurnSharpness();
            double adjustedOffset = offsetDistance * (1.0 - turnSharpness * 0.5);
            
            // Apply offset
            this.x = centerPos.x + Math.cos(perpAngle) * adjustedOffset;
            this.y = centerPos.y + Math.sin(perpAngle) * adjustedOffset;
        } else {
            // Very sharp turn or end of path, use center position
            this.x = centerPos.x;
            this.y = centerPos.y;
        }
    }
    
    /**
     * Calculate how sharp the current turn is (0.0 = straight, 1.0 = 90+ degrees)
     */
    private double calculateTurnSharpness() {
        if (pathProgress < 0.05 || pathProgress > 0.95) return 0.0;
        
        Math2D.Point prevPos = path.getPositionAt(pathProgress - 0.05);
        Math2D.Point currPos = path.getPositionAt(pathProgress);
        Math2D.Point nextPos = path.getPositionAt(pathProgress + 0.05);
        
        if (Math2D.distance(prevPos.x, prevPos.y, currPos.x, currPos.y) > 1.0 &&
            Math2D.distance(currPos.x, currPos.y, nextPos.x, nextPos.y) > 1.0) {
            
            double angle1 = Math2D.angle(prevPos.x, prevPos.y, currPos.x, currPos.y);
            double angle2 = Math2D.angle(currPos.x, currPos.y, nextPos.x, nextPos.y);
            
            double angleDiff = Math.abs(angle1 - angle2);
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            
            // Convert angle difference to sharpness (0-1)
            return Math.min(1.0, Math.abs(angleDiff) / (Math.PI / 2));
        }
        
        return 0.0;
    }
    
    /**
     * Update status effects like slow
     */
    protected void updateStatusEffects(double deltaTime) {
        if (slowDuration > 0) {
            slowDuration -= deltaTime;
            if (slowDuration <= 0) {
                slowMultiplier = 1.0;
            }
        }
        
        currentSpeed = baseSpeed * slowMultiplier;
    }
    
    /**
     * Take damage
     */
    public void takeDamage(int damage) {
        if (!alive) return;
        
        currentHp -= damage;
        onTakeDamage(damage);
        
        if (currentHp <= 0) {
            currentHp = 0;
            alive = false;
            onDeath();
        }
    }
    
    /**
     * Apply slow effect
     */
    public void applySlow(double multiplier, double duration) {
        this.slowMultiplier = Math.min(this.slowMultiplier, multiplier);
        this.slowDuration = Math.max(this.slowDuration, duration);
    }
    
    /**
     * Check if enemy is in range of a position
     */
    public boolean isInRange(double px, double py, double range) {
        return Math2D.distance(x, y, px, py) <= range;
    }
    
    /**
     * Check if this enemy can be targeted by towers (most can, aircraft cannot)
     */
    public boolean canBeTargetedByTowers() {
        return true;
    }
    
    // Abstract/hook methods for subclasses
    protected abstract void updateSpecific(double deltaTime);
    protected abstract void onTakeDamage(int damage);
    protected abstract void onDeath();
    protected abstract void onReachedEnd();
    
    // Getters and setters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public double getSpeed() { return currentSpeed; }
    public double getBaseSpeed() { return baseSpeed; }
    public int getPower() { return power; }
    public int getSpriteIndex() { return spriteIndex; }
    public boolean isAlive() { return alive; }
    public boolean hasReachedEnd() { return reachedEnd; }
    public double getPathProgress() { return pathProgress; }
    public double getSlowMultiplier() { return slowMultiplier; }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}