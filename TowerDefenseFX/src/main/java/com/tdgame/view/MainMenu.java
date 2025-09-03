package com.tdgame.view;

import com.tdgame.App;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

public class MainMenu {
    private final VBox root;
    private final App app;
    
    public MainMenu(App app) {
        this.app = app;
        this.root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        
        Button startEasy = new Button("Start Easy");
        startEasy.setOnAction(e -> app.startGame("level1", "easy"));
        
        Button startNormal = new Button("Start Normal");
        startNormal.setOnAction(e -> app.startGame("level1", "normal"));
        
        Button startHard = new Button("Start Hard");
        startHard.setOnAction(e -> app.startGame("level1", "hard"));
        
        root.getChildren().addAll(startEasy, startNormal, startHard);
    }
    
    public Node getRoot() {
        return root;
    }
}