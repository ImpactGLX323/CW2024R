package com.example.demo.view;

import java.io.InputStream;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainMenu {

    private static Font loadRetroFont(double size) {
        // Try Orbitron first, then VT323, else fallback
        String[] candidates = {
            "/fonts/Orbitron-VariableFont_wght.ttf",
            "/fonts/VT323-Regular.ttf"
        };
        for (String path : candidates) {
            try (InputStream is = MainMenu.class.getResourceAsStream(path)) {
                if (is != null) {
                    Font f = Font.loadFont(is, size);
                    if (f != null) return f;
                }
            } catch (Exception ignored) {}
        }
        return Font.font("Arial", size); // fallback
    }

    public void showMenu(
            Scene menuScene, Group root, Stage primaryStage,
            Runnable onNewGame, Runnable onLogin, Runnable onManual, Runnable onQuit
    ) {
        root.getChildren().clear();

        // Dark, minimal background
        root.setStyle("-fx-background-color: #0f0f14;"); // deep charcoal
        // If the Group is not the scene root, also set on scene root:
        if (menuScene.getRoot() != root) {
            menuScene.getRoot().setStyle("-fx-background-color: #0f0f14;");
        }

        // Load fonts
        Font titleFont = loadRetroFont(72);
        Font buttonFont = loadRetroFont(20);

        // Title (top center)
        Text title = new Text("Crack 2048");
        title.setFont(titleFont);
        title.setFill(Color.web("#E2E8F0")); // soft light
        // subtle neon-ish glow
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00E5FF", 0.6)); // cyan glow
        glow.setRadius(20);
        glow.setSpread(0.2);
        title.setEffect(glow);

        // We need layout bounds to center; force CSS/apply once text is measured
        title.applyCss();
        double titleX = (menuScene.getWidth() - title.getLayoutBounds().getWidth()) / 2.0;
        title.setX(Math.max(20, titleX));
        title.setY(150);
        root.getChildren().add(title);

        // Button configuration
        double buttonWidth = 300;
        double buttonHeight = 60;
        double spacing = 20;
        double centerX = (menuScene.getWidth() - buttonWidth) / 2.0;
        double startY = 300;

        // Colors
        String buttonBg = "#1a1f2b";  // dark card
        String buttonBgHover = "#232a3a";
        String buttonText = "#D6E3FF"; // soft bluish white
        String accent = "#00E5FF";    // neon cyan border

        // Factory
        Button newGameButton = makeButton("NEW GAME", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        newGameButton.relocate(centerX, startY);

        Button loginButton = makeButton("LOGIN", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        loginButton.relocate(centerX, startY + buttonHeight + spacing);

        Button manualButton = makeButton("GAME MANUAL", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        manualButton.relocate(centerX, startY + 2 * (buttonHeight + spacing));

        Button quitButton = makeButton("QUIT GAME", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, "#FF6B6B", buttonBgHover);
        quitButton.relocate(centerX, startY + 3 * (buttonHeight + spacing));

        root.getChildren().addAll(newGameButton, loginButton, manualButton, quitButton);

        // Actions
        newGameButton.setOnAction(e -> { if (onNewGame != null) onNewGame.run(); });
        loginButton.setOnAction(e -> { if (onLogin != null) onLogin.run(); });
        manualButton.setOnAction(e -> { if (onManual != null) onManual.run(); });
        quitButton.setOnAction(e -> { if (onQuit != null) onQuit.run(); });
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
