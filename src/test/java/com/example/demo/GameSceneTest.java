package com.example.demo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.view.GameScene;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GameSceneTest {

    private GameScene gameScene;
    private Group root;
    private Scene scene;
    private Stage stage;

    @BeforeAll
    public static void initToolkit() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);  // Initializes JavaFX
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX platform failed to start.");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            gameScene = new GameScene();
            root = new Group();
            scene = new Scene(root, 900, 900);
            stage = new Stage();
            gameScene.initializeGame(scene, root, stage, new Scene(new Group()), new Group());
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testScoreCalculationAfterMerge() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            int initialScore = getDisplayedScore();
            gameScene.moveLeft();
            int newScore = getDisplayedScore();
            assertTrue(newScore >= initialScore);
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testCellIsFilledRandomly() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            boolean found = root.getChildren().stream().anyMatch(node -> node instanceof Text);
            assertTrue(found, "No text blocks were added during game initialization.");
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testMoveLeftRightFunctionality() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            int before = root.getChildren().size();
            gameScene.moveLeft();
            gameScene.moveRight();
            int after = root.getChildren().size();
            assertEquals(before, after); // Assuming blocks move but none are added/removed
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testScoreDisplayedUpdatesCorrectly() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            int scoreBefore = getDisplayedScore();
            gameScene.moveDown();
            int scoreAfter = getDisplayedScore();
            assertTrue(scoreAfter >= scoreBefore);
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testRandomCellFilling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            int countBefore = (int) root.getChildren().stream().filter(node -> node instanceof Text).count();
            gameScene.moveUp();
            int countAfter = (int) root.getChildren().stream().filter(node -> node instanceof Text).count();
            assertTrue(countAfter >= countBefore);
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    private int getDisplayedScore() {
        return root.getChildren().stream()
                .filter(node -> node instanceof Text)
                .map(node -> ((Text) node).getText())
                .filter(s -> s.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(0);
    }
}
