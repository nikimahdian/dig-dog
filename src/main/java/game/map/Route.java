package game.map;

import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Route {
    
    private final List<Point2D> waypoints;
    private final String name;
    
    public Route(String name) {
        this.name = name;
        this.waypoints = new ArrayList<>();
    }
    
    public void addWaypoint(int x, int y) {
        waypoints.add(new Point2D(x, y));
    }
    
    public void addWaypoint(Point2D point) {
        waypoints.add(point);
    }
    
    public List<Point2D> getWaypoints() {
        return new ArrayList<>(waypoints);
    }
    
    public Point2D getWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) {
            return waypoints.get(index);
        }
        return null;
    }
    
    public int getWaypointCount() {
        return waypoints.size();
    }
    
    public Point2D getStartPoint() {
        return waypoints.isEmpty() ? null : waypoints.get(0);
    }
    
    public Point2D getEndPoint() {
        return waypoints.isEmpty() ? null : waypoints.get(waypoints.size() - 1);
    }
    
    public String getName() {
        return name;
    }
    
    public Point2D getPixelPosition(int waypointIndex, double progress) {
        if (waypointIndex < 0 || waypointIndex >= waypoints.size() - 1) {
            return null;
        }
        
        Point2D start = waypoints.get(waypointIndex);
        Point2D end = waypoints.get(waypointIndex + 1);
        
        double startX = start.getX() * game.Config.TILE_SIZE + game.Config.TILE_SIZE / 2.0;
        double startY = start.getY() * game.Config.TILE_SIZE + game.Config.TILE_SIZE / 2.0;
        double endX = end.getX() * game.Config.TILE_SIZE + game.Config.TILE_SIZE / 2.0;
        double endY = end.getY() * game.Config.TILE_SIZE + game.Config.TILE_SIZE / 2.0;
        
        double x = startX + (endX - startX) * progress;
        double y = startY + (endY - startY) * progress;
        
        return new Point2D(x, y);
    }
    
    public double getTotalLength() {
        double length = 0;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Point2D p1 = waypoints.get(i);
            Point2D p2 = waypoints.get(i + 1);
            length += p1.distance(p2);
        }
        return length;
    }
}