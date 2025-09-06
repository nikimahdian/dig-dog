package game.assets;

import javafx.scene.image.Image;
import game.Config;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    
    private static AssetManager instance;
    private final Map<String, Image> loadedAssets;
    private final String assetBasePath;
    
    private AssetManager() {
        this.loadedAssets = new HashMap<>();
        this.assetBasePath = "kenney_tower-defense-top-down/PNG/";
        loadAllAssets();
    }
    
    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }
    
    private void loadAllAssets() {
        for (Map.Entry<String, String> entry : Config.ASSET_MAP.entrySet()) {
            String assetKey = entry.getKey();
            String assetPath = entry.getValue();
            
            try {
                Image image = loadImage(assetPath);
                if (image != null) {
                    loadedAssets.put(assetKey, image);
                } else {
                    System.err.println("Warning: Failed to load asset: " + assetPath);
                }
            } catch (Exception e) {
                System.err.println("Error loading asset " + assetPath + ": " + e.getMessage());
            }
        }
        
        System.out.println("Loaded " + loadedAssets.size() + " assets");
    }
    
    private Image loadImage(String relativePath) {
        String fullPath = assetBasePath + relativePath;
        File imageFile = new File(fullPath);
        
        if (!imageFile.exists()) {
            System.err.println("Asset file not found: " + fullPath);
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            return new Image(fis);
        } catch (Exception e) {
            System.err.println("Error loading image " + fullPath + ": " + e.getMessage());
            return null;
        }
    }
    
    public Image getAsset(String assetKey) {
        return loadedAssets.get(assetKey);
    }
    
    public boolean hasAsset(String assetKey) {
        return loadedAssets.containsKey(assetKey);
    }
    
    // Convenience methods for common assets
    public Image getGrass() {
        return getAsset("grass");
    }
    
    public Image getDirtHorizontal() {
        return getAsset("dirtH");
    }
    
    public Image getDirtVertical() {
        return getAsset("dirtV");
    }
    
    public Image getCorner() {
        return getAsset("corner");
    }
    
    public Image getSand() {
        return getAsset("sand");
    }
    
    public Image getBuildSlot() {
        return getAsset("slot.build");
    }
    
    public Image getSpeedBumpSlot() {
        return getAsset("slot.speed");
    }
    
    public Image getBombSlot() {
        return getAsset("slot.bomb");
    }
    
    public Image getRock() {
        return getAsset("rock");
    }
    
    public Image getFastTower() {
        return getAsset("tower.fast");
    }
    
    public Image getHeavyTower() {
        return getAsset("tower.heavy");
    }
    
    public Image getAA60Tower() {
        return getAsset("tower.aa60");
    }
    
    public Image getAA80Tower() {
        return getAsset("tower.aa80");
    }
    
    public Image getSoldier() {
        return getAsset("enemy.soldier");
    }
    
    public Image getTank() {
        return getAsset("enemy.tank");
    }
    
    public Image getPlane() {
        return getAsset("enemy.plane");
    }
    
    public Image getBullet() {
        return getAsset("proj.bullet");
    }
    
    public Image getTankShell() {
        return getAsset("proj.tank");
    }
    
    public Image getAA60Projectile() {
        return getAsset("proj.aa60");
    }
    
    public Image getAA80Projectile() {
        return getAsset("proj.aa80");
    }
}