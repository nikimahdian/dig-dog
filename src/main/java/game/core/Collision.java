package game.core;

import javafx.geometry.Point2D;
import game.entity.Entity;
import game.Config;
import java.util.List;
import java.util.ArrayList;

public class Collision {
    
    public static boolean isInRange(Entity entity1, Entity entity2, double range) {
        double distance = entity1.getCenter().distance(entity2.getCenter());
        return distance <= range;
    }
    
    public static double getDistance(Entity entity1, Entity entity2) {
        return entity1.getCenter().distance(entity2.getCenter());
    }
    
    public static double getDistance(Point2D point1, Point2D point2) {
        return point1.distance(point2);
    }
    
    public static List<Entity> getEntitiesInRange(Entity center, List<? extends Entity> entities, double range) {
        List<Entity> entitiesInRange = new ArrayList<>();
        
        for (Entity entity : entities) {
            if (entity == center || !entity.isAlive()) continue;
            
            if (isInRange(center, entity, range)) {
                entitiesInRange.add(entity);
            }
        }
        
        return entitiesInRange;
    }
    
    public static List<Entity> getEntitiesInArea(Point2D center, List<? extends Entity> entities, double radius) {
        List<Entity> entitiesInArea = new ArrayList<>();
        
        for (Entity entity : entities) {
            if (!entity.isAlive()) continue;
            
            double distance = entity.getCenter().distance(center);
            if (distance <= radius) {
                entitiesInArea.add(entity);
            }
        }
        
        return entitiesInArea;
    }
    
    public static boolean hasLineOfSight(Point2D start, Point2D end, List<Point2D> obstacles) {
        // Simple line of sight check - in a more complex implementation,
        // this would check for obstacles along the line
        
        // For now, we'll assume clear line of sight unless there are obstacles
        // directly on the path
        
        if (obstacles == null || obstacles.isEmpty()) {
            return true;
        }
        
        // Check if any obstacles are close to the line
        for (Point2D obstacle : obstacles) {
            double distanceToLine = distanceFromPointToLine(start, end, obstacle);
            if (distanceToLine < Config.TILE_SIZE / 2.0) {
                return false;
            }
        }
        
        return true;
    }
    
    private static double distanceFromPointToLine(Point2D lineStart, Point2D lineEnd, Point2D point) {
        double A = point.getX() - lineStart.getX();
        double B = point.getY() - lineStart.getY();
        double C = lineEnd.getX() - lineStart.getX();
        double D = lineEnd.getY() - lineStart.getY();
        
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        
        if (lenSq == 0) {
            // Line start and end are the same point
            return lineStart.distance(point);
        }
        
        double param = dot / lenSq;
        
        double xx, yy;
        
        if (param < 0) {
            xx = lineStart.getX();
            yy = lineStart.getY();
        } else if (param > 1) {
            xx = lineEnd.getX();
            yy = lineEnd.getY();
        } else {
            xx = lineStart.getX() + param * C;
            yy = lineStart.getY() + param * D;
        }
        
        double dx = point.getX() - xx;
        double dy = point.getY() - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public static boolean isPointInCircle(Point2D point, Point2D circleCenter, double radius) {
        return point.distance(circleCenter) <= radius;
    }
    
    public static boolean isPointInRectangle(Point2D point, Point2D rectTopLeft, double width, double height) {
        return point.getX() >= rectTopLeft.getX() &&
               point.getX() <= rectTopLeft.getX() + width &&
               point.getY() >= rectTopLeft.getY() &&
               point.getY() <= rectTopLeft.getY() + height;
    }
    
    public static Point2D getTileCenter(int tileX, int tileY) {
        return new Point2D(
            tileX * Config.TILE_SIZE + Config.TILE_SIZE / 2.0,
            tileY * Config.TILE_SIZE + Config.TILE_SIZE / 2.0
        );
    }
    
    public static Point2D pixelToTile(double pixelX, double pixelY) {
        return new Point2D(
            Math.floor(pixelX / Config.TILE_SIZE),
            Math.floor(pixelY / Config.TILE_SIZE)
        );
    }
    
    public static boolean isValidTileCoordinate(int x, int y) {
        return x >= 0 && x < Config.GRID_W && y >= 0 && y < Config.GRID_H;
    }
}