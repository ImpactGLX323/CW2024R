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
    Text text = new Text("GAME OVER");
    text.relocate(250, 250);
    text.setFont(Font.font(80));
    root.getChildren().add(text);

    Text scoreText = new Text(String.valueOf(score));
    scoreText.setFill(Color.BLACK);
    scoreText.relocate(250, 600);
    scoreText.setFont(Font.font(80));
    root.getChildren().add(scoreText);

    double buttonWidth = 200;
    double buttonHeight = 80;
    double spacing = 30;
    double centerX = 350;
    double startY = 750;

    Button restartButton = new Button("RESTART");
    restartButton.setPrefSize(buttonWidth, buttonHeight);
    restartButton.setStyle("-fx-text-fill: white; -fx-background-color: #8f7a66;");
    restartButton.relocate(centerX, startY);
    root.getChildren().add(restartButton);

    Button menuButton = new Button("MAIN MENU");
    menuButton.setPrefSize(buttonWidth, buttonHeight);
    menuButton.setStyle("-fx-text-fill: white; -fx-background-color: #8f7a66;");
    menuButton.relocate(centerX, startY + buttonHeight + spacing);
    root.getChildren().add(menuButton);

    Button quitButton = new Button("QUIT");
    quitButton.setPrefSize(buttonWidth, buttonHeight);
    quitButton.setStyle("-fx-text-fill: white; -fx-background-color: #8f7a66;");
    quitButton.relocate(centerX, startY + 2 * (buttonHeight + spacing));
    root.getChildren().add(quitButton);

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