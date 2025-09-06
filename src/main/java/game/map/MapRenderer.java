package game.map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.geometry.Point2D;
import game.Config;
import game.core.ResourceManager;

public class MapRenderer {
    
    private final GraphicsContext gc;
    private final MapDefinition mapDefinition;
    private final ResourceManager resourceManager;
    
    public MapRenderer(GraphicsContext gc, MapDefinition mapDefinition, ResourceManager resourceManager) {
        this.gc = gc;
        this.mapDefinition = mapDefinition;
        this.resourceManager = resourceManager;
    }
    
    public void render() {
        // Clear the canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, Config.MAP_WIDTH, Config.MAP_HEIGHT);
        
        // Render in order: grass → dirt paths → sand → slots → decorations
        renderGrass();
        renderPaths();
        renderSandPad();
        renderSlots();
        renderDecorations();
    }
    
    private void renderGrass() {
        Image grassSprite = resourceManager.getSprite("grass");
        
        for (int x = 0; x < Config.GRID_W; x++) {
            for (int y = 0; y < Config.GRID_H; y++) {
                double pixelX = x * Config.TILE_SIZE;
                double pixelY = y * Config.TILE_SIZE;
                
                if (grassSprite != null) {
                    gc.drawImage(grassSprite, pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
                } else {
                    // Fallback grass color
                    gc.setFill(Color.GREEN);
                    gc.fillRect(pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
                }
            }
        }
    }
    
    private void renderPaths() {
        Image dirtHSprite = resourceManager.getSprite("dirtH");
        Image dirtVSprite = resourceManager.getSprite("dirtV");
        Image cornerSprite = resourceManager.getSprite("corner");
        
        // Route A (left vertical lane): (0,0)…(0,8) → draw …009.png (vertical) on each cell
        for (int y = 0; y <= 8; y++) {
            double pixelX = 0 * Config.TILE_SIZE;
            double pixelY = y * Config.TILE_SIZE;
            
            if (dirtVSprite != null) {
                gc.drawImage(dirtVSprite, pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            } else {
                gc.setFill(Color.SADDLEBROWN);
                gc.fillRect(pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            }
        }
        
        // Route B (top → right edge → left turn at mid):
        
        // Top row (1,0)…(9,0) → …004.png (horizontal)
        for (int x = 1; x <= 9; x++) {
            double pixelX = x * Config.TILE_SIZE;
            double pixelY = 0 * Config.TILE_SIZE;
            
            if (dirtHSprite != null) {
                gc.drawImage(dirtHSprite, pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            } else {
                gc.setFill(Color.SADDLEBROWN);
                gc.fillRect(pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            }
        }
        
        // Corner at (10,0): …005.png rotated 90° CW (left→down)
        double corner1X = 10 * Config.TILE_SIZE;
        double corner1Y = 0 * Config.TILE_SIZE;
        if (cornerSprite != null) {
            renderRotatedSprite(cornerSprite, corner1X, corner1Y, 90);
        } else {
            gc.setFill(Color.SADDLEBROWN);
            gc.fillRect(corner1X, corner1Y, Config.TILE_SIZE, Config.TILE_SIZE);
        }
        
        // Right vertical (10,1)…(10,5) → …009.png
        for (int y = 1; y <= 5; y++) {
            double pixelX = 10 * Config.TILE_SIZE;
            double pixelY = y * Config.TILE_SIZE;
            
            if (dirtVSprite != null) {
                gc.drawImage(dirtVSprite, pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            } else {
                gc.setFill(Color.SADDLEBROWN);
                gc.fillRect(pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            }
        }
        
        // Corner at (10,6): …005.png rotated 180° (up→left)
        double corner2X = 10 * Config.TILE_SIZE;
        double corner2Y = 6 * Config.TILE_SIZE;
        if (cornerSprite != null) {
            renderRotatedSprite(cornerSprite, corner2X, corner2Y, 180);
        } else {
            gc.setFill(Color.SADDLEBROWN);
            gc.fillRect(corner2X, corner2Y, Config.TILE_SIZE, Config.TILE_SIZE);
        }
        
        // Mid horizontal (9,6),(8,6),(7,6),(6,6) → …004.png
        for (int x = 9; x >= 6; x--) {
            double pixelX = x * Config.TILE_SIZE;
            double pixelY = 6 * Config.TILE_SIZE;
            
            if (dirtHSprite != null) {
                gc.drawImage(dirtHSprite, pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            } else {
                gc.setFill(Color.SADDLEBROWN);
                gc.fillRect(pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            }
        }
    }
    
    private void renderSandPad() {
        Image sandSprite = resourceManager.getSprite("sand");
        
        // Sand pad (top-right strip): (7,1),(8,1),(9,1) → …029.png (repeat)
        // (Pad is visible below three build slots, as in sample.jpg.)
        int[] sandX = {7, 8, 9};
        int sandY = 1;
        
        for (int x : sandX) {
            double pixelX = x * Config.TILE_SIZE;
            double pixelY = sandY * Config.TILE_SIZE;
            
            if (sandSprite != null) {
                gc.drawImage(sandSprite, pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            } else {
                gc.setFill(Color.LIGHTYELLOW);
                gc.fillRect(pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            }
        }
    }
    
    private void renderSlots() {
        Image buildSlotSprite = resourceManager.getSprite("slot.build");
        Image speedSlotSprite = resourceManager.getSprite("slot.speed");
        Image bombSlotSprite = resourceManager.getSprite("slot.bomb");
        
        // Build-slot positions: (2,2),(3,2),(2,3),(3,3),(2,5),(9,2),(9,3),(9,4),(9,6),(6,5) → draw …039.png
        int[][] buildSlots = {{2,2}, {3,2}, {2,3}, {3,3}, {2,5}, {9,2}, {9,3}, {9,4}, {9,6}, {6,5}};
        
        for (int[] slot : buildSlots) {
            double pixelX = slot[0] * Config.TILE_SIZE;
            double pixelY = slot[1] * Config.TILE_SIZE;
            
            if (buildSlotSprite != null) {
                gc.drawImage(buildSlotSprite, pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            } else {
                gc.setFill(Color.LIGHTGREEN);
                gc.fillRect(pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            }
        }
        
        // Speed-bump markers on path at (0,3) and (10,2) → draw …040.png
        int[][] speedBumpSlots = {{0,3}, {10,2}};
        
        for (int[] slot : speedBumpSlots) {
            double pixelX = slot[0] * Config.TILE_SIZE;
            double pixelY = slot[1] * Config.TILE_SIZE;
            
            if (speedSlotSprite != null) {
                gc.drawImage(speedSlotSprite, pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            } else {
                gc.setFill(Color.YELLOW);
                gc.fillRect(pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            }
        }
        
        // Bomb marker on (9,6) → draw …092.png
        double bombX = 9 * Config.TILE_SIZE;
        double bombY = 6 * Config.TILE_SIZE;
        
        if (bombSlotSprite != null) {
            gc.drawImage(bombSlotSprite, bombX, bombY, Config.TILE_SIZE, Config.TILE_SIZE);
        } else {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(bombX, bombY, Config.TILE_SIZE, Config.TILE_SIZE);
        }
    }
    
    private void renderDecorations() {
        Image rockSprite = resourceManager.getSprite("rock");
        
        // Minimal décor (match vibe): Place rocks …059.png at (4,3), (3,6), (5,7)
        int[][] rockPositions = {{4,3}, {3,6}, {5,7}};
        
        for (int[] pos : rockPositions) {
            double pixelX = pos[0] * Config.TILE_SIZE;
            double pixelY = pos[1] * Config.TILE_SIZE;
            
            if (rockSprite != null) {
                gc.drawImage(rockSprite, pixelX, pixelY, Config.TILE_SIZE, Config.TILE_SIZE);
            } else {
                gc.setFill(Color.DARKGRAY);
                gc.fillOval(pixelX + 10, pixelY + 10, Config.TILE_SIZE - 20, Config.TILE_SIZE - 20);
            }
        }
    }
    
    private void renderRotatedSprite(Image sprite, double x, double y, double degrees) {
        gc.save();
        
        // Move to tile center, rotate, then draw sprite offset back to corner
        double centerX = x + Config.TILE_SIZE / 2.0;
        double centerY = y + Config.TILE_SIZE / 2.0;
        
        Rotate rotate = new Rotate(degrees, centerX, centerY);
        gc.setTransform(rotate.getMxx(), rotate.getMyx(), rotate.getMxy(), rotate.getMyy(), rotate.getTx(), rotate.getTy());
        
        gc.drawImage(sprite, x, y, Config.TILE_SIZE, Config.TILE_SIZE);
        
        gc.restore();
    }
}