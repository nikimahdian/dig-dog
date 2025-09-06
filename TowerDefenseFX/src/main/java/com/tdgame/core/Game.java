package com.tdgame.core;

import com.tdgame.config.GameConfig;
import com.tdgame.model.grid.GridMap;
import com.tdgame.model.systems.*;
import com.tdgame.view.GameCanvas;
import com.tdgame.view.HUD;
import com.tdgame.controller.InputController;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;

/**
 * Central game loop and state management.
 * Coordinates all game systems and manages the update/render cycle.
 */
public class Game {
    
    private final Stage stage;
    private final GameConfig config;
    private final GridMap gridMap;
    
    // Core systems
    private WaveManager waveManager;
    private CombatSystem combatSystem;
    private EconomyManager economyManager;
    private AircraftStrikeSystem aircraftStrikeSystem;
    private Rules rules;
    private Pathfinding pathfinding;
    
    // View components
    private GameCanvas gameCanvas;
    private HUD hud;
    private InputController inputController;
    
    // Game loop
    private AnimationTimer gameLoop;
    private boolean running = false;
    private boolean paused = false;
    
    public Game(Stage stage, String levelName, String difficulty) {
        this.stage = stage;
        this.config = GameConfig.load(levelName, difficulty);
        this.gridMap = new GridMap(config);
        
        initializeSystems();
        initializeView();
        initializeGameLoop();
    }
    
    private void initializeSystems() {
        // Initialize systems with proper dependencies
        combatSystem = new CombatSystem(config);
        economyManager = new EconomyManager(config);
        waveManager = new WaveManager(config, gridMap, combatSystem.getEnemies());
        waveManager.setCombatSystem(combatSystem);
        aircraftStrikeSystem = new AircraftStrikeSystem(config, gridMap, combatSystem);
        rules = new Rules(config, waveManager, combatSystem);
        pathfinding = new Pathfinding(config, gridMap);
    }
    
    private void initializeView() {
        gameCanvas = new GameCanvas(config);
        hud = new HUD(config);
        inputController = new InputController(this, gameCanvas);
        
        BorderPane root = new BorderPane();
        
        // Style the main game area
        root.setStyle("""
            -fx-background: linear-gradient(to bottom right, #0c0c0c, #1a1a2e, #16213e);
            -fx-border-color: #4a90e2;
            -fx-border-width: 2;
        """);
        
        // Create game area with padding and styling
        BorderPane gameArea = new BorderPane();
        gameArea.setStyle("""
            -fx-background-color: rgba(0,0,0,0.3);
            -fx-border-color: #2c3e50;
            -fx-border-width: 2;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
        """);
        gameArea.setCenter(gameCanvas.getCanvas());
        
        root.setCenter(gameArea);
        root.setTop(hud.getRoot());
        
        // Add some padding around the game
        BorderPane.setMargin(gameArea, new javafx.geometry.Insets(10, 10, 10, 10));
        
        Scene scene = new Scene(root, 1800, 1100);
        
        // Add global styling
        scene.getStylesheets().add("data:text/css," +
            "* { -fx-font-family: 'Arial'; }" +
            ".button { -fx-font-weight: bold; }");
        
        stage.setScene(scene);
        
        // Setup input handling - make sure canvas can receive focus
        gameCanvas.getCanvas().setFocusTraversable(true);
        gameCanvas.getCanvas().setOnMouseClicked(inputController::handleMouseClick);
        
        // Mouse wheel zoom support
        gameCanvas.getCanvas().setOnScroll(event -> {
            gameCanvas.handleZoom(event.getDeltaY());
        });
        
        scene.setOnKeyPressed(inputController::handleKeyPress);
        
        // Focus the canvas so it can receive key events
        gameCanvas.getCanvas().requestFocus();
    }
    
