package com.tdgame.view;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads and manages sprite images from the asset pack.
 * Provides caching to avoid reloading images.
 */
public class SpriteLoader {
    
    private static final SpriteLoader INSTANCE = new SpriteLoader();
    private final Map<Integer, Image> spriteCache = new HashMap<>();
    private final String basePath = "assets/kenney/PNG/Default size/";
    
    private SpriteLoader() {}
    
    public static SpriteLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Load a sprite by its index (1-299)
     */
    public Image getSprite(int index) {
        if (spriteCache.containsKey(index)) {
            return spriteCache.get(index);
        }
        
        try {
            String filename = String.format("towerDefense_tile%03d.png", index);
            String resourcePath = "/" + basePath + filename;
            
            // Load image from resources
            var inputStream = getClass().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                System.err.println("Could not load sprite: " + resourcePath);
                return createMissingImage();
            }
            
            Image image = new Image(inputStream);
            spriteCache.put(index, image);
            return image;
            
        } catch (Exception e) {
            System.err.println("Error loading sprite " + index + ": " + e.getMessage());
            return createMissingImage();
        }
    }
    
    /**
     * Create a placeholder image for missing sprites
     */
    private Image createMissingImage() {
        // Create a simple colored square as placeholder
        if (!spriteCache.containsKey(-1)) {
            // This would be a procedurally generated image in a full implementation
            // For now, we'll return null and handle it in the rendering code
            spriteCache.put(-1, null);
        }
        return spriteCache.get(-1);
    }
    
    /**
     * Preload commonly used sprites
     */
    public void preloadCommonSprites() {
        // Ground tiles
        for (int i = 1; i <= 10; i++) {
            getSprite(i);
        }
        
        // Path tiles
        for (int i = 10; i <= 20; i++) {
            getSprite(i);
        }
        
        // Structure sprites
        for (int i = 100; i <= 150; i++) {
            getSprite(i);
        }
        
        // Enemy sprites
        for (int i = 250; i <= 280; i++) {
            getSprite(i);
        }
    }
    
    /**
     * Clear the sprite cache to free memory
     */
    public void clearCache() {
        spriteCache.clear();
    }
    
    /**
     * Get cache size for debugging
     */
    public int getCacheSize() {
        return spriteCache.size();
    }
}