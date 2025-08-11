package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Tests for the Main class, focusing on scene creation and menu decoration functions.
 * These tests use JavaFX Platform initialization and reflection to access private methods.
 */
public class MainTest {

    /**
     * Tracks whether the JavaFX platform has been initialized.
     */
    private static boolean javafxStarted = false;

    /**
     * Initializes the JavaFX platform before running any tests.
     * This ensures that JavaFX components can be created and manipulated during tests.
     * 
     * @throws InterruptedException if the thread waiting for JavaFX initialization is interrupted
     */
    @BeforeAll
    public static void init() throws InterruptedException {
        if (!javafxStarted) {
            final Object lock = new Object();
            Platform.startup(() -> {
                synchronized (lock) {
                    javafxStarted = true;
                    lock.notify();
                }
            });
            synchronized (lock) {
                lock.wait();  // Wait until JavaFX initializes
            }
        }
    }

    /**
     * Tests that the createScene method correctly creates a Scene with the specified background color
     * and sets the correct root node.
     * Asserts that the Scene is not null, has the expected fill color, and the expected root.
     */
    @Test
    public void testCreateSceneWithBackgroundColor() {
        Platform.runLater(() -> {
            Main main = new Main();
            Group root = new Group();
            Scene scene = mainTest_createScene(main, root, Color.BLUE);

            assertNotNull(scene);
            assertEquals(Color.BLUE, scene.getFill());
            assertEquals(root, scene.getRoot());
        });
    }

    /**
     * Tests that the createMenuDecorations method adds the expected number of Rectangle nodes
     * to the provided Group.
     * Asserts that exactly two Rectangle nodes are added as decorations.
     */
    @Test
    public void testCreateMenuDecorationsAddsRectangles() {
        Platform.runLater(() -> {
            Main main = new Main();
            Group root = new Group();

            mainTest_createMenuDecorations(main, root);

            long count = root.getChildren().stream()
                    .filter(node -> node instanceof Rectangle)
                    .count();

            assertEquals(2, count);
        });
    }

    /**
     * Uses reflection to invoke the private createScene method of the Main class.
     * 
     * @param main the Main instance to invoke the method on
     * @param root the Group to be used as the root of the Scene
     * @param color the background Color for the Scene
     * @return the created Scene instance
     */
    private Scene mainTest_createScene(Main main, Group root, Color color) {
        try {
            var method = Main.class.getDeclaredMethod("createScene", Group.class, Color.class);
            method.setAccessible(true);
            return (Scene) method.invoke(main, root, color);
        } catch (Exception e) {
            fail("Failed to call createScene(): " + e.getMessage());
            return null;
        }
    }

    /**
     * Uses reflection to invoke the private createMenuDecorations method of the Main class.
     * This method modifies the provided Group by adding decorations.
     * 
     * @param main the Main instance to invoke the method on
     * @param root the Group to which decorations will be added
     */
    private void mainTest_createMenuDecorations(Main main, Group root) {
        try {
            var method = Main.class.getDeclaredMethod("createMenuDecorations", Group.class);
            method.setAccessible(true);
            method.invoke(main, root);
        } catch (Exception e) {
            fail("Failed to call createMenuDecorations(): " + e.getMessage());
        }
    }
}
