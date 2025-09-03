package com.tdgame.view;

import com.tdgame.config.GameConfig;
import com.tdgame.core.EventBus;
import com.tdgame.model.systems.*;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.control.ProgressBar;

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
        root = new HBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: white;");
        
        // Money display
        VBox moneyBox = new VBox(5);
        Label moneyTitle = new Label("Money");
        moneyTitle.setFont(Font.font(12));
        moneyTitle.setTextFill(Color.WHITE);
        
        moneyLabel = new Label("$" + currentMoney);
        moneyLabel.setFont(Font.font(16));
        moneyLabel.setTextFill(Color.GOLD);
        
        moneyBox.getChildren().addAll(moneyTitle, moneyLabel);
        
        // Waves display
        VBox wavesBox = new VBox(5);
        Label wavesTitle = new Label("Waves");
        wavesTitle.setFont(Font.font(12));
        wavesTitle.setTextFill(Color.WHITE);
        
        wavesLabel = new Label(currentWave + "/" + totalWaves);
        wavesLabel.setFont(Font.font(16));
        wavesLabel.setTextFill(Color.LIGHTBLUE);
        
        wavesBox.getChildren().addAll(wavesTitle, wavesLabel);
        
        // Timer display
        VBox timerBox = new VBox(5);
        Label timerTitle = new Label("Time");
        timerTitle.setFont(Font.font(12));
        timerTitle.setTextFill(Color.WHITE);
        
        timerLabel = new Label("0:00");
        timerLabel.setFont(Font.font(16));
        timerLabel.setTextFill(Color.WHITE);
        
        timerBox.getChildren().addAll(timerTitle, timerLabel);
        
        // Leak meter
        VBox leakBox = new VBox(5);
        Label leakTitle = new Label("Enemy Leakage");
        leakTitle.setFont(Font.font(12));
        leakTitle.setTextFill(Color.WHITE);
        
        leakLabel = new Label("0.0% / 10.0%");
        leakLabel.setFont(Font.font(14));
        leakLabel.setTextFill(Color.WHITE);
        
        leakProgressBar = new ProgressBar(0.0);
        leakProgressBar.setPrefWidth(150);
        leakProgressBar.setStyle("-fx-accent: red;");
        
        leakBox.getChildren().addAll(leakTitle, leakLabel, leakProgressBar);
        
        // Income display
        VBox incomeBox = new VBox(5);
        Label incomeTitle = new Label("Income");
        incomeTitle.setFont(Font.font(12));
        incomeTitle.setTextFill(Color.WHITE);
        
        Label incomeLabel = new Label("+" + config.getMoneyIncomePerSec() + "/sec");
        incomeLabel.setFont(Font.font(14));
        incomeLabel.setTextFill(Color.LIGHTGREEN);
        
        incomeBox.getChildren().addAll(incomeTitle, incomeLabel);
        
        root.getChildren().addAll(moneyBox, wavesBox, timerBox, leakBox, incomeBox);
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
    
    public Node getRoot() {
        return root;
    }
}