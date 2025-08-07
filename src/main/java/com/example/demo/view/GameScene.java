package com.example.demo.view;

import java.util.Random;

import com.example.demo.model.Cell;
import com.example.demo.utils.TextMaker;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GameScene {
    private static int gridSize = 4;
    private static final int HEIGHT = 700;
    private static final int DISTANCE_BETWEEN_CELLS = 10;
    private static double cellLength = calculateCellLength();
    
    private final TextMaker textMaker = TextMaker.getSingleInstance();
    private final Cell[][] cells = new Cell[gridSize][gridSize];
    private Group root;
    private long score = 0;
    private Text scoreText; // Store reference to score display

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

    public void initializeGame(Scene gameScene, Group root, Stage primaryStage, 
                             Scene endGameScene, Group endGameRoot) {
        this.root = root;
        initializeCells();
        setupScoreDisplay();
        startGame();
        setupKeyHandlers(gameScene, primaryStage, endGameScene, endGameRoot);
    }

    private void initializeCells() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cells[i][j] = new Cell(
                    (j) * cellLength + (j + 1) * DISTANCE_BETWEEN_CELLS,
                    (i) * cellLength + (i + 1) * DISTANCE_BETWEEN_CELLS, 
                    cellLength, root
                );
                // Set score callback for each cell
                cells[i][j].setScoreCallback(mergedValue -> {
                    score += mergedValue;
                    updateScoreDisplay();
                });

            }
        }
    }

    private void setupScoreDisplay() {
        Text scoreLabel = new Text("SCORE :");
        scoreLabel.setFont(Font.font(30));
        scoreLabel.relocate(750, 100);
        root.getChildren().add(scoreLabel);
        
        scoreText = new Text("0");
        scoreText.relocate(750, 150);
        scoreText.setFont(Font.font(20));
        root.getChildren().add(scoreText);
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
        int emptyCellStatus = checkEmptyCells();
        
        if (emptyCellStatus == -1 && canNotMove()) {
            endGame(primaryStage, endGameScene, endGameRoot);
        } else if (emptyCellStatus == 1) {
            fillRandomCell(2);
        }
    }

    private void endGame(Stage primaryStage, Scene endGameScene, Group endGameRoot) {
        primaryStage.setScene(endGameScene);
        EndGame.getInstance().endGameShow(endGameScene, endGameRoot, primaryStage, score);
        root.getChildren().clear();
        score = 0;
        updateScoreDisplay();
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

    private void moveLeft() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 1; j < gridSize; j++) {
                moveHorizontally(i, j, calculateDestination(i, j, 'l'), -1);
            }
            resetModifyFlags(i);
        }
    }

    private void moveRight() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = gridSize - 1; j >= 0; j--) {
                moveHorizontally(i, j, calculateDestination(i, j, 'r'), 1);
            }
            resetModifyFlags(i);
        }
    }

    private void moveUp() {
        for (int j = 0; j < gridSize; j++) {
            for (int i = 1; i < gridSize; i++) {
                moveVertically(i, j, calculateDestination(i, j, 'u'), -1);
            }
            resetVerticalModifyFlags(j);
        }
    }

    private void moveDown() {
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
}