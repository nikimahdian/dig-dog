package com.tdgame.view;

import com.tdgame.App;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;

public class MainMenu {
    private final VBox root;
    private final App app;
    
    public MainMenu(App app) {
        this.app = app;
        this.root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        
        // Background gradient
        root.setStyle("""
            -fx-background: linear-gradient(to bottom, #1a1a2e, #16213e, #0f3460);
            -fx-border-color: #4a90e2;
            -fx-border-width: 2;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
        """);
        
        // Title
        Label title = new Label("🏰 TOWER DEFENSE FX 🏰");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.GOLD);
        title.setEffect(new DropShadow(10, Color.BLACK));
        
        // Subtitle
        Label subtitle = new Label("Choose Your Battlefield!");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.LIGHTBLUE);
        
        // Level selection
        createLevelSelection();
        
        // Instructions
        Label instructions = new Label("💡 Tips: Fast Tower for soldiers, Tank Tower for tanks, AA Defense for aircraft!");
        instructions.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        instructions.setTextFill(Color.LIGHTGRAY);
        instructions.setWrapText(true);
        instructions.setMaxWidth(500);
        instructions.setStyle("-fx-text-alignment: center;");
        
        root.getChildren().addAll(title, subtitle, instructions);
    }
    
    private void createLevelSelection() {
        // Level selection label
        Label levelLabel = new Label("🗺️ SELECT MAP");
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        levelLabel.setTextFill(Color.CYAN);
        levelLabel.setEffect(new DropShadow(5, Color.BLACK));
        
        // Level buttons
        HBox levelBox = new HBox(20);
        levelBox.setAlignment(Pos.CENTER);
        
        Button level1Btn = createLevelButton("🏰 Classic Arena", "Standard 14x10 battlefield", "#3498db", "level1");
        Button level2Btn = createLevelButton("🌊 Desert Storm", "Large 16x12 desert map", "#e67e22", "level2"); 
        Button level3Btn = createLevelButton("🗻 Mountain Pass", "Narrow 12x14 corridor", "#8e44ad", "level3");
        
        levelBox.getChildren().addAll(level1Btn, level2Btn, level3Btn);
        
        root.getChildren().addAll(levelLabel, levelBox);
        
        // Difficulty selection label
        Label difficultyLabel = new Label("⚔️ SELECT DIFFICULTY");
        difficultyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        difficultyLabel.setTextFill(Color.ORANGE);
        difficultyLabel.setEffect(new DropShadow(5, Color.BLACK));
        
        // Difficulty buttons
        VBox difficultyBox = new VBox(15);
        difficultyBox.setAlignment(Pos.CENTER);
        
        Button startEasy = createStyledButton("🟢 EASY MODE", "Beginner friendly", "#2ecc71", e -> app.startGame(selectedLevel, "easy"));
        Button startNormal = createStyledButton("🟡 NORMAL MODE", "Balanced challenge", "#f39c12", e -> app.startGame(selectedLevel, "normal"));
        Button startHard = createStyledButton("🔴 HARD MODE", "Expert difficulty", "#e74c3c", e -> app.startGame(selectedLevel, "hard"));
        
        difficultyBox.getChildren().addAll(startEasy, startNormal, startHard);
        
        root.getChildren().addAll(difficultyLabel, difficultyBox);
    }
    
    private String selectedLevel = "level1";
    
    private Button createLevelButton(String name, String description, String color, String level) {
        VBox buttonContent = new VBox(5);
        buttonContent.setAlignment(Pos.CENTER);
        
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);
        
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        descLabel.setTextFill(Color.LIGHTGRAY);
        
        buttonContent.getChildren().addAll(nameLabel, descLabel);
        
        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setPrefSize(180, 60);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: white;
            -fx-border-width: 1;
        """, color));
        
        button.setOnAction(e -> {
            selectedLevel = level;
            // Update button states
            updateLevelButtonStates(button);
        });
        
        // Default level1 selected
        if (level.equals("level1")) {
            button.setStyle(button.getStyle() + "-fx-border-width: 3; -fx-border-color: gold;");
        }
        
        return button;
    }
    
    private void updateLevelButtonStates(Button selectedButton) {
        // Reset all level buttons and highlight selected
        // This is simplified - in full implementation would track all buttons
    }
    
    private Button createStyledButton(String text, String subtitle, String color, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        VBox buttonContent = new VBox(5);
        buttonContent.setAlignment(Pos.CENTER);
        
        Label mainText = new Label(text);
        mainText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        mainText.setTextFill(Color.WHITE);
        
        Label subText = new Label(subtitle);
        subText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        subText.setTextFill(Color.LIGHTGRAY);
        
        buttonContent.getChildren().addAll(mainText, subText);
        
        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setPrefSize(300, 80);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: white;
            -fx-border-width: 2;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);
        """, color));
        
        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setEffect(new Glow(0.3));
            button.setStyle(button.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });
        
        button.setOnMouseExited(e -> {
            button.setEffect(null);
            button.setStyle(button.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", ""));
        });
        
        button.setOnAction(action);
        return button;
    }
    
    public Node getRoot() {
        return root;
    }
}