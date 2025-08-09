package com.example.demo.view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainMenu {
    public void showMenu(Scene menuScene, Group root, Stage primaryStage, Runnable onNewGame, Runnable onLogin, Runnable onManual, Runnable onQuit) {
        root.getChildren().clear();

        // Title
        Text title = new Text("Crack 2048");
        title.setFont(Font.font("Arial", 80));
        title.setFill(Color.web("#776e65"));
        title.setX((menuScene.getWidth() - 400) / 2); // Adjust as needed
        title.setY(150);
        root.getChildren().add(title);

        // Button configuration
        double buttonWidth = 300;
        double buttonHeight = 60;
        double spacing = 20;
        double centerX = (menuScene.getWidth() - buttonWidth) / 2;
        double startY = 300;

        // New Game Button
        Button newGameButton = new Button("NEW GAME");
        newGameButton.setPrefSize(buttonWidth, buttonHeight);
        newGameButton.setStyle("-fx-font-size: 20;");
        newGameButton.relocate(centerX, startY);
        root.getChildren().add(newGameButton);

        // Login Button
        Button loginButton = new Button("LOGIN");
        loginButton.setPrefSize(buttonWidth, buttonHeight);
        loginButton.setStyle("-fx-font-size: 20;");
        loginButton.relocate(centerX, startY + buttonHeight + spacing);
        root.getChildren().add(loginButton);

        // Game Manual Button
        Button manualButton = new Button("GAME MANUAL");
        manualButton.setPrefSize(buttonWidth, buttonHeight);
        manualButton.setStyle("-fx-font-size: 20;");
        manualButton.relocate(centerX, startY + 2 * (buttonHeight + spacing));
        root.getChildren().add(manualButton);

        // Quit Game Button
        Button quitButton = new Button("QUIT GAME");
        quitButton.setPrefSize(buttonWidth, buttonHeight);
        quitButton.setStyle("-fx-font-size: 20;");
        quitButton.relocate(centerX, startY + 3 * (buttonHeight + spacing));
        root.getChildren().add(quitButton);

        // Button actions
        newGameButton.setOnAction(e -> { if (onNewGame != null) onNewGame.run(); });
        loginButton.setOnAction(e -> { if (onLogin != null) onLogin.run(); });
        manualButton.setOnAction(e -> { if (onManual != null) onManual.run(); });
        quitButton.setOnAction(e -> { if (onQuit != null) onQuit.run(); });
    }
}