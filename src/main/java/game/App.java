package game;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Point2D;
import game.core.GameLoop;
import game.core.ResourceManager;
import game.core.WaveManager;
import game.map.MapRenderer;
import game.map.MapDefinition;
import game.ui.HudView;
import game.ui.BuildMenu;

public class App {
    
    private BorderPane root;
    private GameLoop gameLoop;
    private MapRenderer mapRenderer;
    private HudView hudView;
    private Canvas canvas;
    private ResourceManager resourceManager;
    private WaveManager waveManager;
    private MapDefinition mapDefinition;
    private BuildMenu buildMenu;
    
    public App() {
        initializeUI();
        initializeGame();
    }
    
    private void initializeUI() {
        root = new BorderPane();
        canvas = new Canvas(Config.MAP_WIDTH, Config.MAP_HEIGHT);
        
        root.setCenter(canvas);
        
        // Set up mouse handling
        canvas.setOnMouseClicked(this::handleMouseClick);
    }
    
    private void initializeGame() {
        // Initialize core components
        mapDefinition = new MapDefinition();
        resourceManager = new ResourceManager(() -> {
            if (hudView != null) hudView.updateMoney(resourceManager.getMoney());
        });
        waveManager = new WaveManager(mapDefinition);
        
        // Initialize renderer
        mapRenderer = new MapRenderer(canvas.getGraphicsContext2D(), mapDefinition, resourceManager);
        
        // Initialize UI
        hudView = new HudView();
        buildMenu = new BuildMenu();
        root.setTop(hudView.getRoot());
        
        // Initialize game loop
        gameLoop = new GameLoop(canvas.getGraphicsContext2D(), mapDefinition, resourceManager, waveManager, hudView);
    }
    
    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        
        // Convert to tile coordinates
        int tileX = (int) (x / Config.TILE_SIZE);
        int tileY = (int) (y / Config.TILE_SIZE);
        
        // Check if clicking on a valid tile
        if (tileX < 0 || tileX >= Config.GRID_W || tileY < 0 || tileY >= Config.GRID_H) {
            return;
        }
        
        // Handle build slot clicks
        if (mapDefinition.isBuildSlot(tileX, tileY)) {
            gameLoop.handleBuildSlotClick(tileX, tileY);
        }
        
        // Handle gadget slot clicks
        if (mapDefinition.isSpeedBumpSlot(tileX, tileY)) {
            gameLoop.handleSpeedBumpSlotClick(tileX, tileY);
        }
        
        if (mapDefinition.isBombSlot(tileX, tileY)) {
            gameLoop.handleBombSlotClick(tileX, tileY);
        }
    }
    
    public Parent getRoot() {
        return root;
    }
    
    public void start() {
        gameLoop.start();
    }
    
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (resourceManager != null) {
            resourceManager.shutdown();
        }
    }
}