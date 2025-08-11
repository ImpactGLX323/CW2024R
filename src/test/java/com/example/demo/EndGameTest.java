package com.example.demo;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.demo.view.EndGame;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class EndGameTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX platform failed to start");
    }

    // --- tiny FX helper ---
    private static void onFx(Runnable r) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { try { r.run(); } finally { latch.countDown(); }});
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX task timed out");
    }

    // --- reflection helpers to support both 7-arg and 4-arg overloads ---
    private static boolean hasSevenArgEndGameShow() {
        try {
            Method m = EndGame.class.getMethod(
                "endGameShow",
                Scene.class, Group.class, Stage.class, long.class,
                Runnable.class, Runnable.class, Runnable.class
            );
            return m != null;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }

    private static void showEndGame(EndGame eg, Scene scene, Group root, Stage stage, long score,
                                    Runnable onRestart, Runnable onMenu, Runnable onQuit) {
        try {
            if (hasSevenArgEndGameShow()) {
                Method m = EndGame.class.getMethod(
                    "endGameShow",
                    Scene.class, Group.class, Stage.class, long.class,
                    Runnable.class, Runnable.class, Runnable.class
                );
                m.invoke(eg, scene, root, stage, score, onRestart, onMenu, onQuit);
            } else {
                // Fallback: 4-arg version (no callbacks)
                Method m4 = EndGame.class.getMethod(
                    "endGameShow", Scene.class, Group.class, Stage.class, long.class
                );
                m4.invoke(eg, scene, root, stage, score);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke EndGame.endGameShow reflectively", e);
        }
    }

    // --- node lookup helpers ---
    private static Optional<Node> findNodeByText(Node root, String text) {
        if (root instanceof Text t && text.equals(t.getText())) return Optional.of(t);
        if (root instanceof Button b && text.equals(b.getText())) return Optional.of(b);
        if (root instanceof Group g) {
            for (Node n : g.getChildren()) {
                var f = findNodeByText(n, text);
                if (f.isPresent()) return f;
            }
        }
        return Optional.empty();
    }

    private static Optional<Button> findButton(Group root, String label) {
        return findNodeByText(root, label).filter(n -> n instanceof Button).map(n -> (Button) n);
    }

    // --- tests ---

    @Test
    void buildsUi_withTitleScoreAndButtons() throws Exception {
        onFx(() -> {
            Group root = new Group();
            Scene scene = new Scene(root, 900, 700);
            Stage stage = new Stage(); stage.setScene(scene);

            long score = 1234L;

            showEndGame(EndGame.getInstance(), scene, root, stage, score,
                () -> {}, () -> {}, () -> {}
            );

            assertTrue(findNodeByText(root, "GAME OVER").isPresent(), "title missing");
            assertTrue(findNodeByText(root, "SCORE: " + score).isPresent(), "score text missing");
            assertTrue(findButton(root, "RESTART").isPresent(), "RESTART missing");
            assertTrue(findButton(root, "MAIN MENU").isPresent(), "MAIN MENU missing");
            assertTrue(findButton(root, "QUIT").isPresent(), "QUIT missing");
        });
    }

    @Test
    void restartButton_callsCallback_whenSevenArgOverloadAvailable() throws Exception {
        assumeTrue(hasSevenArgEndGameShow(), "Skipping: only 4-arg endGameShow() available");

        AtomicBoolean called = new AtomicBoolean(false);

        onFx(() -> {
            Group root = new Group();
            Scene scene = new Scene(root, 900, 700);
            Stage stage = new Stage(); stage.setScene(scene);

            showEndGame(EndGame.getInstance(), scene, root, stage, 42L,
                () -> called.set(true), () -> {}, () -> {}
            );

            Button restart = findButton(root, "RESTART")
                .orElseThrow(() -> new AssertionError("RESTART button missing"));
            restart.fire();
        });

        assertTrue(called.get(), "onRestart should have been invoked");
    }

    @Test
    void mainMenuButton_buildsMainMenuUi() throws Exception {
        onFx(() -> {
            Group root = new Group();
            Scene scene = new Scene(root, 900, 700);
            Stage stage = new Stage(); stage.setScene(scene);

            long score = 7L;

            showEndGame(EndGame.getInstance(), scene, root, stage, score,
                () -> {}, () -> {}, () -> {}
            );

            Button menuBtn = findButton(root, "MAIN MENU")
                .orElseThrow(() -> new AssertionError("MAIN MENU button missing"));
            menuBtn.fire();

            // After clicking MAIN MENU, your MainMenu.showMenu() should add the title:
            assertTrue(
                findNodeByText(root, "Crack 2048").isPresent(),
                "Main menu title should be present after clicking MAIN MENU"
            );
        });
    }
}