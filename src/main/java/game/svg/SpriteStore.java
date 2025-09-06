package game.svg;

import javafx.scene.image.Image;
import game.Config;

import java.util.HashMap;
import java.util.Map;

public class SpriteStore {
    
    private static SpriteStore instance;
    private final Map<String, Image> sprites;
    private final SvgAtlasExtractor extractor;
    
    private SpriteStore() {
        this.sprites = new HashMap<>();
        
        try {
            // Use the SVG file path from the prompt
            String svgPath = "kenney_tower-defense-top-down/Vector/towerDefense_vector.svg";
            this.extractor = new SvgAtlasExtractor(svgPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SVG extractor", e);
        }
        
        loadAllSprites();
    }
    
    public static SpriteStore getInstance() {
        if (instance == null) {
            instance = new SpriteStore();
        }
        return instance;
    }
    
    private void loadAllSprites() {
        int tileSize = Config.TILE_SIZE;
        
        // Load tile sprites
        sprites.put("tile.grass", extractor.createSprite("tile.grass", tileSize, tileSize));
        sprites.put("tile.dirt", extractor.createSprite("tile.dirt", tileSize, tileSize));
        
        // Load tower sprites
        sprites.put("tower.fast", extractor.createSprite("tower.fast", tileSize, tileSize));
        sprites.put("tower.heavy", extractor.createSprite("tower.heavy", tileSize, tileSize));
        
        // Load enemy sprites
        sprites.put("enemy.soldier", extractor.createSprite("enemy.soldier", tileSize * 0.8, tileSize * 0.8));
        sprites.put("enemy.tank", extractor.createSprite("enemy.tank", tileSize * 0.9, tileSize * 0.9));
        sprites.put("enemy.plane", extractor.createSprite("enemy.plane", tileSize * 0.7, tileSize * 0.7));
        
        // Load UI sprites
        sprites.put("ui.wrench", extractor.createSprite("ui.wrench", tileSize * 0.4, tileSize * 0.4));
        
        // Load decoration sprites
        sprites.put("decoration.rock", extractor.createSprite("decoration.rock", tileSize * 0.3, tileSize * 0.3));
        sprites.put("decoration.bush", extractor.createSprite("decoration.bush", tileSize * 0.4, tileSize * 0.4));
        
        // Load projectile sprite
        sprites.put("projectile", extractor.createSprite("projectile", 8, 8));
    }
    
    public Image getSprite(String spriteName) {
        return sprites.get(spriteName);
    }
    
    public boolean hasSprite(String spriteName) {
        return sprites.containsKey(spriteName);
    }
    
    public void preloadSprite(String spriteName, double width, double height) {
        if (!sprites.containsKey(spriteName)) {
            sprites.put(spriteName, extractor.createSprite(spriteName, width, height));
        }
    }
    
    // Convenience methods for common sprites
    public Image getGrassTile() {
        return getSprite("tile.grass");
    }
    
    public Image getDirtTile() {
        return getSprite("tile.dirt");
    }
    
    public Image getFastTower() {
        return getSprite("tower.fast");
    }
    
    public Image getHeavyTower() {
        return getSprite("tower.heavy");
    }
    
    public Image getSoldier() {
        return getSprite("enemy.soldier");
    }
    
    public Image getTank() {
        return getSprite("enemy.tank");
    }
    
    public Image getPlane() {
        return getSprite("enemy.plane");
    }
    
    public Image getWrench() {
        return getSprite("ui.wrench");
    }
    
    public Image getRock() {
        return getSprite("decoration.rock");
    }
    
    public Image getBush() {
        return getSprite("decoration.bush");
    }
    
    public Image getProjectile() {
        return getSprite("projectile");
    }
}