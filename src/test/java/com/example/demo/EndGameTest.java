// === EndGameTest.java ===
package com.example.demo;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.demo.view.EndGame;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class EndGameTest {

    @BeforeAll
    public static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @Test
    public void testEndGameShowDisplaysTextAndScore() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            EndGame endGame = EndGame.getInstance();
            Group root = new Group();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            long score = 2048L;
            endGame.endGameShow(scene, root, stage, score);

            // Check for "GAME OVER" text
            boolean hasGameOver = root.getChildren().stream()
                    .anyMatch(node -> node instanceof Text && ((Text) node).getText().equals("GAME OVER"));

            // Check for score text
            boolean hasScore = root.getChildren().stream()
                    .anyMatch(node -> node instanceof Text && ((Text) node).getText().equals(String.valueOf(score)));

            // Check for Quit button
            boolean hasQuitButton = root.getChildren().stream()
                    .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("QUIT"));

            assertTrue(hasGameOver);
            assertTrue(hasScore);
            assertTrue(hasQuitButton);
            latch.countDown();
        });

        latch.await();
    }
}
