package com.tdgame.view;

import com.tdgame.config.GameConfig;
import com.tdgame.core.EventBus;
import com.tdgame.model.systems.*;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;

/**
 * Game HUD displaying money, waves, castle HP, timer and other game statistics.
 * Updates in response to game events.
 */
public class HUD {
    
    private HBox root;
    private final GameConfig config;
    
    // UI Components
    private Label moneyLabel;
    private Label wavesLabel;
    private Label timerLabel;
    private Label leakLabel;
    private ProgressBar leakProgressBar;
    
    // Game data
    private int currentMoney = 0;
    private int currentWave = 0;
    private int totalWaves = 0;
    private double gameTime = 0.0;
    private double currentLeakPercentage = 0.0;
    private double maxLeakPercentage = 0.1; // 10%
    
    public HUD(GameConfig config) {
        this.config = config;
        this.currentMoney = config.getStartingMoney();
        this.maxLeakPercentage = config.getLeakDefeatThreshold();
        
        initializeUI();
        setupEventListeners();
    }
    
    /**
     * Initialize the HUD UI components
     */
    private void initializeUI() {
        root = new HBox(30);
        root.setPadding(new Insets(15));
        root.setStyle("""
            -fx-background: linear-gradient(to right, rgba(26, 26, 46, 0.95), rgba(22, 33, 62, 0.95));
            -fx-border-color: #4a90e2;
            -fx-border-width: 0 0 3 0;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 2);
        """);
        
        // Money display
        VBox moneyBox = createInfoBox("💰 Money", "$" + currentMoney, Color.GOLD, "#f39c12");
        moneyLabel = getValueLabel(moneyBox);
        
        // Waves display  
        VBox wavesBox = createInfoBox("🌊 Wave", currentWave + "/" + totalWaves, Color.LIGHTBLUE, "#3498db");
        wavesLabel = getValueLabel(wavesBox);
        
        // Timer display
        VBox timerBox = createInfoBox("⏱️ Time", "0:00", Color.WHITE, "#95a5a6");
        timerLabel = getValueLabel(timerBox);
        
        // Income display
        VBox incomeBox = createInfoBox("📈 Income", "+" + config.getMoneyIncomePerSec() + "/sec", Color.LIGHTGREEN, "#27ae60");
        
        // Leak meter with progress bar
        VBox leakBox = createLeakMeter();
        
        root.getChildren().addAll(moneyBox, wavesBox, timerBox, incomeBox, leakBox);
    }
    
    /**
     * Setup event listeners for game events
     */
    private void setupEventListeners() {
        EventBus eventBus = EventBus.getInstance();
        
        // Listen for money changes
        eventBus.subscribe(EventBus.MoneyChangedEvent.class, this::onMoneyChanged);
        
        // Listen for wave events
        eventBus.subscribe(EventBus.WaveStartedEvent.class, this::onWaveStarted);
        
        // Listen for enemy reached castle events  
        eventBus.subscribe(EventBus.EnemyReachedCastleEvent.class, this::onEnemyReachedCastle);
    }
    
    /**
     * Handle money changed events
     */
    private void onMoneyChanged(EventBus.MoneyChangedEvent event) {
        currentMoney = event.newAmount;
        updateMoneyDisplay();
    }
    
    /**
     * Handle wave started events
     */
    private void onWaveStarted(EventBus.WaveStartedEvent event) {
        currentWave = event.waveNumber;
        updateWavesDisplay();
    }
    
    /**
     * Handle enemy reached castle events
     */
    private void onEnemyReachedCastle(EventBus.EnemyReachedCastleEvent event) {
        // This will be updated by the Rules system
    }
    
    /**
     * Update the HUD with current game state
     */
    public void update(double deltaTime, WaveManager waveManager, Rules rules) {
        // Update timer
        gameTime += deltaTime;
        updateTimerDisplay();
        
        // Update wave information
        if (waveManager != null) {
            totalWaves = waveManager.getTotalWaves();
            currentWave = waveManager.getCurrentWaveNumber();
            updateWavesDisplay();
        }
        
        // Update leak information
        if (rules != null) {
            currentLeakPercentage = rules.getCurrentLeakPercentage();
            updateLeakDisplay();
        }
    }
    
