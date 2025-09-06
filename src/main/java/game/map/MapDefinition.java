package game.map;

import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapDefinition {
    
    // Exact map geometry from the new prompt specification
    
    // Route A (left vertical lane): (0,0)...(0,8)
    private final List<Point2D> routeAPath = List.of(
        new Point2D(0, 0), new Point2D(0, 1), new Point2D(0, 2), new Point2D(0, 3),
        new Point2D(0, 4), new Point2D(0, 5), new Point2D(0, 6), new Point2D(0, 7), new Point2D(0, 8)
    );
    
    // Route B (top → right edge → left turn at mid)
    private final List<Point2D> routeBPath;
    
    // Build-slot positions: (2,2),(3,2),(2,3),(3,3),(2,5),(9,2),(9,3),(9,4),(9,6),(6,5)
    private final Set<Point2D> buildSlots = Set.of(
        new Point2D(2, 2), new Point2D(3, 2), new Point2D(2, 3), new Point2D(3, 3),
        new Point2D(2, 5), new Point2D(9, 2), new Point2D(9, 3), new Point2D(9, 4),
        new Point2D(9, 6), new Point2D(6, 5)
    );
    
    // Sand pad (top-right strip): (7,1),(8,1),(9,1)
    private final Set<Point2D> sandPadPositions = Set.of(
        new Point2D(7, 1), new Point2D(8, 1), new Point2D(9, 1)
    );
    
    // Speed-bump markers on path at (0,3) and (10,2)
    private final Set<Point2D> speedBumpSlots = Set.of(
        new Point2D(0, 3), new Point2D(10, 2)
    );
    
    // Bomb marker on (9,6)
    private final Set<Point2D> bombSlots = Set.of(
        new Point2D(9, 6)
    );
    
    // Minimal décor: Place rocks at (4,3), (3,6), (5,7)
    private final Set<Point2D> rockPositions = Set.of(
        new Point2D(4, 3), new Point2D(3, 6), new Point2D(5, 7)
    );
    
    public MapDefinition() {
        // Build Route B path: top → right edge → left turn at mid
        List<Point2D> routeBBuilder = new ArrayList<>();
        
        // Top row (1,0)…(9,0) → …004.png (horizontal)
        for (int x = 1; x <= 9; x++) {
            routeBBuilder.add(new Point2D(x, 0));
        }
        
        // Corner at (10,0): …005.png rotated 90° CW (left→down)
        routeBBuilder.add(new Point2D(10, 0));
        
        // Right vertical (10,1)…(10,5) → …009.png
        for (int y = 1; y <= 5; y++) {
            routeBBuilder.add(new Point2D(10, y));
        }
        
        // Corner at (10,6): …005.png rotated 180° (up→left)
        routeBBuilder.add(new Point2D(10, 6));
        
        // Mid horizontal (9,6),(8,6),(7,6),(6,6) → …004.png
        for (int x = 9; x >= 6; x--) {
            routeBBuilder.add(new Point2D(x, 6));
        }
        
        this.routeBPath = List.copyOf(routeBBuilder);
    }
    
    public List<Point2D> getRouteAPath() {
        return routeAPath;
    }
    
    public List<Point2D> getRouteBPath() {
        return routeBPath;
    }
    
    public Set<Point2D> getBuildSlots() {
        return buildSlots;
    }
    
    public Set<Point2D> getSandPadPositions() {
        return sandPadPositions;
    }
    
    public Set<Point2D> getSpeedBumpSlots() {
        return speedBumpSlots;
    }
    
    public Set<Point2D> getBombSlots() {
        return bombSlots;
    }
    
    public Set<Point2D> getRockPositions() {
        return rockPositions;
    }
    
    public boolean isBuildSlot(int x, int y) {
        return buildSlots.contains(new Point2D(x, y));
    }
    
    public boolean isSpeedBumpSlot(int x, int y) {
        return speedBumpSlots.contains(new Point2D(x, y));
    }
    
    public boolean isBombSlot(int x, int y) {
        return bombSlots.contains(new Point2D(x, y));
    }
    
    public boolean isSandPad(int x, int y) {
        return sandPadPositions.contains(new Point2D(x, y));
    }
    
    public boolean isRockPosition(int x, int y) {
        return rockPositions.contains(new Point2D(x, y));
    }
    
    // Path tile type determination based on exact prompt geometry
    public PathTileType getPathTileType(int x, int y) {
        Point2D pos = new Point2D(x, y);
        
        // Route A (left vertical) - all vertical tiles
        if (routeAPath.contains(pos)) {
            return PathTileType.VERTICAL;
        }
        
        // Route B analysis
        if (routeBPath.contains(pos)) {
            if (y == 0 && x >= 1 && x <= 9) {
                return PathTileType.HORIZONTAL; // Top row
            } else if (x == 10 && y >= 1 && y <= 5) {
                return PathTileType.VERTICAL; // Right vertical
            } else if (y == 6 && x >= 6 && x <= 9) {
                return PathTileType.HORIZONTAL; // Mid horizontal
            } else if (x == 10 && y == 0) {
                return PathTileType.CORNER_LEFT_DOWN; // Corner at (10,0) - rotated 90° CW
            } else if (x == 10 && y == 6) {
                return PathTileType.CORNER_UP_LEFT; // Corner at (10,6) - rotated 180°
            }
        }
        
        return PathTileType.NONE;
    }
    
    public enum PathTileType {
        NONE,
        HORIZONTAL,
        VERTICAL, 
        CORNER_LEFT_DOWN,  // 90° CW rotation
        CORNER_UP_LEFT     // 180° rotation
    }
    
    public List<Route> getRoutes() {
        List<Route> routes = new ArrayList<>();
        
        Route routeA = new Route("Route A");
        for (Point2D waypoint : routeAPath) {
            routeA.addWaypoint(waypoint);
        }
        
        Route routeB = new Route("Route B");
        for (Point2D waypoint : routeBPath) {
            routeB.addWaypoint(waypoint);
        }
        
        routes.add(routeA);
        routes.add(routeB);
        return routes;
    }
}