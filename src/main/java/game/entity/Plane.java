package game.entity;

import javafx.geometry.Point2D;
import game.Config;
import java.util.List;

public class Plane extends Entity {
    
    private final Point2D startPosition;
    private final Point2D endPosition;
    private final double totalDistance;
    private double travelDistance;
    private boolean hasBombed;
    private final int powerValue;
    
    public Plane(Point2D start, Point2D end) {
        super(start.getX(), start.getY(), 1); // Planes have minimal HP, rely on AA to shoot them
        this.startPosition = start;
        this.endPosition = end;
        this.totalDistance = start.distance(end);
        this.travelDistance = 0;
        this.hasBombed = false;
        this.powerValue = Config.PLANE_POWER;
        this.width = Config.TILE_SIZE * 0.7;
        this.height = Config.TILE_SIZE * 0.7;
    }
    
    @Override
    public void update(double deltaTime) {
        if (!alive) return;
        
        // Move towards end position
        double moveDistance = Config.PLANE_SPEED * Config.TILE_SIZE * deltaTime;
        travelDistance += moveDistance;
        
        if (travelDistance >= totalDistance) {
            // Reached the end - remove plane
            alive = false;
            return;
        }
        
        // Update position along the flight path
        double progress = travelDistance / totalDistance;
        double newX = startPosition.getX() + (endPosition.getX() - startPosition.getX()) * progress;
        double newY = startPosition.getY() + (endPosition.getY() - startPosition.getY()) * progress;
        setPosition(newX, newY);
        
        // Bomb at the optimal point (middle of the map or when over high-value targets)
        if (!hasBombed && progress >= 0.4 && progress <= 0.6) {
            performBombing();
            hasBombed = true;
        }
    }
    
    private void performBombing() {
        // This method would need access to the game's tower and build slot lists
        // For now, we'll leave it as a stub that can be called by the game loop
    }
    
    public void performBombingOnTargets(List<Tower> towers, List<Point2D> buildSlots) {
        if (hasBombed) return;
        
        // Find the optimal row or column to bomb based on player value
        BombTarget optimalTarget = findOptimalRowOrColumn(towers, buildSlots);
        
        if (optimalTarget != null) {
            bombRowOrColumn(optimalTarget, towers);
            hasBombed = true;
        }
    }
    
    private static class BombTarget {
        final boolean isRow;
        final int index;
        final double totalValue;
        
        BombTarget(boolean isRow, int index, double totalValue) {
            this.isRow = isRow;
            this.index = index;
            this.totalValue = totalValue;
        }
    }
    
    private BombTarget findOptimalRowOrColumn(List<Tower> towers, List<Point2D> buildSlots) {
        BombTarget bestTarget = null;
        double maxValue = 0;
        
        // Evaluate each row (0-8)
        for (int row = 0; row < Config.GRID_H; row++) {
            double rowValue = calculateRowValue(row, towers);
            if (rowValue > maxValue) {
                maxValue = rowValue;
                bestTarget = new BombTarget(true, row, rowValue);
            }
        }
        
        // Evaluate each column (0-10)  
        for (int col = 0; col < Config.GRID_W; col++) {
            double colValue = calculateColumnValue(col, towers);
            if (colValue > maxValue) {
                maxValue = colValue;
                bestTarget = new BombTarget(false, col, colValue);
            }
        }
        
        return bestTarget;
    }
    
    private double calculateRowValue(int row, List<Tower> towers) {
        double totalValue = 0;
        double rowY = row * Config.TILE_SIZE;
        double bombRadius = Config.TILE_SIZE * 1.5;
        
        for (Tower tower : towers) {
            if (!tower.isAlive()) continue;
            
            double towerY = tower.getY() + tower.getHeight() / 2;
            if (Math.abs(towerY - rowY) <= bombRadius) {
                // Tower value = cost + remaining HP factor
                totalValue += tower.getCost() + (tower.getCurrentHp() / tower.getMaxHp() * 20);
            }
        }
        
        return totalValue;
    }
    
    private double calculateColumnValue(int col, List<Tower> towers) {
        double totalValue = 0;
        double colX = col * Config.TILE_SIZE;
        double bombRadius = Config.TILE_SIZE * 1.5;
        
        for (Tower tower : towers) {
            if (!tower.isAlive()) continue;
            
            double towerX = tower.getX() + tower.getWidth() / 2;
            if (Math.abs(towerX - colX) <= bombRadius) {
                // Tower value = cost + remaining HP factor
                totalValue += tower.getCost() + (tower.getCurrentHp() / tower.getMaxHp() * 20);
            }
        }
        
        return totalValue;
    }
    
    private void bombRowOrColumn(BombTarget target, List<Tower> towers) {
        double bombRadius = Config.TILE_SIZE * 1.5;
        double bombDamage = 50;
        
        if (target.isRow) {
            // Bomb entire row
            double rowY = target.index * Config.TILE_SIZE + Config.TILE_SIZE / 2;
            for (int col = 0; col < Config.GRID_W; col++) {
                double cellX = col * Config.TILE_SIZE + Config.TILE_SIZE / 2;
                Point2D bombPoint = new Point2D(cellX, rowY);
                damageTowersInRadius(bombPoint, bombRadius, bombDamage, towers);
            }
        } else {
            // Bomb entire column
            double colX = target.index * Config.TILE_SIZE + Config.TILE_SIZE / 2;
            for (int row = 0; row < Config.GRID_H; row++) {
                double cellY = row * Config.TILE_SIZE + Config.TILE_SIZE / 2;
                Point2D bombPoint = new Point2D(colX, cellY);
                damageTowersInRadius(bombPoint, bombRadius, bombDamage, towers);
            }
        }
    }
    
    private void damageTowersInRadius(Point2D center, double radius, double damage, List<Tower> towers) {
        for (Tower tower : towers) {
            if (!tower.isAlive()) continue;
            
            double distance = tower.getCenter().distance(center);
            if (distance <= radius) {
                tower.takeDamage(damage);
            }
        }
    }
    
    private void bombArea(Point2D target, List<Tower> towers) {
        double bombRadius = Config.TILE_SIZE * 1.5; // Bomb affects 1.5 tile radius
        double bombDamage = 50; // Fixed bomb damage
        
        // Damage all towers in bomb radius
        for (Tower tower : towers) {
            if (!tower.isAlive()) continue;
            
            double distance = tower.getCenter().distance(target);
            if (distance <= bombRadius) {
                tower.takeDamage(bombDamage);
            }
        }
    }
    
    @Override
    protected void onDeath() {
        // Planes are destroyed by AA fire
    }
    
    public int getPowerValue() {
        return powerValue;
    }
    
    public boolean hasLeaked() {
        return !alive && travelDistance >= totalDistance && !hasBombed;
    }
    
    public boolean hasBombed() {
        return hasBombed;
    }
    
    public double getProgress() {
        return totalDistance > 0 ? travelDistance / totalDistance : 0;
    }
    
    public Point2D getStartPosition() {
        return startPosition;
    }
    
    public Point2D getEndPosition() {
        return endPosition;
    }
}