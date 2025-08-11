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

/**
 * UI-level tests for the {@link EndGame} overlay.
 *
 * <p>This test class boots a minimal JavaFX platform once, then builds the
 * End Game screen into a throwaway {@link Group} and {@link Scene}.
 * It verifies the presence of key UI elements (title, score, buttons) and
 * basic interactions (e.g., clicking "RESTART" invokes its callback when the
 * 7-arg overload is available; clicking "MAIN MENU" swaps the UI to the menu).</p>
 *
 * <p><strong>Implementation note:</strong> The project currently ships two
 * overloads of {@code EndGame.endGameShow}: a 7-argument version (with callbacks)
 * and a 4-argument version (no callbacks). To keep the tests compatible with
 * either signature, we invoke the method reflectively. If the 7-arg overload is
 * present we exercise callbacks; otherwise we fall back to the 4-arg overload.</p>
 *
 * @since 1.0
 */
public class EndGameTest {

    /**
     * Starts the JavaFX runtime once for all tests.
     *
     * @throws Exception if the JavaFX platform fails to start
     */
    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX platform failed to start");
    }

    /**
     * Runs the given {@link Runnable} on the JavaFX Application Thread and
     * blocks until it completes (or times out).
     *
     * @param r work to run on the FX thread
     * @throws Exception if the task does not complete within the timeout
     */
    private static void onFx(Runnable r) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { try { r.run(); } finally { latch.countDown(); }});
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX task timed out");
    }

    // -------- reflection helpers to support both 7-arg and 4-arg overloads --------

    /**
     * Checks whether {@link EndGame} declares the 7-argument
     * {@code endGameShow(Scene, Group, Stage, long, Runnable, Runnable, Runnable)} overload.
     *
     * @return {@code true} if the 7-arg overload exists; {@code false} otherwise
     */
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

    /**
     * Invokes {@link EndGame#endGameShow} using reflection, selecting the
     * 7-argument overload when available, otherwise falling back to the
     * 4-argument overload.
     *
     * @param eg the {@link EndGame} instance
     * @param scene the target {@link Scene}
     * @param root the {@link Group} to populate
     * @param stage the {@link Stage} containing the scene
     * @param score score value to display
     * @param onRestart restart callback (ignored if 4-arg overload is used)
     * @param onMenu main menu callback (ignored if 4-arg overload is used)
     * @param onQuit quit callback (ignored if 4-arg overload is used)
     */
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

    // --------------------------- node lookup helpers ---------------------------

    /**
     * Depth-first search for a {@link Node} within {@code root} that displays
     * the given text. Supports {@link Text} and {@link Button}.
     *
     * @param root the search root
     * @param text exact text to match
     * @return the first matching node, or {@link Optional#empty()} if none found
     */
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

    /**
     * Convenience wrapper around {@link #findNodeByText(Node, String)} that
     * narrows the result to a {@link Button}.
     *
     * @param root the search root
     * @param label the button's text label
     * @return the button if found, otherwise {@link Optional#empty()}
     */
    private static Optional<Button> findButton(Group root, String label) {
        return findNodeByText(root, label).filter(n -> n instanceof Button).map(n -> (Button) n);
    }

    // ---------------------------------- tests ----------------------------------

    /**
     * Verifies the overlay builds the expected UI:
     * <ul>
     *   <li>Title text: {@code GAME OVER}</li>
     *   <li>Score label containing the provided score</li>
     *   <li>Buttons: {@code RESTART}, {@code MAIN MENU}, {@code QUIT}</li>
     * </ul>
     *
     * @throws Exception if FX thread coordination fails
     */
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

    /**
     * Verifies that clicking the {@code RESTART} button invokes the provided
     * callback, but only when the 7-argument overload is present. The test is
     * skipped automatically if only the 4-argument overload exists.
     *
     * @throws Exception if FX thread coordination fails
     */
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

    /**
     * Verifies that clicking the {@code MAIN MENU} button replaces the content
     * with the main menu UI (detected by the presence of the title
     * {@code "Crack 2048"} after the click).
     *
     * @throws Exception if FX thread coordination fails
     */
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