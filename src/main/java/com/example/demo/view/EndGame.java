package com.example.demo.view;

import java.util.Optional;

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
    private EndGame(){

    }
    public static EndGame getInstance(){
        if(singleInstance == null)
            singleInstance= new EndGame();
        return singleInstance;
    }

    public void endGameShow(Scene endGameScene, Group root, Stage primaryStage,long score, Scene gameScene, Group gameRoot, Scene menuScene, Group menuRoot) {
        Text text = new Text("GAME OVER");
        text.relocate(250,250);
        text.setFont(Font.font(80));
        root.getChildren().add(text);

        Text scoreText = new Text(score+"");
        scoreText.setFill(Color.BLACK);
        scoreText.relocate(250,600);
        scoreText.setFont(Font.font(80));
        root.getChildren().add(scoreText);

        double buttonWidth = 200;
        double buttonHeight = 80;
        double spacing = 30;
        double centerX = 350; // Adjust as needed for your layout
        double startY = 750;

        // Restart Button
        Button restartButton = new Button("RESTART");
        restartButton.setPrefSize(buttonWidth, buttonHeight);
        restartButton.setTextFill(Color.DARKBLUE);
        restartButton.relocate(centerX, startY);
        root.getChildren().add(restartButton);

        // Main Menu Button
        Button menuButton = new Button("MAIN MENU");
        menuButton.setPrefSize(buttonWidth, buttonHeight);
        menuButton.setTextFill(Color.DARKGREEN);
        menuButton.relocate(centerX, startY + buttonHeight + spacing);
        root.getChildren().add(menuButton);

        // Quit Button
        Button quitButton = new Button("QUIT");
        quitButton.setPrefSize(buttonWidth, buttonHeight);
        quitButton.setTextFill(Color.PINK);
        root.getChildren().add(quitButton);
        quitButton.relocate(centerX, startY + 2 * (buttonHeight + spacing));
        root.getChildren().add(quitButton);
        
        // Button actions
        restartButton.setOnMouseClicked(event -> {
            root.getChildren().clear();
            //  Clear and reinitialize the game scene
            gameRoot.getChildren().clear();
            primaryStage.setScene(gameScene);
        });

        menuButton.setOnMouseClicked(event -> {
            root.getChildren().clear();
            // You may want to show your main menu here
            primaryStage.setScene(menuScene);
        });

        quitButton.setOnMouseClicked(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Quit Dialog");
            alert.setHeaderText("Quit from this page");
            alert.setContentText("Are you sure?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                root.getChildren().clear();
                // Optionally, exit the application:
                // Platform.exit();
            }
        });
    }
}
