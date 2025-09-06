package game.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class Toast extends StackPane {
    
    private final Label messageLabel;
    
    public Toast() {
        this.messageLabel = new Label();
        
        setupLayout();
        setVisible(false);
    }
    
    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10, 15, 10, 15));
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); " +
                "-fx-background-radius: 10; " +
                "-fx-border-radius: 10;");
        
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        messageLabel.setTextFill(Color.WHITE);
        
        getChildren().add(messageLabel);
    }
    
    public void show(String message, Duration duration) {
        messageLabel.setText(message);
        
        // Position in center of parent
        if (getParent() != null) {
            setLayoutX((getParent().getBoundsInLocal().getWidth() - getWidth()) / 2);
            setLayoutY((getParent().getBoundsInLocal().getHeight() - getHeight()) / 2);
        }
        
        setVisible(true);
        setOpacity(1.0);
        
        // Fade out after duration
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), this);
        fadeOut.setDelay(duration);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> setVisible(false));
        fadeOut.play();
    }
    
    public void showInfo(String message) {
        messageLabel.setTextFill(Color.LIGHTBLUE);
        show(message, Duration.seconds(2));
    }
    
    public void showSuccess(String message) {
        messageLabel.setTextFill(Color.LIGHTGREEN);
        show(message, Duration.seconds(2));
    }
    
    public void showWarning(String message) {
        messageLabel.setTextFill(Color.ORANGE);
        show(message, Duration.seconds(3));
    }
    
    public void showError(String message) {
        messageLabel.setTextFill(Color.LIGHTCORAL);
        show(message, Duration.seconds(4));
    }
}