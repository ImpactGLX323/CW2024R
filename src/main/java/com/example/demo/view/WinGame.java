package com.example.demo.view;

import java.io.InputStream;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
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

    // Match MainMenu's font loader
    private static Font loadRetroFont(double size) {
        String[] candidates = {
            "/fonts/Orbitron-VariableFont_wght.ttf",
            "/fonts/VT323-Regular.ttf"
        };
        for (String path : candidates) {
            try (InputStream is = WinGame.class.getResourceAsStream(path)) {
                if (is != null) {
                    Font f = Font.loadFont(is, size);
                    if (f != null) return f;
                }
            } catch (Exception ignored) {}
        }
        return Font.font("Arial", size);
    }

    public void winGameShow(Scene overlayScene, Group overlayRoot, Stage stage, long score,
                            Runnable onNextLevel, Runnable onRestartFromStart, Runnable onMenu) {

        // Clear and set a dark base color like MainMenu
        overlayRoot.getChildren().clear();
        overlayRoot.setStyle("-fx-background-color: #0f0f14;");

        // Dimensions (fallbacks if scene isn't laid out yet)
        double W = overlayScene != null && overlayScene.getWidth()  > 0 ? overlayScene.getWidth()  : 900;
        double H = overlayScene != null && overlayScene.getHeight() > 0 ? overlayScene.getHeight() : 700;

        // Background image (same root as MainMenu)
        var url = getClass().getResource("/com/example/demo/image/wingame.png");
        if (url != null) {
            ImageView bg = new ImageView(new Image(url.toExternalForm()));
            bg.setFitWidth(W);
            bg.setFitHeight(H);
            bg.setPreserveRatio(false);
            overlayRoot.getChildren().add(bg);
        }

        // Subtle dark overlay for contrast
        javafx.scene.shape.Rectangle dim = new javafx.scene.shape.Rectangle(W, H);
        dim.setFill(Color.color(0, 0, 0, 0.35));
        overlayRoot.getChildren().add(dim);

        // Title with glow (consistent with MainMenu)
        Font titleFont = loadRetroFont(56);
        Text title = new Text("YOU WIN!");
        title.setFont(titleFont);
        title.setFill(Color.web("#E2E8F0"));

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00E5FF", 0.6));
        glow.setRadius(20);
        glow.setSpread(0.2);
        title.setEffect(glow);

        title.applyCss();
        double titleX = (W - title.getLayoutBounds().getWidth()) / 2.0;
        title.setX(Math.max(20, titleX));
        title.setY(170);
        overlayRoot.getChildren().add(title);

        // Score text
        Font scoreFont = loadRetroFont(22);
        Text scoreText = new Text("Score: " + score);
        scoreText.setFont(scoreFont);
        scoreText.setFill(Color.web("#D6E3FF"));
        scoreText.applyCss();
        double scoreX = (W - scoreText.getLayoutBounds().getWidth()) / 2.0;
        scoreText.setX(Math.max(20, scoreX));
        scoreText.setY(210);
        overlayRoot.getChildren().add(scoreText);

        // Buttons (same style as MainMenu)
        Font buttonFont = loadRetroFont(20);
        double buttonWidth = 300;
        double buttonHeight = 60;
        double spacing = 20;
        double centerX = (W - buttonWidth) / 2.0;
        double startY  = 280;

        String buttonBg      = "#1a1f2b";
        String buttonBgHover = "#232a3a";
        String buttonTextCol = "#D6E3FF";
        String accent        = "#00E5FF";
        String dangerAccent  = "#FF6B6B";

        Button nextLevelBtn = makeButton("NEXT LEVEL", buttonWidth, buttonHeight, buttonFont,
                                         buttonBg, buttonTextCol, accent, buttonBgHover);
        nextLevelBtn.relocate(centerX, startY);

        Button restartBtn = makeButton("RESTART FROM START", buttonWidth, buttonHeight, buttonFont,
                                       buttonBg, buttonTextCol, accent, buttonBgHover);
        restartBtn.relocate(centerX, startY + buttonHeight + spacing);

        Button mainMenuBtn = makeButton("MAIN MENU", buttonWidth, buttonHeight, buttonFont,
                                        buttonBg, buttonTextCol, dangerAccent, buttonBgHover);
        mainMenuBtn.relocate(centerX, startY + 2 * (buttonHeight + spacing));

        overlayRoot.getChildren().addAll(nextLevelBtn, restartBtn, mainMenuBtn);

        // Wire actions (no extra logic here—just delegate)
        nextLevelBtn.setOnAction(e -> { if (onNextLevel != null) onNextLevel.run(); });
        restartBtn.setOnAction(e -> { if (onRestartFromStart != null) onRestartFromStart.run(); });
        mainMenuBtn.setOnAction(e -> { if (onMenu != null) onMenu.run(); });

        // Show
        stage.setScene(overlayScene);
    }

    private Button makeButton(String text,
                              double w, double h, Font font,
                              String bg, String fg, String borderAccent, String hoverBg) {
        Button b = new Button(text);
        b.setPrefSize(w, h);
        b.setFont(font);
        b.setStyle(baseButtonStyle(bg, fg, borderAccent));
        b.setOnMouseEntered(ev -> b.setStyle(baseButtonStyle(hoverBg, fg, borderAccent)));
        b.setOnMouseExited(ev -> b.setStyle(baseButtonStyle(bg, fg, borderAccent)));
        return b;
    }

    private String baseButtonStyle(String bg, String fg, String borderAccent) {
        return String.join("",
            "-fx-background-color: ", bg, ";",
            "-fx-text-fill: ", fg, ";",
            "-fx-background-radius: 14;",
            "-fx-border-radius: 14;",
            "-fx-border-width: 2;",
            "-fx-border-color: ", borderAccent, ";",
            "-fx-cursor: hand;",
            "-fx-font-weight: bold;"
        );
    }
}