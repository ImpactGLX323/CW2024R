package com.example.demo.view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class EndGame {
    private static EndGame singleInstance = null;
    private EndGame() {}

    public static EndGame getInstance() {
        if (singleInstance == null) singleInstance = new EndGame();
        return singleInstance;
    }

    // Pass in actions so EndGame stays decoupled from GameScene/Main
    public void endGameShow(
        Scene endGameScene, Group root, Stage primaryStage, long score,
        Runnable onRestart, Runnable onMenu, Runnable onQuit
    ) {
        // Clear any existing children
        root.getChildren().clear();
        
        // Set dark background
        endGameScene.setFill(Color.web("#2d2d2d"));
        
        // Game Over text (top)
        Text gameOverText = new Text("GAME OVER");
        gameOverText.setFont(Font.font("Arial", 80));
        gameOverText.setFill(Color.WHITE);
        
        // Center the text horizontally
        gameOverText.setX((endGameScene.getWidth() - gameOverText.getLayoutBounds().getWidth()) / 2);
        gameOverText.setY(150);
        root.getChildren().add(gameOverText);
        
        // Score text (middle)
        Text scoreText = new Text("SCORE: " + score);
        scoreText.setFont(Font.font("Arial", 60));
        scoreText.setFill(Color.WHITE);
        
        // Center the score text
        scoreText.setX((endGameScene.getWidth() - scoreText.getLayoutBounds().getWidth()) / 2);
        scoreText.setY(300);
        root.getChildren().add(scoreText);
        
        // Button configuration
        double buttonWidth = 300;
        double buttonHeight = 60;
        double spacing = 20;
        double centerX = (endGameScene.getWidth() - buttonWidth) / 2;
        double startY = 400; // Position below the score
        
        // Restart Button
        Button restartButton = new Button("RESTART");
        restartButton.setPrefSize(buttonWidth, buttonHeight);
        restartButton.setStyle("-fx-text-fill: white; -fx-background-color: #8f7a66; -fx-font-size: 20;");
        restartButton.relocate(centerX, startY);
        root.getChildren().add(restartButton);
        
        // Menu Button
        Button menuButton = new Button("MAIN MENU");
        menuButton.setPrefSize(buttonWidth, buttonHeight);
        menuButton.setStyle("-fx-text-fill: white; -fx-background-color: #8f7a66; -fx-font-size: 20;");
        menuButton.relocate(centerX, startY + buttonHeight + spacing);
        root.getChildren().add(menuButton);
        
        // Quit Button
        Button quitButton = new Button("QUIT");
        quitButton.setPrefSize(buttonWidth, buttonHeight);
        quitButton.setStyle("-fx-text-fill: white; -fx-background-color: #8f7a66; -fx-font-size: 20;");
        quitButton.relocate(centerX, startY + 2 * (buttonHeight + spacing));
        root.getChildren().add(quitButton);
        
        // Button actions (keep existing functionality)
        restartButton.setOnAction(e -> {
            root.getChildren().clear();
            if (onRestart != null) onRestart.run();
        });
        
        menuButton.setOnAction(e -> {
            root.getChildren().clear();
            if (onMenu != null) onMenu.run();
        });
        
        quitButton.setOnAction(e -> {
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Quit Dialog");
            alert.setHeaderText("Quit from this page");
            alert.setContentText("Are you sure?");
            var result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (onQuit != null) onQuit.run();
            }
        });
    }
}