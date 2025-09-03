package com.tdgame.model.systems;

import com.tdgame.config.GameConfig;
import com.tdgame.model.grid.GridMap;
import com.tdgame.util.Math2D;

import java.util.List;

/**
 * Pathfinding system for enemy movement.
 * Currently uses serialized waypoints from level data.
 * Clean seam provided for future A* implementation.
 */
public class Pathfinding {
    
    private final GameConfig config;
    private final GridMap gridMap;
    
    public Pathfinding(GameConfig config, GridMap gridMap) {
        this.config = config;
        this.gridMap = gridMap;
    }
    
    /**
     * Get the default enemy path (main path from level data)
     */
    public GridMap.Path getEnemyPath() {
        return gridMap.getMainPath();
    }
    
    /**
     * Calculate position along path for given progress (0.0 to 1.0)
     */
    public Math2D.Point getPositionOnPath(double progress) {
        GridMap.Path path = getEnemyPath();
        return path != null ? path.getPositionAt(progress) : new Math2D.Point(0, 0);
    }
    
    /**
     * Get total length of the enemy path
     */
    public double getPathLength() {
        GridMap.Path path = getEnemyPath();
        return path != null ? path.getTotalLength() : 0.0;
    }
    
    /**
     * Check if a position is on the enemy path (within tolerance)
     */
    public boolean isOnPath(double x, double y, double tolerance) {
        GridMap.Path path = getEnemyPath();
        if (path == null) return false;
        
        List<Math2D.Point> waypoints = path.getWaypoints();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Math2D.Point from = waypoints.get(i);
            Math2D.Point to = waypoints.get(i + 1);
            
            if (distanceToLineSegment(x, y, from.x, from.y, to.x, to.y) <= tolerance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate distance from point to line segment
     */
    private double distanceToLineSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        if (length == 0) {
            return Math2D.distance(px, py, x1, y1);
        }
        
        double t = Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy) / (length * length)));
        double projX = x1 + t * dx;
        double projY = y1 + t * dy;
        
        return Math2D.distance(px, py, projX, projY);
    }
    
    // TODO: Future A* implementation would go here
    // public List<Math2D.Point> findPath(Math2D.Point start, Math2D.Point end) { ... }
}