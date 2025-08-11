package com.example.demo;

import com.example.demo.view.GameScene;
import com.example.demo.view.MainMenu;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Main class serves as the entry point and primary controller for the 2048 Game application.
 * This class extends JavaFX Application and manages the initialization and coordination
 * of all major game scenes including the main menu, game scene, and end game overlay.
 * 
 * <p>The application follows a scene-based architecture where different game states
 * (menu, gameplay, game over) are represented as separate JavaFX scenes that can be
 * switched dynamically based on user interactions.</p>
 * 
 * <p>Key responsibilities include:
 * <ul>
 *   <li>Application lifecycle management</li>
 *   <li>Scene initialization and switching</li>
 *   <li>Coordination between menu and game controllers</li>
 *   <li>Primary stage configuration</li>
 * </ul>
 * 
 * @author Generated JavaDoc
 * @version 1.0
 * @since 1.0
 */
public class Main extends Application {
    
    /** The application window width in pixels */
    private static final int WIDTH = 900;
    
    /** The application window height in pixels */
    private static final int HEIGHT = 900;
    
    /** The root Group node for the game scene containing all game UI elements */
    private Group gameRoot;
    
    /** The Scene object representing the main gameplay area */
    private Scene gameScene;
    
    /** The Scene object representing the main menu interface */
    private Scene menuScene;
    
    /** The root Group node for the menu scene containing all menu UI elements */
    private Group menuRoot;
    
    /** The controller responsible for managing game logic and UI updates */
    private GameScene gameController;

    /**
     * The main entry point for the JavaFX application. This method is called after
     * the JavaFX runtime initializes and creates the primary stage.
     * 
     * <p>This method performs the following initialization sequence:
     * <ol>
     *   <li>Creates the end game overlay scene</li>
     *   <li>Initializes the main menu scene with decorative elements</li>
     *   <li>Initializes the game scene and controller</li>
     *   <li>Configures the main menu with callback functions for navigation</li>
     *   <li>Sets up the primary stage and displays the main menu</li>
     * </ol>
     * 
     * @param primaryStage the primary stage provided by the JavaFX platform
     * @throws Exception if an error occurs during application startup
     */
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

    /**
     * Creates a new Scene with the specified root node and background color.
     * The scene dimensions are set to the application's standard WIDTH and HEIGHT.
     * 
     * <p>This utility method provides a consistent way to create scenes across
     * the application with standardized dimensions.</p>
     * 
     * @param root the root Group node that will contain all UI elements for this scene
     * @param backgroundColor the background color for the scene using JavaFX Color
     * @return a new Scene object with the specified parameters and standard dimensions
     */
    private Scene createScene(Group root, Color backgroundColor) {
        return new Scene(root, WIDTH, HEIGHT, backgroundColor);
    }

    /**
     * Creates and adds decorative background elements to the main menu scene.
     * 
     * <p>This method adds visual enhancements to the menu interface by creating
     * two overlapping rectangular backgrounds with different colors and transparency
     * levels to create a layered visual effect.</p>
     * 
     * <p>The decorations include:
     * <ul>
     *   <li>A gray semi-transparent background rectangle (240x120 pixels)</li>
     *   <li>A purple/magenta semi-transparent overlay rectangle (240x140 pixels)</li>
     * </ul>
     * 
     * Both rectangles are centered horizontally and positioned at a fixed vertical
     * offset to complement the main menu layout.
     * 
     * @param menuRoot the root Group node of the menu scene to which decorations will be added
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

    /**
     * The main method that serves as the entry point for the entire application.
     * This method delegates to the JavaFX Application.launch() method to initialize
     * the JavaFX runtime and start the application lifecycle.
     * 
     * <p>Command line arguments are passed through to the JavaFX framework
     * for potential use in application configuration.</p>
     * 
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}