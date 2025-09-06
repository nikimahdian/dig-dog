package game.map;

import game.Config;
import javafx.geometry.Point2D;

public class Grid {
    
    private final Tile[][] tiles;
    private final int width;
    private final int height;
    
    public Grid() {
        this.width = Config.GRID_W;
        this.height = Config.GRID_H;
        this.tiles = new Tile[width][height];
        
        // Initialize all tiles as grass by default
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = Tile.GRASS;
            }
        }
    }
    
    public Tile getTile(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return tiles[x][y];
        }
        return Tile.GRASS; // Default for out-of-bounds
    }
    
    public void setTile(int x, int y, Tile tile) {
        if (isValidCoordinate(x, y)) {
            tiles[x][y] = tile;
        }
    }
    
    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    public Point2D tileToPixel(int x, int y) {
        return new Point2D(x * Config.TILE_SIZE, y * Config.TILE_SIZE);
    }
    
    public Point2D pixelToTile(double pixelX, double pixelY) {
        int tileX = (int) (pixelX / Config.TILE_SIZE);
        int tileY = (int) (pixelY / Config.TILE_SIZE);
        return new Point2D(tileX, tileY);
    }
    
    public Point2D getTileCenter(int x, int y) {
        Point2D pixel = tileToPixel(x, y);
        return new Point2D(pixel.getX() + Config.TILE_SIZE / 2.0, 
                          pixel.getY() + Config.TILE_SIZE / 2.0);
    }
    
    public double getDistance(int x1, int y1, int x2, int y2) {
        Point2D center1 = getTileCenter(x1, y1);
        Point2D center2 = getTileCenter(x2, y2);
        return center1.distance(center2) / Config.TILE_SIZE; // Distance in tiles
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}