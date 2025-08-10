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
import javafx.scene.shape.Rectangle;
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
        stopBackgroundMedia();

        root.getChildren().clear();
        root.setStyle("-fx-background-color: #0f0f14;");
        if (menuScene.getRoot() != root) {
            menuScene.getRoot().setStyle("-fx-background-color: #0f0f14;");
        }

        // 1) Try video background
        boolean videoAdded = tryAddVideoBackground(menuScene, root,
                "/com/example/demo/image/MainMenu.mp4"); // <-- place your mp4 here

        // 1.5) Dim overlay ABOVE video, BELOW UI for contrast
        Rectangle dim = new Rectangle();
        dim.widthProperty().bind(menuScene.widthProperty());
        dim.heightProperty().bind(menuScene.heightProperty());
        dim.setFill(Color.color(0, 0, 0, 0.35));
        root.getChildren().add(dim);

        // 2) Fallback image if video missing/failed
        if (!videoAdded) {
            URL imgUrl = getClass().getResource("/com/example/demo/image/Crack2048.jpg");
            if (imgUrl != null) {
                Image bgImage = new Image(imgUrl.toExternalForm());
                ImageView bgView = new ImageView(bgImage);
                bgView.fitWidthProperty().bind(menuScene.widthProperty());
                bgView.fitHeightProperty().bind(menuScene.heightProperty());
                bgView.setPreserveRatio(false);
                root.getChildren().add(0, bgView); // under dim
            }
        }

        // 3) Title (neon glow)
        Font titleFont = loadRetroFont(72);
        Text title = new Text("Crack 2048");
        title.setFont(titleFont);
        title.setFill(Color.web("#E2E8F0"));
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00E5FF", 0.6));
        glow.setRadius(20);
        glow.setSpread(0.2);
        title.setEffect(glow);
        root.getChildren().add(title);

        // Center title now + on resize
        Runnable layoutTitle = () -> {
            title.applyCss();
            double titleX = Math.max(20, (menuScene.getWidth() - title.getLayoutBounds().getWidth()) / 2.0);
            title.setX(titleX);
            title.setY(150);
        };
        layoutTitle.run();
        title.layoutBoundsProperty().addListener((obs, o, n) -> layoutTitle.run());
        menuScene.widthProperty().addListener((o, a, b) -> layoutTitle.run());
        menuScene.heightProperty().addListener((o, a, b) -> layoutTitle.run());

        // 4) Buttons
        Font buttonFont = loadRetroFont(20);
        double buttonWidth = 300;
        double buttonHeight = 60;
        double spacing = 20;

        String buttonBg      = "#1a1f2b";
        String buttonBgHover = "#232a3a";
        String buttonText    = "#D6E3FF";
        String accent        = "#00E5FF";

        Button newGameButton = makeButton("NEW GAME", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        Button loginButton   = makeButton("LOGIN", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        Button manualButton  = makeButton("GAME MANUAL", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, accent, buttonBgHover);
        Button quitButton    = makeButton("QUIT GAME", buttonWidth, buttonHeight, buttonFont, buttonBg, buttonText, "#FF6B6B", buttonBgHover);

        root.getChildren().addAll(newGameButton, loginButton, manualButton, quitButton);

        Runnable layoutButtons = () -> {
            double centerX = (menuScene.getWidth() - buttonWidth) / 2.0;
            double startY  = 300;
            newGameButton.relocate(centerX, startY);
            loginButton.relocate(centerX, startY + buttonHeight + spacing);
            manualButton.relocate(centerX, startY + 2 * (buttonHeight + spacing));
            quitButton.relocate(centerX, startY + 3 * (buttonHeight + spacing));
        };
        layoutButtons.run();
        menuScene.widthProperty().addListener((o, a, b) -> layoutButtons.run());
        menuScene.heightProperty().addListener((o, a, b) -> layoutButtons.run());

        // Make sure UI is above dim/video
        title.toFront();
        newGameButton.toFront();
        loginButton.toFront();
        manualButton.toFront();
        quitButton.toFront();

        // Actions
        newGameButton.setOnAction(e -> { stopBackgroundMedia(); if (onNewGame != null) onNewGame.run(); });
        loginButton.setOnAction(e -> { stopBackgroundMedia(); if (onLogin != null) onLogin.run(); });
        manualButton.setOnAction(e -> { if (onManual != null) onManual.run(); }); // keep video if overlay
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
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);

            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setMouseTransparent(true);            // never steal clicks
            mediaView.setPreserveRatio(false);
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());

            root.getChildren().add(0, mediaView); // bottom layer
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