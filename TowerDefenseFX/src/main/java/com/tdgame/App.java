package com.tdgame;

import com.tdgame.view.MainMenu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Main JavaFX Application entry point.
 * Bootstraps the application and manages scene transitions between MainMenu and Game.
 */
public class App extends Application {
    
    private Stage primaryStage;
    private static final String TITLE = "🏰 Tower Defense FX - Elite Edition";
    private static final int WINDOW_WIDTH = 1800;
    private static final int WINDOW_HEIGHT = 1100;
    
    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        
        primaryStage.setTitle(TITLE);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(WINDOW_WIDTH);
        primaryStage.setMinHeight(WINDOW_HEIGHT);
        
        // Set application icon (if available)
        try {
            primaryStage.getIcons().add(new javafx.scene.image.Image("/icons/castle.png"));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        showMainMenu();
    }
    
    /**
     * Shows the main menu scene
     */
    public void showMainMenu() {
        MainMenu mainMenu = new MainMenu(this);
        Scene scene = new Scene((Parent)mainMenu.getRoot(), WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Dark theme CSS
        scene.getStylesheets().add("data:text/css," + 
            "* { -fx-font-family: 'Arial'; }" +
            ".root { -fx-background-color: linear-gradient(to bottom, #0c0c0c, #1a1a2e); }");
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Transitions to the game scene with the specified level and difficulty
     */
    public void startGame(String levelName, String difficulty) {
        try {
            System.out.println("Starting game with level: " + levelName + ", difficulty: " + difficulty);
            com.tdgame.core.Game game = new com.tdgame.core.Game(primaryStage, levelName, difficulty);
            game.start();
            System.out.println("Game started successfully!");
        } catch (Exception e) {
            System.err.println("ERROR starting game: " + e.getMessage());
            e.printStackTrace();
            
            // Show error dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to start game");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}