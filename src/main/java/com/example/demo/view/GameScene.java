/**
 * Main gameplay scene for Crack 2048.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Draw and scale the board responsively- responsive and properly scaled UI</li>
 *   <li>Handle key input and tile movement/merging</li>
 *   <li>Track score, display the scores and level progression</li>
 *   <li>Show Win or EndGame overlays</li>
 *   <li>Initialize and reset game state</li>
 *   <li>Manage game levels and grid sizes</li>
 * </ul>
 *
 * <p><strong>Design notes:</strong> The content is rendered in a {@code contentLayer}
 * which is scaled to preserve aspect ratio; background imagery is placed underneath on
 * {@code gameRoot}.</p>
 */

package com.example.demo.view;

import java.io.InputStream;
import java.util.Random;

import com.example.demo.model.Cell;
import com.example.demo.utils.TextMaker;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class GameScene {

        // LEVELS FOR THE CODE
    private static final int[][] LEVELS = {
        {4, 2048},  // Level 1: 4x4, target 2048
        {8, 4096},  // Level 2: 8x8, target 4096
        {10, 8192}  // Level 3: 10x10, target 8192
    };

    private int levelIndex = 0;      // starts at level 1 (index 0)
    private boolean won = false;     // preventing double popups
    private static int gridSize = 4;
    private static final int HEIGHT = 700;
    private static final int WIDTH = 900; // Add this if not present
    private static final int DISTANCE_BETWEEN_CELLS = 10;
    private static double cellLength = calculateCellLength();
    
    private final TextMaker textMaker = TextMaker.getSingleInstance();
    private Cell[][] cells; //cahnged this to dynamic not final
    private Group root;
    private long score = 0;
    private Text scoreText; // Store reference to score display
    private Scene gameScene;
    private Group gameRoot;
    private Scene menuScene;
    private Group menuRoot;
    private Scene gameSceneRef;
    private Group gameRootRef;
    private Stage primaryStageRef;
    private Group contentLayer;
    private ImageView bgView;
    private Font titleFont;
    private Font uiFont;

    // Base UI size
    private static final double BASE_W = WIDTH;  // 900
    private static final double BASE_H = HEIGHT; // 700

    private void setupResponsiveLayout(Scene scene) {
        if (contentLayer == null) return;

        // Scale factor that preserves aspect ratio
        DoubleBinding scale = Bindings.createDoubleBinding(
            () -> Math.min(scene.getWidth() / BASE_W, scene.getHeight() / BASE_H),
            scene.widthProperty(), scene.heightProperty()
        );

        // Apply scale via a Scale transform 
        Scale s = new Scale();
        s.xProperty().bind(scale);
        s.yProperty().bind(scale);
        contentLayer.getTransforms().setAll(s);

        // Center the scaled content
        ChangeListener<Object> centerer = (obs, oldV, newV) -> {
            double k = scale.get();
            double contentW = BASE_W * k;
            double contentH = BASE_H * k;
            contentLayer.setLayoutX((scene.getWidth() - contentW) / 2.0);
            contentLayer.setLayoutY((scene.getHeight() - contentH) / 2.0);
        };

        // Recenter on resize
        scene.widthProperty().addListener(centerer);
        scene.heightProperty().addListener(centerer);
        // Initial center
        centerer.changed(null, null, null);
    }

    public static void setGridSize(int size) {
        gridSize = size;
        cellLength = calculateCellLength();
    }

    public static double getCellLength() {
        return cellLength;
    }

    private static double calculateCellLength() {
        return (HEIGHT - ((gridSize + 1) * DISTANCE_BETWEEN_CELLS)) / (double) gridSize;
    }

  // 5-arg overload: only delegates to the 7-arg version.
    public void initializeGame(Scene gameScene, Group gameRoot, Stage primaryStage,
                            Scene endGameScene, Group endGameRoot) {
        initializeGame(gameScene, gameRoot, primaryStage,
                    endGameScene, endGameRoot,
                    null, null); // no main menu passed
    }

    //  7-arg full method: real implementation 
    public void initializeGame(Scene gameScene, Group gameRoot, Stage primaryStage,
                            Scene endGameScene, Group endGameRoot,
                            Scene menuScene, Group menuRoot) {
        this.gameSceneRef = gameScene;
        this.gameRootRef = gameRoot;
        this.menuScene = menuScene;          
        this.menuRoot  = menuRoot;           
        this.root = gameRoot;
        this.primaryStageRef = primaryStage;

        // content layer (for scaling responsively)
        if (contentLayer == null) {
            contentLayer = new Group();
            gameRoot.getChildren().add(contentLayer);
        }
        this.root = contentLayer;

        titleFont = loadRetroFont(48);
        uiFont    = loadRetroFont(20);

        setupBackground(gameSceneRef, gameRootRef);  // keeps bgView on gameRoot
        setupResponsiveLayout(gameSceneRef);

        initializeCells();
        setupScoreDisplay();
        startGame();

        cells[0][0].setColorByNumber(currentTargetTile());
        // -----------------------------------------------------------

        setupKeyHandlers(gameScene, primaryStage, endGameScene, endGameRoot);
    }

   private void initializeCells() {
        gridSize = currentGridSize();
            cellLength = calculateCellLength();

        // allocate cells fresh each time (different level sizes)
        cells = new Cell[gridSize][gridSize];
        double xOffset = 50;

        // shrink only for small grids; big grids are already small
        double scaledCellLength = (gridSize <= 4) ? cellLength * 0.75 : cellLength;

        double yOffset = 180;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double x = xOffset + j * scaledCellLength + (j + 1) * DISTANCE_BETWEEN_CELLS;
                double y = yOffset + i * scaledCellLength + (i + 1) * DISTANCE_BETWEEN_CELLS;

                cells[i][j] = new Cell(x, y, scaledCellLength, root);

                // score callback preserved
                cells[i][j].setScoreCallback(mergedValue -> {
                    score += mergedValue;
                    updateScoreDisplay();
                });
            }
        }
    }

    private boolean reachedTarget() {
        int target = currentTargetTile();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (cells[i][j].getNumber() >= target) return true;
            }
        }
        return false;
    }

    private void setupScoreDisplay() {
        // Title
        Text title = new Text("Crack 2048");
        title.setFont(titleFont != null ? titleFont : loadRetroFont(48));
        title.setFill(Color.rgb(255, 253, 251));
        title.applyCss();
        double titleX = (BASE_W - title.getLayoutBounds().getWidth()) / 2.0;
        title.setX(Math.max(20, titleX));
        title.setY(80);
        root.getChildren().add(title);

        // SCORE
        double boxW = 130, boxH = 80;
        double boxX = BASE_W - boxW - 70;  
        double boxY = 40;

        Rectangle scoreBox = new Rectangle(boxW, boxH);
        scoreBox.setArcWidth(15);
        scoreBox.setArcHeight(15);
        scoreBox.setFill(Color.rgb(119, 110, 101));
        scoreBox.setX(boxX);
        scoreBox.setY(boxY);
        root.getChildren().add(scoreBox);

        Text scoreLabel = new Text("SCORE");
        scoreLabel.setFont(uiFont != null ? uiFont : loadRetroFont(20));
        scoreLabel.setFill(Color.rgb(238, 228, 218));
        scoreLabel.setX(boxX + 20);
        scoreLabel.setY(boxY + 25);
        root.getChildren().add(scoreLabel);

        scoreText = new Text("0");
        scoreText.setFont(loadRetroFont(24));
        scoreText.setFill(Color.WHITE);
        scoreText.setX(boxX + 35);
        scoreText.setY(boxY + 55);
        root.getChildren().add(scoreText);

        //  Right-side vertical buttons 
        double buttonWidth = 130;
        double buttonHeight = 80;
        double spacing = 20;
        double startX = BASE_W - buttonWidth - 30;                // right margin
        double yBottom = BASE_H - (buttonHeight * 3 + spacing * 2) - 30;

        // Restart
        Rectangle restartBox = new Rectangle(buttonWidth, buttonHeight);
        restartBox.setArcWidth(15);
        restartBox.setArcHeight(15);
        restartBox.setFill(Color.rgb(96, 96, 96));
        restartBox.setX(startX);
        restartBox.setY(yBottom + 2 * (buttonHeight + spacing));
        root.getChildren().add(restartBox);

        Text restartText = new Text("RESTART");
        restartText.setFont(uiFont != null ? uiFont : loadRetroFont(20));
        restartText.setFill(Color.WHITE);
        restartText.setX(startX + 20);
        restartText.setY(yBottom + 2 * (buttonHeight + spacing) + 50);
        root.getChildren().add(restartText);

        restartText.setOnMouseClicked(event -> {
            contentLayer.getChildren().clear();
            won = false;
            setupBackground(gameSceneRef, gameRootRef);
            initializeCells();
            setupScoreDisplay();
            startGame();
            score = 0;
            updateScoreDisplay();
        });

        // Main Menu
        Rectangle menuBox = new Rectangle(buttonWidth, buttonHeight);
        menuBox.setArcWidth(15);
        menuBox.setArcHeight(15);
        menuBox.setFill(Color.rgb(96, 96, 96));
        menuBox.setX(startX);
        menuBox.setY(yBottom + (buttonHeight + spacing));
        root.getChildren().add(menuBox);

        Text menuText = new Text("MAIN MENU");
        menuText.setFont(uiFont != null ? uiFont : loadRetroFont(20));
        menuText.setFill(Color.WHITE);
        menuText.setX(startX + 10);
        menuText.setY(yBottom + (buttonHeight + spacing) + 50);
        root.getChildren().add(menuText);

        menuText.setOnMouseClicked(event -> {
            contentLayer.getChildren().clear();

            // Fallbacks so tests or callers without a menu still work
            if (menuRoot == null) menuRoot = new Group();
            if (menuScene == null) menuScene = new Scene(menuRoot, BASE_W, BASE_H);

            if (primaryStageRef != null) {
                MainMenu mm = new MainMenu();
                mm.showMenu(
                    menuScene,
                    menuRoot,
                    primaryStageRef,
                    // onNewGame:
                    () -> {
                        contentLayer.getChildren().clear();
                        setupBackground(gameSceneRef, gameRootRef);
                        score = 0;
                        won = false;
                        initializeCells();
                        setupScoreDisplay();
                        startGame();
                        updateScoreDisplay();
                        primaryStageRef.setScene(gameSceneRef);
                    },
                    // onLogin:
                    () -> { /* TODO: login overlay later */ },
                    // onManual:
                    () -> { /* TODO: manual overlay later */ },
                    // onQuit:
                    Platform::exit
                );
                primaryStageRef.setScene(menuScene);
            }
        });

        // Quit
        Rectangle quitBox = new Rectangle(buttonWidth, buttonHeight);
        quitBox.setArcWidth(15);
        quitBox.setArcHeight(15);
        quitBox.setFill(Color.rgb(96, 96, 96));
        quitBox.setX(startX);
        quitBox.setY(yBottom);
        root.getChildren().add(quitBox);

        Text quitText = new Text("QUIT GAME");
        quitText.setFont(uiFont != null ? uiFont : loadRetroFont(20));
        quitText.setFill(Color.WHITE);
        quitText.setX(startX + 10);
        quitText.setY(yBottom + 50);
        root.getChildren().add(quitText);

        quitText.setOnMouseClicked(event -> Platform.exit());
    }

    private void startGame() {
        fillRandomCell(1);
        fillRandomCell(1);
    }

    private void setupKeyHandlers(Scene gameScene, Stage primaryStage, 
                                Scene endGameScene, Group endGameRoot) {
        gameScene.addEventHandler(KeyEvent.KEY_PRESSED, key -> {
            Platform.runLater(() -> {
                handleKeyPress(key.getCode());
                updateGameState(primaryStage, endGameScene, endGameRoot);
            });
        });
    }

    private void handleKeyPress(KeyCode code) {
        switch (code) {
            case DOWN -> moveDown();
            case UP -> moveUp();
            case LEFT -> moveLeft();
            case RIGHT -> moveRight();
            default -> {}
        }
    }

        private void updateGameState(Stage primaryStage, Scene endGameScene, Group endGameRoot) {
        if (!won && reachedTarget()) {
            won = true;
            showWin(primaryStage, endGameScene, endGameRoot);
            return;
        }

        int emptyCellStatus = checkEmptyCells();

        if (emptyCellStatus == -1 && canNotMove()) {
            endGame(primaryStage, endGameScene, endGameRoot);
        } else if (emptyCellStatus == 1) {
            fillRandomCell(2);
        }
    }

    private void endGame(Stage primaryStage, Scene endGameScene, Group endGameRoot) {
        primaryStage.setScene(endGameScene);

        EndGame.getInstance().endGameShow(
            endGameScene, endGameRoot, primaryStage, score,
            // onRestart:
            () -> {
                // Ensures contentLayer exists and is attached
                if (contentLayer == null) {
                    contentLayer = new Group();
                }
                if (contentLayer.getParent() != gameRootRef) {
                    gameRootRef.getChildren().clear();
                    gameRootRef.getChildren().add(contentLayer);
                } else {
                    contentLayer.getChildren().clear();
                }

                // Re-add/update the background under the content layer
                setupBackground(gameSceneRef, gameRootRef);

                // Draw into contentLayer (root must point to it)
                this.root = contentLayer;

                // Reset game state
                score = 0;
                won = false;

                initializeCells();
                setupScoreDisplay();
                startGame();
                updateScoreDisplay();

                // Back to the game scene
                primaryStage.setScene(gameSceneRef);
            },
            // onMenu:
            () -> {
                if (menuRoot != null && menuScene != null) {
                    menuRoot.getChildren().clear();
                    primaryStage.setScene(menuScene);
                }
            },
            // onQuit:
            () -> {
                endGameRoot.getChildren().clear();
                Platform.exit();
            }
        );
    }

    private void fillRandomCell(int turn) {
        Cell randomCell = findRandomEmptyCell();
        if (randomCell == null) return;

        boolean putTwo = new Random().nextBoolean();
        int number = putTwo ? 2 : 4;
        
        Text text = textMaker.madeText(
            String.valueOf(number), 
            randomCell.getX(), 
            randomCell.getY(), 
            root
        );
        
        randomCell.setTextClass(text);
        root.getChildren().add(text);
        randomCell.setColorByNumber(number);
    }

    private Cell findRandomEmptyCell() {
        Cell[] emptyCells = new Cell[gridSize * gridSize];
        int count = 0;
        
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (cells[i][j].getNumber() == 0) {
                    emptyCells[count++] = cells[i][j];
                }
                if (cells[i][j].getNumber() == 2048) {
                    return null;
                }
            }
        }
        
        if (count == 0) return null;
        return emptyCells[new Random().nextInt(count)];
    }

    private int checkEmptyCells() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (cells[i][j].getNumber() == 0) return 1;
                if (cells[i][j].getNumber() == 2048) return 0;
            }
        }
        return -1;
    }

    private int calculateDestination(int i, int j, char direction) {
        return switch (direction) {
            case 'l' -> calculateLeftDestination(i, j);
            case 'r' -> calculateRightDestination(i, j);
            case 'u' -> calculateUpDestination(i, j);
            case 'd' -> calculateDownDestination(i, j);
            default -> -1;
        };
    }

    private int calculateLeftDestination(int i, int j) {
        for (int k = j - 1; k >= 0; k--) {
            if (cells[i][k].getNumber() != 0) return k + 1;
        }
        return 0;
    }

    private int calculateRightDestination(int i, int j) {
        for (int k = j + 1; k < gridSize; k++) {
            if (cells[i][k].getNumber() != 0) return k - 1;
        }
        return gridSize - 1;
    }

    private int calculateUpDestination(int i, int j) {
        for (int k = i - 1; k >= 0; k--) {
            if (cells[k][j].getNumber() != 0) return k + 1;
        }
        return 0;
    }

    private int calculateDownDestination(int i, int j) {
        for (int k = i + 1; k < gridSize; k++) {
            if (cells[k][j].getNumber() != 0) return k - 1;
        }
        return gridSize - 1;
    }

    public void moveLeft() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 1; j < gridSize; j++) {
                moveHorizontally(i, j, calculateDestination(i, j, 'l'), -1);
            }
            resetModifyFlags(i);
        }
    }

    public void moveRight() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = gridSize - 1; j >= 0; j--) {
                moveHorizontally(i, j, calculateDestination(i, j, 'r'), 1);
            }
            resetModifyFlags(i);
        }
    }

    public void moveUp() {
        for (int j = 0; j < gridSize; j++) {
            for (int i = 1; i < gridSize; i++) {
                moveVertically(i, j, calculateDestination(i, j, 'u'), -1);
            }
            resetVerticalModifyFlags(j);
        }
    }

    public void moveDown() {
        for (int j = 0; j < gridSize; j++) {
            for (int i = gridSize - 1; i >= 0; i--) {
                moveVertically(i, j, calculateDestination(i, j, 'd'), 1);
            }
            resetVerticalModifyFlags(j);
        }
    }

    private void resetModifyFlags(int row) {
        for (int j = 0; j < gridSize; j++) {
            cells[row][j].setModify(false);
        }
    }

    private void resetVerticalModifyFlags(int col) {
        for (int i = 0; i < gridSize; i++) {
            cells[i][col].setModify(false);
        }
    }

    private boolean isValidHorizontalMove(int i, int j, int des, int sign) {
        return des + sign < gridSize && des + sign >= 0 
            && cells[i][des + sign].getNumber() == cells[i][j].getNumber() 
            && !cells[i][des + sign].getModify() 
            && cells[i][des + sign].getNumber() != 0;
    }

    private void moveHorizontally(int i, int j, int des, int sign) {
        if (isValidHorizontalMove(i, j, des, sign)) {
            int mergedValue = cells[i][j].getNumber() + cells[i][des + sign].getNumber();
            cells[i][j].adder(cells[i][des + sign]);
            score += mergedValue; // Add this line
            updateScoreDisplay(); // Add this line to update the UI
            cells[i][des].setModify(true);
        } else if (des != j) {
            cells[i][j].changeCell(cells[i][des]);
        }
    }
    private boolean isValidVerticalMove(int i, int j, int des, int sign) {
        return des + sign < gridSize && des + sign >= 0
            && cells[des + sign][j].getNumber() == cells[i][j].getNumber()
            && !cells[des + sign][j].getModify()
            && cells[des + sign][j].getNumber() != 0;
    }
    
    private void moveVertically(int i, int j, int des, int sign) {
        if (isValidVerticalMove(i, j, des, sign)) {
            int mergedValue = cells[i][j].getNumber() + cells[des + sign][j].getNumber();
            cells[i][j].adder(cells[des + sign][j]);
            score += mergedValue; // Add this line
            updateScoreDisplay(); // Add this line to update the UI
            cells[des][j].setModify(true);
        } else if (des != i) {
            cells[i][j].changeCell(cells[des][j]);
        }
    }

    private boolean hasSameNeighbor(int i, int j) {
        return (i < gridSize - 1 && cells[i + 1][j].getNumber() == cells[i][j].getNumber())
            || (j < gridSize - 1 && cells[i][j + 1].getNumber() == cells[i][j].getNumber());
    }

    private boolean canNotMove() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (hasSameNeighbor(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateScoreDisplay() {
        if (scoreText != null) {
            scoreText.setText(String.valueOf(score));
        }
    }
    public static void setCellLength(double len) {
    cellLength = len;
}


//HELPERS FOR NEW LEVEL 
    private int currentGridSize() {
        return LEVELS[levelIndex][0];
    }

    private int currentTargetTile() {
        return LEVELS[levelIndex][1];
    }

    private boolean isLastLevel() {
        return levelIndex >= LEVELS.length - 1;
    }

        private void showWin(Stage primaryStage, Scene endGameScene, Group endGameRoot) {
        // Reuse a separate WinGame overlay similar to EndGame
        WinGame.getInstance().winGameShow(
            endGameScene, endGameRoot, primaryStage, score,
            // onNextLevel:
            () -> {
                if (!isLastLevel()) {
                    levelIndex++;
                    contentLayer.getChildren().clear();
                    setupBackground(gameSceneRef, gameRootRef); 
                    score = 0;
                    won = false;
                    initializeCells();
                    setupScoreDisplay();
                    startGame();
                    updateScoreDisplay();
                    primaryStage.setScene(gameSceneRef);
                } else {
                    menuRoot.getChildren().clear();
                    primaryStage.setScene(menuScene);
                }
            },
            // onRestartFromStart:
            () -> {
                levelIndex = 0; // back to level 1
                contentLayer.getChildren().clear();
                setupBackground(gameSceneRef, gameRootRef);
                score = 0;
                won = false;
                initializeCells();
                setupScoreDisplay();
                startGame();
                updateScoreDisplay();
                primaryStage.setScene(gameSceneRef);
            },
            // onMenu:
            () -> {
                endGameRoot.getChildren().clear();
                primaryStage.setScene(menuScene);
            }
        );
    }

    private static Font loadRetroFont(double size) {
        String[] candidates = {
            "/com/example/demo/fonts/Orbitron-VariableFont_wght.ttf",
            "/com/example/demo/fonts/VT323-Regular.ttf"
        };
        for (String path : candidates) {
            try (InputStream is = GameScene.class.getResourceAsStream(path)) {
                if (is != null) {
                    Font f = Font.loadFont(is, size);
                    if (f != null) return f;
                }
            } catch (Exception ignored) {}
        }
        return Font.font("Arial", size); // fallback
    }

    private void setupBackground(Scene scene, Group baseRoot) {
        final String bgPath = switch (currentGridSize()) {
            case 4  -> "/com/example/demo/image/level1_bg.jpg";
            case 8  -> "/com/example/demo/image/level2_bg.jpg";
            case 10 -> "/com/example/demo/image/level3_bg.jpg";
            default -> "/com/example/demo/image/level1_bg.jpg";
        };

        if (bgView == null) {
            bgView = new ImageView();
            bgView.setPreserveRatio(false);
            bgView.fitWidthProperty().bind(scene.widthProperty());
            bgView.fitHeightProperty().bind(scene.heightProperty());
            baseRoot.getChildren().add(0, bgView); // bottom-most
        } else if (bgView.getParent() != baseRoot) {
            // move from old parent (if any)
            ((Group) bgView.getParent()).getChildren().remove(bgView);
            baseRoot.getChildren().add(0, bgView);
        }

        var url = getClass().getResource(bgPath);
        if (url == null) {
            System.err.println("[GameScene] Background not found: " + bgPath);
            return;
        }
        bgView.setImage(new Image(url.toExternalForm()));
        System.out.println("[GameScene] Background set: " + bgPath);
    }
}


