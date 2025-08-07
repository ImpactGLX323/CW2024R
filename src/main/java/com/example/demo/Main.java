package com.example.demo;

import com.example.demo.view.GameScene;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 900;

    private Group gameRoot;
    private Scene gameScene;
    private GameScene gameController; // Add GameScene instance

    @Override
    public void start(Stage primaryStage) {
        // Initialize necessary scenes
        Scene endGameScene = createScene(new Group(), Color.rgb(250, 20, 100, 0.2));
        Group endGameRoot = (Group) endGameScene.getRoot();

        // Initialize the game scene components
        this.gameRoot = new Group();
        this.gameScene = new Scene(gameRoot, WIDTH, HEIGHT, Color.rgb(189, 177, 92));
        this.gameController = new GameScene(); // Initialize GameScene controller

        // Initialize game logic through the GameScene controller
        gameController.initializeGame(gameScene, gameRoot, primaryStage, endGameScene, endGameRoot);

        primaryStage.setScene(gameScene);
        primaryStage.setTitle("2048 Game");
        primaryStage.show();
    }

    /**
     * Utility method to create a Scene with given background color.
     */
    private Scene createScene(Group root, Color backgroundColor) {
        return new Scene(root, WIDTH, HEIGHT, backgroundColor);
    }

    /**
     * Optionally create decorated rectangles in scenes (can be expanded later).
     */
    private void createMenuDecorations(Group menuRoot) {
        Rectangle backgroundOfMenu = new Rectangle(240, 120, Color.rgb(120, 120, 120, 0.2));
        backgroundOfMenu.setX(WIDTH / 2 - 120);
        backgroundOfMenu.setY(180);
        menuRoot.getChildren().add(backgroundOfMenu);

        Rectangle backgroundOfMenuForPlay = new Rectangle(240, 140, Color.rgb(120, 20, 100, 0.2));
        backgroundOfMenuForPlay.setX(WIDTH / 2 - 120);
        backgroundOfMenuForPlay.setY(180);
        menuRoot.getChildren().add(backgroundOfMenuForPlay);
    }

    public static void main(String[] args) {
        launch(args);
    }
}