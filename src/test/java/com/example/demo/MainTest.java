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

public class MainTest {

    private static boolean javafxStarted = false;

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

    // === Helper methods using reflection ===

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
