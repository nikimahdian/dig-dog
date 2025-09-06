package game.entity;

import javafx.geometry.Point2D;

public abstract class Entity {
    
    protected Point2D position;
    public double maxHp; // Made public so WaveManager can modify it
    protected double currentHp;
    protected boolean alive;
    protected double width;
    protected double height;
    
    public Entity(double x, double y, double hp) {
        this.position = new Point2D(x, y);
        this.maxHp = hp;
        this.currentHp = hp;
        this.alive = true;
        this.width = 32;  // Default size
        this.height = 32;
    }
    
    public abstract void update(double deltaTime);
    
    public void takeDamage(double damage) {
        if (!alive) return;
        
        currentHp -= damage;
        if (currentHp <= 0) {
            currentHp = 0;
            alive = false;
            onDeath();
        }
    }
    
    public void heal(double amount) {
        if (!alive) return;
        
        currentHp = Math.min(maxHp, currentHp + amount);
    }
    
    protected void onDeath() {
        // Override in subclasses for death behavior
    }
    
    public Point2D getPosition() {
        return position;
    }
    
    public void setPosition(Point2D position) {
        this.position = position;
    }
    
    public void setPosition(double x, double y) {
        this.position = new Point2D(x, y);
    }
    
    public double getX() {
        return position.getX();
    }
    
    public double getY() {
        return position.getY();
    }
    
    public double getMaxHp() {
        return maxHp;
    }
    
    public double getCurrentHp() {
        return currentHp;
    }
    
    public double getHpRatio() {
        return maxHp > 0 ? currentHp / maxHp : 0;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void kill() {
        alive = false;
        currentHp = 0;
        onDeath();
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public Point2D getCenter() {
        return new Point2D(position.getX() + width / 2, position.getY() + height / 2);
    }
    
    public double getDistanceTo(Entity other) {
        return getCenter().distance(other.getCenter());
    }
    
    public boolean isInRange(Entity other, double range) {
        return getDistanceTo(other) <= range;
    }
}