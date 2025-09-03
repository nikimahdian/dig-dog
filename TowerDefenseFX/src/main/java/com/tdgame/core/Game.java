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
        aircraftStrikeSystem = new AircraftStrikeSystem(config, gridMap, combatSystem);
        rules = new Rules(config, waveManager, combatSystem);
        pathfinding = new Pathfinding(config, gridMap);
    }
    
    private void initializeView() {
        gameCanvas = new GameCanvas(config);
        hud = new HUD(config);
        inputController = new InputController(this, gameCanvas);
        
        BorderPane root = new BorderPane();
        root.setCenter(gameCanvas.getCanvas());
        root.setTop(hud.getRoot());
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        
        // Setup input handling - make sure canvas can receive focus
        gameCanvas.getCanvas().setFocusTraversable(true);
        gameCanvas.getCanvas().setOnMouseClicked(inputController::handleMouseClick);
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
        
        String title = victory ? "Victory!" : "Defeat!";
        String message = victory ? 
            "Congratulations! You successfully defended against all enemy waves!" :
            "Game Over! Too many enemies reached the castle.";
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.OK);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Return to main menu
                stop();
                stage.close();
                // In a full implementation, you'd return to the main menu here
            }
        });
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