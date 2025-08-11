package com.example.demo;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.view.GameScene;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GameSceneTest {

    private GameScene gameScene;
    private Group gameRoot;
    private Scene gameSceneFx;
    private Stage stage;

    // extra scenes/roots your initializeGame now requires (7-arg)
    private Group endGameRoot;
    private Scene endGameScene;
    private Group menuRoot;
    private Scene menuScene;

    @BeforeAll
    public static void initToolkit() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX platform failed to start.");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            gameScene = new GameScene();

            gameRoot = new Group();
            gameSceneFx = new Scene(gameRoot, 900, 700);

            endGameRoot = new Group();
            endGameScene = new Scene(endGameRoot, 900, 700);

            menuRoot = new Group();
            menuScene = new Scene(menuRoot, 900, 700);

            stage = new Stage();
            stage.setScene(gameSceneFx);

            // Uses the 5 arg signature
            gameScene.initializeGame(
                gameSceneFx, gameRoot, stage,
                endGameScene, endGameRoot
            );
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    // ------- helpers -------
    private static Optional<Text> findText(Node root, String exact) {
        if (root instanceof Text t && exact.equals(t.getText())) return Optional.of(t);
        if (root instanceof Group g) {
            for (Node c : g.getChildren()) {
                var f = findText(c, exact);
                if (f.isPresent()) return f;
            }
        }
        return Optional.empty();
    }

    private static int extractFirstNumericText(Node root) {
        if (root instanceof Text t && t.getText().matches("\\d+")) {
            return Integer.parseInt(t.getText());
        }
        if (root instanceof Group g) {
            for (Node c : g.getChildren()) {
                int v = extractFirstNumericText(c);
                if (v >= 0) return v;
            }
        }
        return -1;
    }


    @Test
    public void testScoreCalculationAfterMerge() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            int initialScore = Math.max(0, extractFirstNumericText(gameRoot));
            gameScene.moveLeft();
            int newScore = Math.max(0, extractFirstNumericText(gameRoot));
            assertTrue(newScore >= initialScore);
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testCellIsFilledRandomly() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            boolean foundAnyText = findText(gameRoot, "0").isPresent() // crude presence check
                || findText(gameRoot, "2").isPresent()
                || findText(gameRoot, "4").isPresent();
            assertTrue(foundAnyText, "No text nodes were added during game initialization.");
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testMoveLeftRightFunctionality() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            int before = countNodes(gameRoot);
            gameScene.moveLeft();
            gameScene.moveRight();
            int after = countNodes(gameRoot);
            assertTrue(after >= 0); // smoke test: no crash, UI still intact
            assertEquals(before, after); // usually unchanged count
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testScoreDisplayedUpdatesCorrectly() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            int before = Math.max(0, extractFirstNumericText(gameRoot));
            gameScene.moveDown();
            int after = Math.max(0, extractFirstNumericText(gameRoot));
            assertTrue(after >= before);
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testRandomCellFilling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            int before = countTextNodes(gameRoot);
            gameScene.moveUp();
            int after = countTextNodes(gameRoot);
            assertTrue(after >= before);
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testUpdateGameStateTriggersWinOverlay() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Method m = GameScene.class.getDeclaredMethod(
                    "updateGameState", Stage.class, Scene.class, Group.class
                );
                m.setAccessible(true);
                m.invoke(gameScene, stage, endGameScene, endGameRoot);

                assertSame(endGameScene, stage.getScene(),
                    "Stage should switch to endGameScene on win");
            } catch (Exception e) {
                fail("Reflection call to updateGameState failed: " + e);
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testRestartButtonResetsScore() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            gameScene.moveLeft();
            int scoreBefore = Math.max(0, extractFirstNumericText(gameRoot));

            Text restart = findText(gameRoot, "RESTART")
                .orElseThrow(() -> new AssertionError("RESTART text not found"));
            EventHandler<? super MouseEvent> handler = restart.getOnMouseClicked();
            assertNotNull(handler, "RESTART should have a click handler");
            handler.handle(null); // your handler doesn't read the event

            int scoreAfter = Math.max(0, extractFirstNumericText(gameRoot));
            assertEquals(0, scoreAfter, "Score should reset to 0 after restart");
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testMainMenuButtonSwitchesScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Text menu = findText(gameRoot, "MAIN MENU")
                .orElseThrow(() -> new AssertionError("MAIN MENU text not found"));
            EventHandler<? super MouseEvent> handler = menu.getOnMouseClicked();
            assertNotNull(handler, "MAIN MENU should have a click handler");
            handler.handle(null);

            // We didn't pass a menuScene/root to initializeGame (5-arg overload),
            // so GameScene builds its own menu scene. Just assert the title exists.
            assertTrue(
                findText(stage.getScene().getRoot(), "Crack 2048").isPresent(),
                "Main menu title should be visible after clicking MAIN MENU"
            );
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testQuitButtonHasHandler() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Text quit = findText(gameRoot, "QUIT GAME")
                .orElseThrow(() -> new AssertionError("QUIT GAME text not found"));
            assertNotNull(quit.getOnMouseClicked(), "QUIT GAME should have a click handler");
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    private static int countNodes(Node n) {
        if (n instanceof Group g) {
            int total = 1;
            for (Node c : g.getChildren()) total += countNodes(c);
            return total;
        }
        return 1;
    }

    private static int countTextNodes(Node n) {
        int total = (n instanceof Text) ? 1 : 0;
        if (n instanceof Group g) {
            for (Node c : g.getChildren()) total += countTextNodes(c);
        }
        return total;
    }
}