    private void initializeGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!paused) {
                    update();
                }
                render();
            }
        };
    }
    
    /**
     * Starts the game
     */
    public void start() {
        if (!running) {
            running = true;
            paused = false;
            Time.reset();
            economyManager.start();
            waveManager.start();
            
            // Subscribe to game over events
            EventBus.getInstance().subscribe(EventBus.GameOverEvent.class, this::onGameOver);
            
            gameLoop.start();
        }
    }
    
    /**
     * Pauses the game
     */
    public void pause() {
        paused = true;
        economyManager.pause();
    }
    
    /**
     * Resumes the game
     */
    public void resume() {
        paused = false;
        economyManager.resume();
        Time.reset(); // Reset to avoid large delta time
    }
    
    /**
     * Stops the game and cleans up resources
     */
    public void stop() {
        if (running) {
            running = false;
            paused = false;
            gameLoop.stop();
            economyManager.stop();
        }
    }
    
    private void update() {
        double deltaTime = Time.update();
        
        if (!running || paused) return;
        
        // Update all systems in proper order
        waveManager.update(deltaTime);
        combatSystem.update(deltaTime);
        aircraftStrikeSystem.update(deltaTime);
        rules.update(deltaTime);
        
        // Update HUD with current game state
        hud.update(deltaTime, waveManager, rules);
        
        // Check for game over conditions
        if (rules.isGameOver()) {
            handleGameOver(rules.isVictory());
        }
    }
    
    private void render() {
        // Update canvas with current game state
        gameCanvas.setRenderData(
            gridMap,
            combatSystem.getEnemies(),
            combatSystem.getTowers(),
            combatSystem.getAADefenses(),
            combatSystem.getProjectiles(),
            combatSystem.getSpeedBumps(),
            combatSystem.getBombs(),
            aircraftStrikeSystem.getPendingStrikes()
        );
        
        gameCanvas.render();
    }
    
    private void handleGameOver(boolean victory) {
        pause();
        
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(victory ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            
            if (victory) {
                alert.setTitle("🎉 VICTORY!");
                alert.setHeaderText("🏆 Congratulations, Commander!");
                alert.setContentText("🏰 You have successfully defended the castle!\n" +
                                      "🌊 All " + waveManager.getTotalWaves() + " enemy waves defeated!\n" +
                                      "🛡️ Castle defense: " + String.format("%.1f%%", (1.0 - rules.getCurrentLeakPercentage()) * 100) + " successful!");
                
                // Victory styling
                alert.getDialogPane().setStyle("""
                    -fx-background-color: linear-gradient(to bottom, #27ae60, #2ecc71);
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-font-size: 14px;
                """);
            } else {
                alert.setTitle("💀 DEFEAT!");
                alert.setHeaderText("🚨 Castle Defense Failed!");
                alert.setContentText("💥 Too many enemies breached your defenses!\n" + 
                                      "📊 Enemy leakage: " + String.format("%.1f%%", rules.getCurrentLeakPercentage() * 100) + 
                                      " (Limit: " + String.format("%.1f%%", rules.getLeakDefeatThreshold() * 100) + ")\n" +
                                      "⚔️ Wave reached: " + waveManager.getCurrentWaveNumber() + "/" + waveManager.getTotalWaves());
                
                // Defeat styling  
                alert.getDialogPane().setStyle("""
                    -fx-background-color: linear-gradient(to bottom, #e74c3c, #c0392b);
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-font-size: 14px;
                """);
            }
            
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Custom button text
            ((javafx.scene.control.Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("🏠 Main Menu");
            ((javafx.scene.control.Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("🔄 Try Again");
            
            alert.showAndWait().ifPresent(response -> {
                stop();
                if (response == ButtonType.OK) {
                    // Go back to main menu
                    stage.close();
                } else if (response == ButtonType.CANCEL) {
                    // Restart game (close and let user restart)
                    stage.close();
                }
            });
        });
    }
    
    /**
     * Handle game over events from EventBus
     */
    private void onGameOver(EventBus.GameOverEvent event) {
        handleGameOver(event.victory);
    }

    // Getters for systems (used by controllers)
    public GameConfig getConfig() { return config; }
    public GridMap getGridMap() { return gridMap; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public CombatSystem getCombatSystem() { return combatSystem; }
    public WaveManager getWaveManager() { return waveManager; }
    public Rules getRules() { return rules; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
}