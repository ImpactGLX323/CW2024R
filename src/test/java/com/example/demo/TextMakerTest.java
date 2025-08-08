// === TextMakerTest.java ===
package com.example.demo;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.example.demo.utils.TextMaker;
import com.example.demo.view.GameScene;


import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class TextMakerTest {

    @BeforeAll
    public static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @Test
    public void testMadeTextCreatesCorrectText() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            Group root = new Group();
            TextMaker maker = TextMaker.getSingleInstance();

            // Manually set LENGTH if necessary in GameScene
            GameScene.setCellLength(175); // Optional: Set static length if it’s used

            Text result = maker.madeText("64", 100, 100, root);

            assertNotNull(result);
            assertEquals("64", result.getText());
            assertTrue(result.getFont().getSize() > 0);
            assertEquals(javafx.scene.paint.Color.WHITE, result.getFill());

            latch.countDown();
        });

        latch.await();
    }

    @Test
    public void testChangeTwoTextSwapsCorrectly() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            Text a = new Text("2");
            Text b = new Text("4");
            a.setX(10);
            a.setY(20);
            b.setX(100);
            b.setY(200);

            TextMaker.changeTwoText(a, b);

            assertEquals("4", a.getText());
            assertEquals("2", b.getText());
            assertEquals(100, a.getX());
            assertEquals(10, b.getX());
            assertEquals(200, a.getY());
            assertEquals(20, b.getY());

            latch.countDown();
        });

        latch.await();
    }
}
