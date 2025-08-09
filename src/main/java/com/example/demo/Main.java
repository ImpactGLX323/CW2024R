package com.example.demo;

import com.example.demo.view.GameScene;
import com.example.demo.view.MainMenu;

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
    private Scene menuScene;
    private Group menuRoot;
    private GameScene gameController;

    @Override
    public void start(Stage primaryStage) {
        // Initialize necessary scenes
        Scene endGameScene = createScene(new Group(), Color.rgb(250, 20, 100, 0.2));
        Group endGameRoot = (Group) endGameScene.getRoot();

        // Initialize the menu scene components FIRST
        this.menuRoot = new Group();
        this.menuScene = new Scene(menuRoot, WIDTH, HEIGHT, Color.rgb(220, 220, 220, 0.9));
        createMenuDecorations(menuRoot);

        // Initialize the game scene components
        this.gameRoot = new Group();
        this.gameScene = new Scene(gameRoot, WIDTH, HEIGHT, Color.rgb(189, 177, 92));
        this.gameController = new GameScene();

        // Prepare the main menu
        MainMenu mainMenu = new MainMenu();
        mainMenu.showMenu(
            menuScene, menuRoot, primaryStage,
            // onNewGame
            () -> {
                gameRoot.getChildren().clear();
                gameController.initializeGame(gameScene, gameRoot, primaryStage, endGameScene, endGameRoot, menuScene, menuRoot);
                primaryStage.setScene(gameScene);
            },
            // onLogin
            () -> {
                // TODO: Show login dialog or screen
            },
            // onManual
            () -> {
                // TODO: Show manual dialog or screen
            },
            // onQuit
            () -> {
                primaryStage.close();
            }
        );

        primaryStage.setScene(menuScene); // Start with the main menu
        primaryStage.setTitle("2048 Game");
        primaryStage.show();
    }

    private Scene createScene(Group root, Color backgroundColor) {
        return new Scene(root, WIDTH, HEIGHT, backgroundColor);
    }

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