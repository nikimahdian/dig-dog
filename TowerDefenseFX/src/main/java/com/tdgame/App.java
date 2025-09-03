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
    private static final String TITLE = "Tower Defense FX";
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    
    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        
        primaryStage.setTitle(TITLE);
        primaryStage.setResizable(false);
        
        showMainMenu();
    }
    
    /**
     * Shows the main menu scene
     */
    public void showMainMenu() {
        MainMenu mainMenu = new MainMenu(this);
        Scene scene = new Scene((Parent)mainMenu.getRoot(), WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Transitions to the game scene with the specified level and difficulty
     */
    public void startGame(String levelName, String difficulty) {
        com.tdgame.core.Game game = new com.tdgame.core.Game(primaryStage, levelName, difficulty);
        game.start();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}