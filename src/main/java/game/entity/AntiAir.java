package game.entity;

import game.Config;
import java.util.List;
import java.util.Random;

public abstract class AntiAir extends Entity {
    
    protected final double hitChance;
    protected final int cost;
    protected double fireTimer;
    protected Plane target;
    protected final Random random;
    
    public AntiAir(double hitChance, int cost) {
        super(0, 0, 100); // AA doesn't have a physical position on map, arbitrary HP
        this.hitChance = hitChance;
        this.cost = cost;
        this.fireTimer = 0;
        this.target = null;
        this.random = new Random();
        this.width = 0;  // AA has global coverage
        this.height = 0;
    }
    
    @Override
    public void update(double deltaTime) {
        if (!alive) return;
        
        fireTimer -= deltaTime;
        
        // Fire at planes with our hit chance
        if (fireTimer <= 0 && target != null && target.isAlive()) {
            fireAtPlane(target);
            fireTimer = 1.0; // 1 second between shots
        }
    }
    
    public void setPlanesInRange(List<Plane> planes) {
        // AA has global coverage, so all planes are "in range"
        if (target != null && target.isAlive()) {
            return; // Keep current target
        }
        
        // Target the plane closest to completing its bombing run
        target = findBestTarget(planes);
    }
    
    private Plane findBestTarget(List<Plane> planes) {
        Plane bestTarget = null;
        double highestProgress = -1;
        
        for (Plane plane : planes) {
            if (!plane.isAlive()) continue;
            
            double progress = plane.getProgress();
            if (progress > highestProgress) {
                highestProgress = progress;
                bestTarget = plane;
            }
        }
        
        return bestTarget;
    }
    
    private void fireAtPlane(Plane plane) {
        // Roll for hit
        if (random.nextDouble() < hitChance) {
            // Hit! Destroy the plane
            plane.kill();
            target = null;
        }
        // Miss - plane continues
    }
    
    public double getHitChance() {
        return hitChance;
    }
    
    public int getCost() {
        return cost;
    }
    
    public Plane getTarget() {
        return target;
    }
    
    @Override
    protected void onDeath() {
        // AA system destroyed
    }
}