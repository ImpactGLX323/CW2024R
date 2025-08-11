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

/**
 * WinGame class manages the victory screen display for the game application.
 * This class implements the Singleton design pattern to ensure only one instance
 * exists throughout the application lifecycle.
 * 
 * <p>The class is responsible for creating and displaying a win game overlay
 * with styling consistent with the main menu, including retro fonts, glow effects,
 * and styled buttons for navigation options.</p>
 * 
 * @author Generated JavaDoc
 * @version 1.0
 * @since 1.0
 */
public class WinGame {
    
    /** The single instance of WinGame (Singleton pattern) */
    private static WinGame instance;
    
    /**
     * Private constructor to prevent direct instantiation.
     * Use {@link #getInstance()} to obtain the singleton instance.
     */
    private WinGame() {}
    
    /**
     * Returns the singleton instance of WinGame.
     * Creates a new instance if one doesn't exist.
     * 
     * @return the singleton WinGame instance
     */
    public static WinGame getInstance() {
        if (instance == null) instance = new WinGame();
        return instance;
    }

    /**
     * Loads a retro-style font from the application's resources.
     * Attempts to load fonts in the following priority order:
     * <ol>
     *   <li>Orbitron-VariableFont_wght.ttf</li>
     *   <li>VT323-Regular.ttf</li>
     *   <li>Arial (fallback)</li>
     * </ol>
     * 
     * @param size the desired font size in points
     * @return a Font object with the specified size, or Arial fallback if retro fonts are unavailable
     */
    private static Font loadRetroFont(double size) {
        String[] candidates = {
            "/com/example/demo/fonts/Orbitron-VariableFont_wght.ttf",
            "/com/example/demo/fonts/VT323-Regular.ttf"
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

    /**
     * Creates and displays the win game overlay screen with victory message and navigation options.
     * 
     * <p>This method constructs a complete victory screen with:
     * <ul>
     *   <li>Background image and dark overlay for visual appeal</li>
     *   <li>Glowing "YOU WIN!" title text</li>
     *   <li>Score display</li>
     *   <li>Three navigation buttons: Next Level, Restart, and Main Menu</li>
     * </ul>
     * 
     * <p>The styling is consistent with the main menu design, using retro fonts
     * and cyberpunk-inspired color schemes.</p>
     * 
     * @param overlayScene the Scene object that will contain the win screen
     * @param overlayRoot the root Group node to which UI elements will be added
     * @param stage the primary Stage for displaying the scene
     * @param score the player's final score to be displayed
     * @param onNextLevel callback function executed when "Next Level" button is clicked
     * @param onRestartFromStart callback function executed when "Restart From Start" button is clicked
     * @param onMenu callback function executed when "Main Menu" button is clicked
     */
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
        double scoreX = (scoreText.getLayoutBounds().getWidth()) / 2.0;
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

    /**
     * Creates a styled button with hover effects matching the game's visual theme.
     * 
     * <p>The button features:
     * <ul>
     *   <li>Rounded corners with border accent color</li>
     *   <li>Hover state color changes</li>
     *   <li>Hand cursor on mouse over</li>
     *   <li>Bold font styling</li>
     * </ul>
     * 
     * @param text the text to display on the button
     * @param w the button width in pixels
     * @param h the button height in pixels
     * @param font the Font to use for the button text
     * @param bg the background color in CSS format (e.g., "#1a1f2b")
     * @param fg the foreground (text) color in CSS format
     * @param borderAccent the border accent color in CSS format
     * @param hoverBg the background color when hovering in CSS format
     * @return a fully configured Button with styling and hover effects
     */
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

    /**
     * Generates the CSS style string for buttons.
     * 
     * <p>Creates a consistent button style with rounded corners, border, 
     * hand cursor, and bold text formatting.</p>
     * 
     * @param bg the background color in CSS format
     * @param fg the foreground (text) color in CSS format  
     * @param borderAccent the border color in CSS format
     * @return a CSS style string ready to apply to a button
     */
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