package game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class HudView {
    
    private HBox root;
    private Label moneyLabel;
    private Label waveLabel;
    private Label leakageLabel;
    
    public HudView() {
        initializeUI();
    }
    
    private void initializeUI() {
        root = new HBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        
        moneyLabel = new Label("Money: $100");
        moneyLabel.setTextFill(Color.WHITE);
        moneyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        waveLabel = new Label("Wave: 1/6");
        waveLabel.setTextFill(Color.WHITE);
        waveLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        leakageLabel = new Label("Leaked: 0.0%");
        leakageLabel.setTextFill(Color.GREEN);
        leakageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        root.getChildren().addAll(moneyLabel, waveLabel, leakageLabel);
    }
    
    public HBox getRoot() {
        return root;
    }
    
    public void updateMoney(int money) {
        moneyLabel.setText("Money: $" + money);
    }
    
    public void updateWave(int currentWave, int totalWaves) {
        waveLabel.setText("Wave: " + currentWave + "/" + totalWaves);
    }
    
    public void updateLeakage(double leakPercentage) {
        leakageLabel.setText(String.format("Leaked: %.1f%%", leakPercentage * 100));
        
        // Change color based on danger level
        if (leakPercentage < 0.05) {
            leakageLabel.setTextFill(Color.GREEN);
        } else if (leakPercentage < 0.08) {
            leakageLabel.setTextFill(Color.YELLOW);
        } else {
            leakageLabel.setTextFill(Color.RED);
        }
    }
}