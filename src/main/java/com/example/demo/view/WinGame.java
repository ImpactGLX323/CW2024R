package com.example.demo.view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class WinGame {
    private static WinGame instance;
    private WinGame() {}
    public static WinGame getInstance() {
        if (instance == null) instance = new WinGame();
        return instance;
    }

    public void winGameShow(Scene overlayScene, Group overlayRoot, Stage stage, long score,
                            Runnable onNextLevel, Runnable onRestartFromStart, Runnable onMenu) {
        overlayRoot.getChildren().clear();

        Rectangle dim = new Rectangle(900, 700);
        dim.setFill(Color.rgb(0, 0, 0, 0.4));
        overlayRoot.getChildren().add(dim);

        double panelW = 420, panelH = 260;
        double panelX = (900 - panelW) / 2.0;
        double panelY = (700 - panelH) / 2.0;

        Rectangle panel = new Rectangle(panelW, panelH);
        panel.setX(panelX);
        panel.setY(panelY);
        panel.setArcWidth(20);
        panel.setArcHeight(20);
        panel.setFill(Color.rgb(250, 248, 239));
        overlayRoot.getChildren().add(panel);

        Text title = new Text("You Win!");
        title.setFont(Font.font("Arial", 36));
        title.setFill(Color.rgb(119, 110, 101));
        title.setX(panelX + 140);
        title.setY(panelY + 50);
        overlayRoot.getChildren().add(title);

        Text scoreText = new Text("Score: " + score);
        scoreText.setFont(Font.font("Arial", 20));
        scoreText.setFill(Color.rgb(119, 110, 101));
        scoreText.setX(panelX + 160);
        scoreText.setY(panelY + 85);
        overlayRoot.getChildren().add(scoreText);

        double bw = 160, bh = 50, gap = 16;
        double bx = panelX + (panelW - bw)/2;

        Rectangle nextBtn = mkBtn(bx, panelY + 110, bw, bh, "Next Level", overlayRoot);
        Rectangle restartBtn = mkBtn(bx, panelY + 110 + bh + gap, bw, bh, "Restart from start", overlayRoot);
        Rectangle menuBtn = mkBtn(bx, panelY + 110 + 2*(bh + gap), bw, bh, "Main menu", overlayRoot);

        nextBtn.setOnMouseClicked(e -> onNextLevel.run());
        restartBtn.setOnMouseClicked(e -> onRestartFromStart.run());
        menuBtn.setOnMouseClicked(e -> onMenu.run());

        stage.setScene(overlayScene);
    }

    private Rectangle mkBtn(double x, double y, double w, double h, String label, Group root) {
        Rectangle r = new Rectangle(w, h);
        r.setX(x); r.setY(y);
        r.setArcWidth(14); r.setArcHeight(14);
        r.setFill(Color.rgb(143, 122, 102));
        root.getChildren().add(r);

        Text t = new Text(label);
        t.setFont(Font.font("Arial", 18));
        t.setFill(Color.WHITE);
        t.setX(x + (w - t.getLayoutBounds().getWidth()) / 2.0);
        t.setY(y + (h + t.getLayoutBounds().getHeight()) / 2.3);
        root.getChildren().add(t);

        t.setOnMouseClicked(e -> r.fireEvent(e));
        return r;
    }
}