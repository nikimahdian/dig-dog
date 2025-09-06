package com.tdgame.controller;

import com.tdgame.core.Game;
import com.tdgame.view.GameCanvas;
import com.tdgame.model.grid.BuildSlot;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;

/**
 * Handles user input for the game including mouse clicks and keyboard shortcuts.
 * Manages build menu popup and construction commands.
 */
public class InputController {
    
    private final Game game;
    private final GameCanvas gameCanvas;
    private final BuildMenuController buildMenuController;
    
    private Popup buildMenuPopup;
    private BuildSlot selectedSlot;
    
    public InputController(Game game, GameCanvas gameCanvas) {
        this.game = game;
        this.gameCanvas = gameCanvas;
        this.buildMenuController = new BuildMenuController(
            game.getConfig(), 
            game.getEconomyManager(), 
            game.getCombatSystem()
        );
        
        initializeBuildMenu();
    }
    
    /**
     * Initialize the build menu popup
     */
    private void initializeBuildMenu() {
        buildMenuPopup = new Popup();
        buildMenuPopup.setAutoHide(true);
        
        VBox menuBox = new VBox(5);
        menuBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); " +
                        "-fx-border-color: white; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 10;");
        
        // Build option buttons
        for (BuildMenuController.BuildOption option : BuildMenuController.BuildOption.values()) {
            Button button = createBuildButton(option);
            menuBox.getChildren().add(button);
        }
        
        // Cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> hideBuildMenu());
        cancelButton.setStyle("-fx-text-fill: white; -fx-background-color: #666;");
        menuBox.getChildren().add(cancelButton);
        
        buildMenuPopup.getContent().add(menuBox);
    }
    
    /**
     * Create a build button for a specific option
     */
    private Button createBuildButton(BuildMenuController.BuildOption option) {
        int cost = buildMenuController.getBuildCost(option);
        String text = option.name().replace("_", " ") + " ($" + cost + ")";
        
        Button button = new Button(text);
        button.setStyle("-fx-text-fill: white; -fx-background-color: #333;");
        button.setMaxWidth(200);
        
        // Update button state based on affordability
        updateButtonState(button, option);
        
        button.setOnAction(e -> {
            if (selectedSlot != null && buildMenuController.tryBuild(option, selectedSlot)) {
                hideBuildMenu();
            }
        });
        
        return button;
    }
    
    /**
     * Update button state based on whether player can afford it
     */
    private void updateButtonState(Button button, BuildMenuController.BuildOption option) {
        if (buildMenuController.canAfford(option)) {
            button.setDisable(false);
            button.setStyle("-fx-text-fill: white; -fx-background-color: #2a5d2a;");
        } else {
            button.setDisable(true);
            button.setStyle("-fx-text-fill: gray; -fx-background-color: #666;");
        }
    }
    
    public void handleMouseClick(MouseEvent event) {
        // Convert screen coordinates to world coordinates (accounting for zoom)
        double[] worldCoords = gameCanvas.screenToWorld(event.getX(), event.getY());
        double x = worldCoords[0];
        double y = worldCoords[1];
        
        // Check for build slot clicks
        BuildSlot buildSlot = game.getGridMap().findBuildSlotAt(x, y, 30);
        if (buildSlot != null && !buildSlot.isOccupied()) {
            showBuildMenu(buildSlot, event.getScreenX(), event.getScreenY());
            return;
        }
        
        // Check for speed bump slot clicks
        BuildSlot speedBumpSlot = game.getGridMap().findSpeedBumpSlotAt(x, y, 30);
        if (speedBumpSlot != null) {
            // Directly place speed bump
            buildMenuController.tryBuild(BuildMenuController.BuildOption.SPEED_BUMP, speedBumpSlot);
            return;
        }
        
        // Check for bomb slot clicks
        BuildSlot bombSlot = game.getGridMap().findBombSlotAt(x, y, 30);
        if (bombSlot != null) {
            // Directly place bomb
            buildMenuController.tryBuild(BuildMenuController.BuildOption.BOMB, bombSlot);
            return;
        }
        
        // Close build menu if clicking elsewhere
        hideBuildMenu();
    }
    
    /**
     * Show build menu at the specified position
     */
    private void showBuildMenu(BuildSlot slot, double screenX, double screenY) {
        selectedSlot = slot;
        
        // Update button states before showing
        buildMenuPopup.getContent().clear();
        VBox menuBox = new VBox(5);
        menuBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); " +
                        "-fx-border-color: white; " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 10;");
        
        // Only show tower and AA options for build slots
        BuildMenuController.BuildOption[] towerOptions = {
            BuildMenuController.BuildOption.FAST_TOWER,
            BuildMenuController.BuildOption.POWER_TOWER,
            BuildMenuController.BuildOption.AA_60,
            BuildMenuController.BuildOption.AA_80
        };
        
        for (BuildMenuController.BuildOption option : towerOptions) {
            Button button = createBuildButton(option);
            menuBox.getChildren().add(button);
        }
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> hideBuildMenu());
        cancelButton.setStyle("-fx-text-fill: white; -fx-background-color: #666;");
        menuBox.getChildren().add(cancelButton);
        
        buildMenuPopup.getContent().add(menuBox);
        
        buildMenuPopup.show(gameCanvas.getCanvas(), screenX, screenY);
    }
    
    /**
     * Hide the build menu
     */
    private void hideBuildMenu() {
        buildMenuPopup.hide();
        selectedSlot = null;
    }
    
    public void handleKeyPress(KeyEvent event) {
        KeyCode key = event.getCode();
        
        switch (key) {
            case ESCAPE -> {
                if (game.isPaused()) {
                    game.resume();
                } else {
                    game.pause();
                }
            }
            case SPACE -> {
                // Toggle pause
                if (game.isPaused()) {
                    game.resume();
                } else {
                    game.pause();
                }
            }
            case DIGIT1 -> {
                // Quick build fast tower (if slot selected)
                if (selectedSlot != null) {
                    buildMenuController.tryBuild(BuildMenuController.BuildOption.FAST_TOWER, selectedSlot);
                    hideBuildMenu();
                }
            }
            case DIGIT2 -> {
                // Quick build power tower
                if (selectedSlot != null) {
                    buildMenuController.tryBuild(BuildMenuController.BuildOption.POWER_TOWER, selectedSlot);
                    hideBuildMenu();
                }
            }
            case DIGIT3 -> {
                // Quick build AA 60
                if (selectedSlot != null) {
                    buildMenuController.tryBuild(BuildMenuController.BuildOption.AA_60, selectedSlot);
                    hideBuildMenu();
                }
            }
            case DIGIT4 -> {
                // Quick build AA 80
                if (selectedSlot != null) {
                    buildMenuController.tryBuild(BuildMenuController.BuildOption.AA_80, selectedSlot);
                    hideBuildMenu();
                }
            }
            case PLUS, EQUALS -> {
                // Zoom in
                gameCanvas.zoomIn();
            }
            case MINUS -> {
                // Zoom out
                gameCanvas.zoomOut();
            }
            case R -> {
                // Reset camera (zoom + pan)
                gameCanvas.resetCamera();
            }
            case LEFT, A -> {
                // Pan left
                gameCanvas.panLeft();
            }
            case RIGHT, D -> {
                // Pan right
                gameCanvas.panRight();
            }
            case UP, W -> {
                // Pan up
                gameCanvas.panUp();
            }
            case DOWN, S -> {
                // Pan down
                gameCanvas.panDown();
            }
        }
    }
}