package com.example.demo.view;

import java.util.Random;

import com.example.demo.model.Cell;
import com.example.demo.utils.TextMaker;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GameScene {

        // --- LEVELS: {gridSize, targetTile} ---
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

    public void initializeGame(Scene gameScene, Group gameRoot, Stage primaryStage,
                          Scene endGameScene, Group endGameRoot,
                          Scene menuScene, Group menuRoot) {
        this.gameSceneRef = gameScene;
        this.gameRootRef = gameRoot;
        this.menuScene = menuScene;
        this.menuRoot = menuRoot;
        this.root = gameRoot;
        initializeCells();
        setupScoreDisplay();
        startGame();

        // --- TEST CODE: instantly win the game ---
        cells[0][0].setTextClass(
            textMaker.madeText(String.valueOf(currentTargetTile()),
            cells[0][0].getX(),
            cells[0][0].getY(),
            root)
        );
        cells[0][0].setColorByNumber(currentTargetTile());
    // ------------------------------------------
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
    title.setFont(Font.font("Arial", 48));
    title.setFill(Color.rgb(119, 110, 101));
    title.setX(320); // Centered horizontally
    title.setY(80);
    root.getChildren().add(title);
    // Score Box
    Rectangle scoreBox = new Rectangle(130, 80);
    scoreBox.setArcWidth(15);
    scoreBox.setArcHeight(15);
    scoreBox.setFill(Color.rgb(119, 110, 101));
    scoreBox.setX(700);
    scoreBox.setY(40);
    root.getChildren().add(scoreBox);

    Text scoreLabel = new Text("SCORE");
    scoreLabel.setFont(Font.font("Arial", 20));
    scoreLabel.setFill(Color.rgb(238, 228, 218));
    scoreLabel.setX(720);
    scoreLabel.setY(65);
    root.getChildren().add(scoreLabel);

    scoreText = new Text("0");
    scoreText.setFont(Font.font("Arial", 24));
    scoreText.setFill(Color.rgb(255, 255, 255));
    scoreText.setX(735);
    scoreText.setY(95);
    root.getChildren().add(scoreText);

    // --- Bottom Right Vertical Buttons ---
    double buttonWidth = 130;
    double buttonHeight = 80;
    double spacing = 20;
    double startX = WIDTH - buttonWidth - 30; // 30px margin from right
    double yBottom = HEIGHT - (buttonHeight * 3 + spacing * 2) - 30; // Stack 3 buttons upward from bottom

    // Restart Button (bottom)
    Rectangle restartBox = new Rectangle(buttonWidth, buttonHeight);
    restartBox.setArcWidth(15);
    restartBox.setArcHeight(15);
    restartBox.setFill(Color.rgb(96, 96, 96));
    restartBox.setX(startX);
    restartBox.setY(yBottom + 2 * (buttonHeight + spacing));
    root.getChildren().add(restartBox);

    Text restartText = new Text("RESTART");
    restartText.setFont(Font.font("Arial", 20));
    restartText.setFill(Color.rgb(255, 255, 255));
    restartText.setX(startX + 20);
    restartText.setY(yBottom + 2 * (buttonHeight + spacing) + 50);
    root.getChildren().add(restartText);

    restartText.setOnMouseClicked(event -> {
        root.getChildren().clear();
        initializeCells();
        setupScoreDisplay();
        startGame();
        score = 0;
        updateScoreDisplay();
    });

    // Main Menu Button (middle)
    Rectangle menuBox = new Rectangle(buttonWidth, buttonHeight);
    menuBox.setArcWidth(15);
    menuBox.setArcHeight(15);
    menuBox.setFill(Color.rgb(96, 96, 96));
    menuBox.setX(startX);
    menuBox.setY(yBottom + (buttonHeight + spacing));
    root.getChildren().add(menuBox);

    Text menuText = new Text("MAIN MENU");
    menuText.setFont(Font.font("Arial", 20));
    menuText.setFill(Color.rgb(255, 255, 255));
    menuText.setX(startX + 10);
    menuText.setY(yBottom + (buttonHeight + spacing) + 50);
    root.getChildren().add(menuText);

    // TODO: Add menuText.setOnMouseClicked(...) to handle main menu navigation

    // Quit Game Button (top)
    Rectangle quitBox = new Rectangle(buttonWidth, buttonHeight);
    quitBox.setArcWidth(15);
    quitBox.setArcHeight(15);
    quitBox.setFill(Color.rgb(96, 96, 96));
    quitBox.setX(startX);
    quitBox.setY(yBottom);
    root.getChildren().add(quitBox);

    Text quitText = new Text("QUIT GAME");
    quitText.setFont(Font.font("Arial", 20));
    quitText.setFill(Color.rgb(255, 255, 255));
    quitText.setX(startX + 10);
    quitText.setY(yBottom + 50);
    root.getChildren().add(quitText);

    quitText.setOnMouseClicked(event -> {
        Platform.exit();
    });
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
                gameRootRef.getChildren().clear();
                score = 0;
                initializeCells();
                setupScoreDisplay();
                startGame();
                updateScoreDisplay();
                primaryStage.setScene(gameSceneRef);
            },
            // onMenu:
            () -> {
                menuRoot.getChildren().clear();
                primaryStage.setScene(menuScene);
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
                    gameRootRef.getChildren().clear();
                    score = 0;
                    won = false;
                    initializeCells();
                    setupScoreDisplay();
                    startGame();
                    updateScoreDisplay();
                    primaryStage.setScene(gameSceneRef);
                } else {
                    // If already last level, return to main menu
                    menuRoot.getChildren().clear();
                    primaryStage.setScene(menuScene);
                }
            },
            // onRestartFromStart:
            () -> {
                levelIndex = 0; // back to level 1
                gameRootRef.getChildren().clear();
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

}

