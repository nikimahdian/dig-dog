package game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        App app = new App();
        Scene scene = new Scene(app.getRoot(), 704, 576); // 11*64, 9*64
        
        primaryStage.setTitle("Tower Defense");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        app.start();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}