    /**
     * Update money display
     */
    private void updateMoneyDisplay() {
        moneyLabel.setText("$" + currentMoney);
    }
    
    /**
     * Update waves display
     */
    private void updateWavesDisplay() {
        wavesLabel.setText(currentWave + "/" + totalWaves);
    }
    
    /**
     * Update timer display
     */
    private void updateTimerDisplay() {
        int minutes = (int) (gameTime / 60);
        int seconds = (int) (gameTime % 60);
        timerLabel.setText(String.format("%d:%02d", minutes, seconds));
    }
    
    /**
     * Update leak display
     */
    private void updateLeakDisplay() {
        double leakPercent = currentLeakPercentage * 100;
        double maxPercent = maxLeakPercentage * 100;
        
        leakLabel.setText(String.format("%.1f%% / %.1f%%", leakPercent, maxPercent));
        leakProgressBar.setProgress(currentLeakPercentage / maxLeakPercentage);
        
        // Change color based on danger level
        if (currentLeakPercentage >= maxLeakPercentage * 0.8) {
            leakLabel.setTextFill(Color.RED);
            leakProgressBar.setStyle("-fx-accent: red;");
        } else if (currentLeakPercentage >= maxLeakPercentage * 0.5) {
            leakLabel.setTextFill(Color.ORANGE);
            leakProgressBar.setStyle("-fx-accent: orange;");
        } else {
            leakLabel.setTextFill(Color.WHITE);
            leakProgressBar.setStyle("-fx-accent: green;");
        }
    }
    
    /**
     * Reset HUD for new game
     */
    public void reset() {
        gameTime = 0.0;
        currentWave = 0;
        totalWaves = 0;
        currentLeakPercentage = 0.0;
        currentMoney = config.getStartingMoney();
        
        updateMoneyDisplay();
        updateWavesDisplay();
        updateTimerDisplay();
        updateLeakDisplay();
    }
    
    /**
     * Create a styled info box with title and value
     */
    private VBox createInfoBox(String title, String value, Color valueColor, String bgColor) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12));
        box.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-border-color: rgba(255,255,255,0.3);
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 1);
        """, bgColor));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(2, Color.BLACK));
        
        VBox valueContainer = new VBox();
        valueContainer.setAlignment(Pos.CENTER);
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        valueLabel.setTextFill(valueColor);
        valueLabel.setEffect(new DropShadow(2, Color.BLACK));
        
        valueContainer.getChildren().add(valueLabel);
        box.getChildren().addAll(titleLabel, valueContainer);
        
        return box;
    }
    
    /**
     * Get the value label from an info box
     */
    private Label getValueLabel(VBox infoBox) {
        VBox valueContainer = (VBox) infoBox.getChildren().get(1);
        return (Label) valueContainer.getChildren().get(0);
    }
    
    /**
     * Create the leak meter with progress bar
     */
    private VBox createLeakMeter() {
        VBox leakBox = new VBox(8);
        leakBox.setAlignment(Pos.CENTER);
        leakBox.setPadding(new Insets(12));
        leakBox.setStyle("""
            -fx-background-color: #e74c3c;
            -fx-background-radius: 8;
            -fx-border-color: rgba(255,255,255,0.3);
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 1);
        """);
        
        Label leakTitle = new Label("🛡️ Castle Defense");
        leakTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        leakTitle.setTextFill(Color.WHITE);
        leakTitle.setEffect(new DropShadow(2, Color.BLACK));
        
        leakLabel = new Label("0.0% / 10.0%");
        leakLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        leakLabel.setTextFill(Color.WHITE);
        leakLabel.setEffect(new DropShadow(2, Color.BLACK));
        
        leakProgressBar = new ProgressBar(0.0);
        leakProgressBar.setPrefWidth(120);
        leakProgressBar.setPrefHeight(12);
        leakProgressBar.setStyle("""
            -fx-accent: #2ecc71;
            -fx-background-color: rgba(255,255,255,0.2);
            -fx-border-radius: 6;
            -fx-background-radius: 6;
        """);
        
        leakBox.getChildren().addAll(leakTitle, leakLabel, leakProgressBar);
        return leakBox;
    }

    public Node getRoot() {
        return root;
    }
}