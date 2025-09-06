package game.core;

import javafx.application.Platform;
import javafx.scene.image.Image;
import game.Config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceManager {
    
    private final Map<String, Image> sprites = new HashMap<>();
    private final AtomicInteger money;
    private final ScheduledExecutorService scheduler;
    private final Runnable onMoneyChanged;
    
    public ResourceManager(Runnable onMoneyChanged) {
        this.money = new AtomicInteger(Config.STARTING_MONEY);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ResourceManager");
            t.setDaemon(true);
            return t;
        });
        this.onMoneyChanged = onMoneyChanged;
        
        loadSprites();
        startMoneyGeneration();
    }
    
    private void loadSprites() {
        // Load all sprites based on Config.ASSET_MAP
        for (Map.Entry<String, String> entry : Config.ASSET_MAP.entrySet()) {
            String key = entry.getKey();
            String path = entry.getValue();
            
            try {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
                if (stream != null) {
                    Image image = new Image(stream);
                    sprites.put(key, image);
                } else {
                    System.err.println("Could not load sprite: " + path);
                }
            } catch (Exception e) {
                System.err.println("Error loading sprite " + path + ": " + e.getMessage());
            }
        }
    }
    
    public Image getSprite(String key) {
        return sprites.get(key);
    }
    
    private void startMoneyGeneration() {
        long periodMs = (long) (Config.MONEY_TICK_SEC * 1000);
        
        scheduler.scheduleAtFixedRate(() -> {
            money.addAndGet((int) Config.MONEY_PER_TICK);
            
            // Notify UI thread of money change
            if (onMoneyChanged != null) {
                Platform.runLater(onMoneyChanged);
            }
        }, periodMs, periodMs, TimeUnit.MILLISECONDS);
    }
    
    public int getMoney() {
        return money.get();
    }
    
    public boolean canAfford(int cost) {
        return money.get() >= cost;
    }
    
    public boolean spendMoney(int cost) {
        while (true) {
            int currentMoney = money.get();
            if (currentMoney < cost) {
                return false; // Can't afford
            }
            
            if (money.compareAndSet(currentMoney, currentMoney - cost)) {
                // Successfully spent money
                if (onMoneyChanged != null) {
                    Platform.runLater(onMoneyChanged);
                }
                return true;
            }
            // Retry if another thread modified the money in between
        }
    }
    
    public void addMoney(int amount) {
        if (amount > 0) {
            money.addAndGet(amount);
            if (onMoneyChanged != null) {
                Platform.runLater(onMoneyChanged);
            }
        }
    }
    
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}