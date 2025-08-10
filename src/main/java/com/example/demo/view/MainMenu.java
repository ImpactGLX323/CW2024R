package com.example.demo.view;

import java.io.InputStream;
import java.net.URL;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainMenu {

    // Keep a reference so we can stop/dispose when leaving the menu
    private MediaPlayer mediaPlayer;

    private static Font loadRetroFont(double size) {
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
        return Font.font("Arial", size);
    }

    public void showMenu(
            Scene menuScene, Group root, Stage primaryStage,
            Runnable onNewGame, Runnable onLogin, Runnable onManual, Runnable onQuit
    ) {
        // Stop any previous media if showMenu is called again
        stopBackgroundMedia();

        root.getChildren().clear();
        root.setStyle("-fx-background-color: #0f0f14;");
        if (menuScene.getRoot() != root) {
            menuScene.getRoot().setStyle("-fx-background-color: #0f0f14;");
        }

        // --- Try video background first ---
        boolean videoAdded = tryAddVideoBackground(menuScene, root,
                "/com/example/demo/image/MainMenu.mp4"); // <-- put your video here

        // --- Fallback: static image if video missing or failed ---
        if (!videoAdded) {
            URL imgUrl = getClass().getResource("/com/example/demo/image/Crack2048.jpg");
            if (imgUrl != null) {
                Image bgImage = new Image(imgUrl.toExternalForm());
                ImageView bgView = new ImageView(bgImage);
                bgView.fitWidthProperty().bind(menuScene.widthProperty());
                bgView.fitHeightProperty().bind(menuScene.heightProperty());
                bgView.setPreserveRatio(false);
                root.getChildren().add(bgView);
            }
        }

        // --- Title (neon glow) ---
        Font titleFont = loadRetroFont(72);
        Text title = new Text("Crack 2048");
        title.setFont(titleFont);
        title.setFill(Color.web("#E2E8F0"));
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00E5FF", 0.6));
        glow.setRadius(20);
        glow.setSpread(0.2);
        title.setEffect(glow);

        // Center title
        root.getChildren().add(title);
        title.layoutBoundsProperty().addListener((obs, o, n) -> {
            double titleX = (menuScene.getWidth() - n.getWidth()) / 2.0;
            title.setX(Math.max(20, titleX));
            title.setY(150);
        });

        // --- Buttons (same style as before) ---
        Font buttonFont = loadRetroFont(20);
        double buttonWidth = 300;
        double buttonHeight = 60;
        double spacing = 20;

        String buttonBg = "#1a1f2b";
        String buttonBgHover = "#232a3a";
        String buttonText = "#D6E3FF";
        String accent = "#00E5FF";

        Button newGameButton = makeButton("NEW GAME", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        Button loginButton   = makeButton("LOGIN", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        Button manualButton  = makeButton("GAME MANUAL", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        Button quitButton    = makeButton("QUIT GAME", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, "#FF6B6B", buttonBgHover);

        root.getChildren().addAll(newGameButton, loginButton, manualButton, quitButton);

        // Center buttons after scene has size
        Runnable layoutButtons = () -> {
            double centerX = (menuScene.getWidth() - buttonWidth) / 2.0;
            double startY  = 300;
            newGameButton.relocate(centerX, startY);
            loginButton.relocate(centerX, startY + buttonHeight + spacing);
            manualButton.relocate(centerX, startY + 2 * (buttonHeight + spacing));
            quitButton.relocate(centerX, startY + 3 * (buttonHeight + spacing));
        };
        // Initial layout + update on resize
        layoutButtons.run();
        menuScene.widthProperty().addListener((o, a, b) -> layoutButtons.run());
        menuScene.heightProperty().addListener((o, a, b) -> layoutButtons.run());

        // Actions (stop video before leaving)
        newGameButton.setOnAction(e -> { stopBackgroundMedia(); if (onNewGame != null) onNewGame.run(); });
        loginButton.setOnAction(e -> { stopBackgroundMedia(); if (onLogin != null) onLogin.run(); });
        manualButton.setOnAction(e -> { /* keep video playing if manual is overlay; otherwise stop */ if (onManual != null) onManual.run(); });
        quitButton.setOnAction(e -> { stopBackgroundMedia(); if (onQuit != null) onQuit.run(); });
    }

    private boolean tryAddVideoBackground(Scene scene, Group root, String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("[MainMenu] Video not found at " + resourcePath);
                return false;
            }
            Media media = new Media(url.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // loop
            mediaPlayer.setMute(true); // background ambiance

            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());
            mediaView.setPreserveRatio(false);

            // Put behind everything else
            root.getChildren().add(0, mediaView);
            return true;
        } catch (MediaException ex) {
            System.err.println("[MainMenu] Failed to load/play video: " + ex);
            return false;
        } catch (Throwable t) {
            System.err.println("[MainMenu] Unexpected error loading video: " + t);
            return false;
        }
    }

    private void stopBackgroundMedia() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
        } catch (Throwable ignored) {
        } finally {
            mediaPlayer = null;
        }
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