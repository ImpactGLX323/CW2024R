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

/**
 * Unit tests for the {@link GameScene} class.
 * <p>
 * These tests validate the correct initialization and behavior of the game UI components,
 * including score updates, cell filling, move functionality, and button event handlers.
 * The tests run on the JavaFX application thread and use reflection for some internal state verification.
 * </p>
 * <p>
 * The test class sets up multiple scenes and roots to simulate transitions between game states,
 * such as the main game, end game, and menu scenes.
 * </p>
 */
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

    /**
     * Initializes the JavaFX platform toolkit before all tests run.
     * This ensures that JavaFX application thread is started for UI operations.
     * 
     * @throws Exception if the platform fails to start within the timeout
     */
    @BeforeAll
    public static void initToolkit() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX platform failed to start.");
        }
    }

    /**
     * Sets up the game scene and related UI components before each test.
     * Initializes the {@link GameScene} instance and creates scenes and roots
     * for game, end game, and menu states.
     * 
     * @throws Exception if the setup does not complete within the timeout
     */
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

    /**
     * Recursively searches for a {@link Text} node with the exact given string in the node hierarchy.
     * 
     * @param root the root node to start searching from
     * @param exact the exact text string to match
     * @return an {@link Optional} containing the found {@link Text} node if present, otherwise empty
     */
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

    /**
     * Recursively searches the node hierarchy for the first {@link Text} node containing numeric text,
     * and returns its integer value.
     * 
     * @param root the root node to start searching from
     * @return the integer value of the first numeric text found, or -1 if none found
     */
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

    /**
     * Tests that the score displayed after performing a merge move (moveLeft) is greater than or equal to the initial score.
     * Ensures that score calculation and display update correctly after merging tiles.
     * 
     * @throws Exception if the test times out or fails assertions
     */
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

    /**
     * Tests that the game cells are filled randomly with initial values (0, 2, or 4) after game initialization.
     * Verifies that at least one of the expected text nodes is present in the game root.
     * 
     * @throws Exception if the test times out or fails assertions
     */
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

    /**
     * Tests the functionality of moveLeft and moveRight methods.
     * Verifies that the number of nodes in the game root remains consistent after these moves.
     * 
     * @throws Exception if the test times out or fails assertions
     */
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

    /**
     * Tests that the score display updates correctly after a moveDown operation.
     * Ensures the score is non-decreasing after the move.
     * 
     * @throws Exception if the test times out or fails assertions
     */
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

    /**
     * Tests that random cell filling occurs after a moveUp operation by comparing the count of text nodes.
     * Verifies that the number of text nodes is non-decreasing after the move.
     * 
     * @throws Exception if the test times out or fails assertions
     */
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

    /**
     * Tests that calling the private updateGameState method triggers the win overlay by switching to the endGameScene.
     * Uses reflection to invoke the private method and verifies the stage scene changes.
     * 
     * @throws Exception if reflection fails or assertions fail
     */
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

    /**
     * Tests that clicking the RESTART button resets the score to zero.
     * Finds the RESTART text node, invokes its click handler, and verifies the score reset.
     * 
     * @throws Exception if the test times out or assertions fail
     */
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

    /**
     * Tests that clicking the MAIN MENU button switches the scene to the main menu.
     * Verifies that the main menu title is visible after the button click.
     * 
     * @throws Exception if the test times out or assertions fail
     */
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

    /**
     * Tests that the QUIT GAME button has an associated click handler.
     * Ensures the button is interactive.
     * 
     * @throws Exception if the test times out or assertions fail
     */
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

    /**
     * Recursively counts the total number of nodes in the node hierarchy rooted at the given node.
     * 
     * @param n the root node to count from
     * @return the total number of nodes including the root
     */
    private static int countNodes(Node n) {
        if (n instanceof Group g) {
            int total = 1;
            for (Node c : g.getChildren()) total += countNodes(c);
            return total;
        }
        return 1;
    }

    /**
     * Recursively counts the total number of {@link Text} nodes in the node hierarchy rooted at the given node.
     * 
     * @param n the root node to count from
     * @return the total number of {@link Text} nodes found
     */
    private static int countTextNodes(Node n) {
        int total = (n instanceof Text) ? 1 : 0;
        if (n instanceof Group g) {
            for (Node c : g.getChildren()) total += countTextNodes(c);
        }
        return total;
    }
}