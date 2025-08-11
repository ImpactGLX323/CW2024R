/**
 * Builds the Game Over screen into the given root.
 *
 * @param endGameScene scene to render into
 * @param root root Group to populate
 * @param primaryStage main window (used to switch scenes)
 * @param score final score to display
 * @param onRestart callback when user chooses "RESTART" (may be {@code null})
 * @param onMenu callback when user chooses "MAIN MENU" (may be {@code null})
 * @param onQuit callback when user chooses "QUIT" (may be {@code null})
 */

package com.example.demo.view;

import java.io.InputStream;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class EndGame {
    private static EndGame singleInstance = null;
    private EndGame() {}

    public static EndGame getInstance() {
        if (singleInstance == null) singleInstance = new EndGame();
        return singleInstance;
    }

    private static Font loadRetroFont(double size) {
        String[] candidates = {
            "/com/example/demo/fonts/Orbitron-VariableFont_wght.ttf",
            "/com/example/demo/fonts/VT323-Regular.ttf"
        };
        for (String path : candidates) {
            try (InputStream is = EndGame.class.getResourceAsStream(path)) {
                if (is != null) {
                    Font f = Font.loadFont(is, size);
                    if (f != null) return f;
                }
            } catch (Exception ignored) {}
        }
        return Font.font("Arial", size);
    }

    public void endGameShow(Scene endGameScene, Group root, Stage primaryStage, long score) {
        endGameShow(endGameScene, root, primaryStage, score,
            () -> {},   // onRestart
            () -> {},   // onMenu
            () -> javafx.application.Platform.exit()    // onQuit
        );
    }

    public void endGameShow(Scene endGameScene, Group root, Stage primaryStage, long score,
                             Runnable onRestart, Runnable onMenu, Runnable onQuit) {
        root.getChildren().clear();

        // Background
        Image bgImage = new Image(getClass().getResource("/com/example/demo/image/Crack2048.jpg").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(endGameScene.getWidth());
        bgView.setFitHeight(endGameScene.getHeight());
        bgView.setPreserveRatio(false);
        root.getChildren().add(bgView);

        // Sett dark overlay for readability
        javafx.scene.shape.Rectangle overlay = new javafx.scene.shape.Rectangle(
            endGameScene.getWidth(), endGameScene.getHeight(), Color.rgb(15, 15, 20, 0.7)
        );
        root.getChildren().add(overlay);

        // Load fonts
        Font titleFont = loadRetroFont(72);
        Font scoreFont = loadRetroFont(48);
        Font buttonFont = loadRetroFont(20);

        // Title (top center)
        Text gameOverText = new Text("GAME OVER");
        gameOverText.setFont(titleFont);
        gameOverText.setFill(Color.web("#E2E8F0"));
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00E5FF", 0.6));
        glow.setRadius(20);
        glow.setSpread(0.2);
        gameOverText.setEffect(glow);
        gameOverText.applyCss();
        double titleX = (endGameScene.getWidth() - gameOverText.getLayoutBounds().getWidth()) / 2.0;
        gameOverText.setX(Math.max(20, titleX));
        gameOverText.setY(150);
        root.getChildren().add(gameOverText);

        // Score text (middle)
        Text scoreText = new Text("SCORE: " + score);
        scoreText.setFont(scoreFont);
        scoreText.setFill(Color.web("#E2E8F0"));
        scoreText.applyCss();
        double scoreX = (endGameScene.getWidth() - scoreText.getLayoutBounds().getWidth()) / 2.0;
        scoreText.setX(Math.max(20, scoreX));
        scoreText.setY(260);
        root.getChildren().add(scoreText);

        // Button configuration
        double buttonWidth = 300;
        double buttonHeight = 60;
        double spacing = 20;
        double centerX = (endGameScene.getWidth() - buttonWidth) / 2.0;
        double startY = 350;

        // Colors
        String buttonBg = "#1a1f2b";
        String buttonBgHover = "#232a3a";
        String buttonText = "#D6E3FF";
        String accent = "#00E5FF";

        // Buttons
        Button restartButton = makeButton("RESTART", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        restartButton.relocate(centerX, startY);

        Button menuButton = makeButton("MAIN MENU", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        menuButton.relocate(centerX, startY + buttonHeight + spacing);

        Button quitButton = makeButton("QUIT", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, "#FF6B6B", buttonBgHover);
        quitButton.relocate(centerX, startY + 2 * (buttonHeight + spacing));

        root.getChildren().addAll(restartButton, menuButton, quitButton);

        // Button actions
        restartButton.setOnAction(e -> {
            root.getChildren().clear();
            if (onRestart != null) onRestart.run();
        });

        menuButton.setOnAction(e -> {
            root.getChildren().clear();
            MainMenu mainMenu = new MainMenu();
            mainMenu.showMenu(
                endGameScene,   // reuse the same scene
                root,
                primaryStage,
                () -> { if (onRestart != null) onRestart.run(); }, // when "NEW GAME" in main menu is clicked
                null,           // LOGIN action (set later if needed)
                null,           // MANUAL action (set later if needed)
                onQuit          // pass existing quit action
            );
        });

        quitButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Quit Dialog");
            alert.setHeaderText("Quit from this page");
            alert.setContentText("Are you sure?");
            var result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (onQuit != null) onQuit.run();
            }
        });
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