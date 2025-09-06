package game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import game.entity.*;
import game.core.ResourceManager;
import game.core.GameLoop;
import game.Config;

public class BuildMenu extends VBox {
    
    private final ResourceManager resourceManager;
    private final GameLoop gameLoop;
    private Point2D buildLocation;
    
    private final Button fastTowerButton;
    private final Button heavyTowerButton;
    private final Button cheapAAButton;
    private final Button expensiveAAButton;
    private final Button speedBumpButton;
    private final Button bombButton;
    private final Button closeButton;
    
    public BuildMenu(ResourceManager resourceManager, GameLoop gameLoop) {
        this.resourceManager = resourceManager;
        this.gameLoop = gameLoop;
        
        // Initialize buttons
        this.fastTowerButton = new Button("Fast Tower\n$" + Config.FAST_TOWER_COST);
        this.heavyTowerButton = new Button("Heavy Tower\n$" + Config.HEAVY_TOWER_COST);
        this.cheapAAButton = new Button("AA 60%\n$" + Config.AA_CHEAP_COST);
        this.expensiveAAButton = new Button("AA 80%\n$" + Config.AA_EXPENSIVE_COST);
        this.speedBumpButton = new Button("Speed Bump\n$" + Config.SPEED_BUMP_COST);
        this.bombButton = new Button("Bomb\n$" + Config.BOMB_COST);
        this.closeButton = new Button("Close");
        
        setupLayout();
        setupEventHandlers();
        setVisible(false);
    }
    
    private void setupLayout() {
        setSpacing(5);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: rgba(50, 50, 50, 0.9); -fx-border-color: white; -fx-border-width: 2;");
        
        Font buttonFont = Font.font("Arial", FontWeight.NORMAL, 12);
        
        // Style all buttons
        Button[] buttons = {fastTowerButton, heavyTowerButton, cheapAAButton, 
                           expensiveAAButton, speedBumpButton, bombButton, closeButton};
        
        for (Button button : buttons) {
            button.setFont(buttonFont);
            button.setPrefWidth(120);
            button.setPrefHeight(40);
        }
        
        // Different colors for different types
        fastTowerButton.setStyle("-fx-background-color: lightblue;");
        heavyTowerButton.setStyle("-fx-background-color: darkgray;");
        cheapAAButton.setStyle("-fx-background-color: lightcoral;");
        expensiveAAButton.setStyle("-fx-background-color: darkred;");
        speedBumpButton.setStyle("-fx-background-color: yellow;");
        bombButton.setStyle("-fx-background-color: orange;");
        closeButton.setStyle("-fx-background-color: lightgray;");
        
        getChildren().addAll(fastTowerButton, heavyTowerButton, cheapAAButton, 
                            expensiveAAButton, speedBumpButton, bombButton, closeButton);
    }
    
    private void setupEventHandlers() {
        fastTowerButton.setOnAction(e -> buildFastTower());
        heavyTowerButton.setOnAction(e -> buildHeavyTower());
        cheapAAButton.setOnAction(e -> buildCheapAA());
        expensiveAAButton.setOnAction(e -> buildExpensiveAA());
        speedBumpButton.setOnAction(e -> buildSpeedBump());
        bombButton.setOnAction(e -> buildBomb());
        closeButton.setOnAction(e -> hide());
    }
    
    public void show(Point2D location, boolean isBuildSlot, boolean isSpeedBumpSlot, boolean isBombSlot) {
        this.buildLocation = location;
        
        // Show/hide appropriate buttons based on slot type
        fastTowerButton.setVisible(isBuildSlot);
        heavyTowerButton.setVisible(isBuildSlot);
        cheapAAButton.setVisible(isBuildSlot);
        expensiveAAButton.setVisible(isBuildSlot);
        
        speedBumpButton.setVisible(isSpeedBumpSlot);
        bombButton.setVisible(isBombSlot);
        
        updateButtonStates();
        
        // Position menu
        setLayoutX(location.getX());
        setLayoutY(location.getY());
        setVisible(true);
        
        // Ensure menu doesn't go off-screen
        double maxX = getScene().getWidth() - getWidth();
        double maxY = getScene().getHeight() - getHeight();
        
        if (getLayoutX() > maxX) setLayoutX(maxX);
        if (getLayoutY() > maxY) setLayoutY(maxY);
    }
    
    public void hide() {
        setVisible(false);
    }
    
    private void updateButtonStates() {
        int money = resourceManager.getMoney();
        
        fastTowerButton.setDisable(!resourceManager.canAfford(Config.FAST_TOWER_COST));
        heavyTowerButton.setDisable(!resourceManager.canAfford(Config.HEAVY_TOWER_COST));
        cheapAAButton.setDisable(!resourceManager.canAfford(Config.AA_CHEAP_COST));
        expensiveAAButton.setDisable(!resourceManager.canAfford(Config.AA_EXPENSIVE_COST));
        speedBumpButton.setDisable(!resourceManager.canAfford(Config.SPEED_BUMP_COST));
        bombButton.setDisable(!resourceManager.canAfford(Config.BOMB_COST));
    }
    
    private void buildFastTower() {
        if (resourceManager.spendMoney(Config.FAST_TOWER_COST)) {
            FastTower tower = new FastTower(buildLocation.getX(), buildLocation.getY());
            gameLoop.addTower(tower);
            hide();
        }
    }
    
    private void buildHeavyTower() {
        if (resourceManager.spendMoney(Config.HEAVY_TOWER_COST)) {
            HeavyTower tower = new HeavyTower(buildLocation.getX(), buildLocation.getY());
            gameLoop.addTower(tower);
            hide();
        }
    }
    
    private void buildCheapAA() {
        if (resourceManager.spendMoney(Config.AA_CHEAP_COST)) {
            AA60 aa = new AA60();
            gameLoop.addAntiAir(aa);
            hide();
        }
    }
    
    private void buildExpensiveAA() {
        if (resourceManager.spendMoney(Config.AA_EXPENSIVE_COST)) {
            AA80 aa = new AA80();
            gameLoop.addAntiAir(aa);
            hide();
        }
    }
    
    private void buildSpeedBump() {
        if (resourceManager.spendMoney(Config.SPEED_BUMP_COST)) {
            Point2D tileCenter = new Point2D(
                ((int)(buildLocation.getX() / Config.TILE_SIZE)) * Config.TILE_SIZE,
                ((int)(buildLocation.getY() / Config.TILE_SIZE)) * Config.TILE_SIZE
            );
            gameLoop.addSpeedBump(tileCenter);
            hide();
        }
    }
    
    private void buildBomb() {
        if (resourceManager.spendMoney(Config.BOMB_COST)) {
            Point2D tileCenter = new Point2D(
                ((int)(buildLocation.getX() / Config.TILE_SIZE)) * Config.TILE_SIZE,
                ((int)(buildLocation.getY() / Config.TILE_SIZE)) * Config.TILE_SIZE
            );
            gameLoop.addBomb(tileCenter);
            hide();
        }
    }
}