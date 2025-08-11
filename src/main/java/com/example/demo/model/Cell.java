package com.example.demo.model;

import java.io.InputStream;
import java.util.function.Consumer;

import com.example.demo.utils.TextMaker;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Cell represents an individual tile in the 2048 game grid. Each cell contains a 
 * visual rectangle and text element that displays the cell's numeric value.
 * 
 * <p>The Cell class is responsible for:
 * <ul>
 *   <li>Visual representation of game tiles with rounded corners and subtle styling</li>
 *   <li>Dynamic color schemes that change based on tile values</li>
 *   <li>Text rendering with custom Orbitron font for a modern appearance</li>
 *   <li>Cell merging operations during gameplay</li>
 *   <li>Score callback functionality for tracking game progress</li>
 * </ul>
 * 
 * <p>The visual design uses a semi-transparent color palette that allows background
 * images to show through while maintaining good contrast for readability. Higher
 * numbered tiles use warmer colors and automatically adjust text color for optimal
 * contrast.</p>
 * 
 * @author Generated JavaDoc
 * @version 1.0
 * @since 1.0
 */
public class Cell {
    
    /** The rectangular visual representation of the cell */
    private Rectangle rectangle;
    
    /** The root Group node containing this cell's visual elements */
    private Group root;
    
    /** The Text element displaying the cell's numeric value */
    private Text textClass;
    
    /** Flag indicating whether this cell has been modified in the current game turn */
    private boolean modify = false;
    
    /** Callback function executed when cells merge to update the game score */
    private Consumer<Integer> scoreCallback;

    /** Cached Orbitron font instance to avoid repeated loading from resources */
    private static Font ORBITRON_REGULAR;

    /**
     * Creates a new Cell with specified position, size, and parent container.
     * 
     * <p>The cell is initialized with:
     * <ul>
     *   <li>A rounded rectangle with subtle styling and transparency</li>
     *   <li>Default value of "0" using the Orbitron font</li>
     *   <li>Soft highlight border and card-like appearance</li>
     * </ul>
     * 
     * @param x the x-coordinate position of the cell in pixels
     * @param y the y-coordinate position of the cell in pixels
     * @param scale the width and height dimensions of the cell in pixels
     * @param root the parent Group node to which this cell will be added
     */
    public Cell(double x, double y, double scale, Group root) {
        rectangle = new Rectangle();
        rectangle.setX(x);
        rectangle.setY(y);
        rectangle.setHeight(scale);
        rectangle.setWidth(scale);
        this.root = root;

        // Subtle card style
        rectangle.setArcWidth(14);
        rectangle.setArcHeight(14);
        rectangle.setFill(Color.rgb(224, 226, 226, 0.45)); // neutral light, semi-transparent
        rectangle.setStroke(Color.rgb(255, 255, 255, 0.12)); // soft highlight
        rectangle.setStrokeWidth(1.25);

        // Create text with TextMaker then apply our Orbitron font & styling
        this.textClass = TextMaker.getSingleInstance().madeText("0", x, y, root);
        applyOrbitronFont(this.textClass, scale);

        root.getChildren().add(rectangle);
    }

    /**
     * Ensures the Orbitron font is loaded and cached for use across all cells.
     * Attempts to load the custom font from resources, falling back to Arial if unavailable.
     * 
     * <p>This method uses lazy initialization and caching to avoid repeated font loading
     * operations, improving performance when multiple cells are created.</p>
     * 
     * @param sizeHint the suggested font size for initial loading, used as fallback size
     */
    private static void ensureOrbitronLoaded(double sizeHint) {
        if (ORBITRON_REGULAR == null) {
            String path = "/com/example/demo/fonts/Orbitron-VariableFont_wght.ttf";
            try (InputStream is = Cell.class.getResourceAsStream(path)) {
                if (is != null) {
                    // load with size hint; we'll override actual size per-cell anyway
                    ORBITRON_REGULAR = Font.loadFont(is, sizeHint > 0 ? sizeHint : 24);
                }
            } catch (Exception ignored) {}
            if (ORBITRON_REGULAR == null) {
                ORBITRON_REGULAR = Font.font("Arial", sizeHint > 0 ? sizeHint : 24);
            }
        }
    }

    /**
     * Applies the Orbitron font to a text element with size scaled relative to cell dimensions.
     * 
     * <p>The font size is calculated as 45% of the cell scale, with a minimum size of 14
     * pixels to ensure readability. The text color is set to a default dark color that
     * will be adjusted by {@link #setColorByNumber(int)} based on the cell's value.</p>
     * 
     * @param t the Text element to which the font will be applied
     * @param scale the cell scale used to calculate appropriate font size
     */
    private void applyOrbitronFont(Text t, double scale) {
        // Size the number relative to cell size (comfortable & legible)
        double size = Math.max(14, scale * 0.45);
        ensureOrbitronLoaded(size);
        t.setFont(Font.font(ORBITRON_REGULAR.getFamily(), size));
        // Default color; we'll adjust per number in setColorByNumber
        t.setFill(Color.rgb(30, 35, 45, 0.92));
    }

    /**
     * Sets the callback function to be executed when cells merge during gameplay.
     * The callback receives the merged cell's value for score tracking purposes.
     * 
     * @param callback a Consumer function that accepts the merged cell value as an Integer
     */
    public void setScoreCallback(Consumer<Integer> callback) {
        this.scoreCallback = callback;
    }

    /**
     * Sets the modification flag for this cell, typically used to track changes
     * during a single game turn to prevent multiple modifications.
     * 
     * @param modify true if the cell has been modified this turn, false otherwise
     */
    public void setModify(boolean modify) {
        this.modify = modify;
    }

    /**
     * Returns the current modification status of this cell.
     * 
     * @return true if the cell has been modified this turn, false otherwise
     */
    public boolean getModify() {
        return modify;
    }

    /**
     * Sets a new Text element for this cell and applies consistent styling.
     * 
     * <p>When a new text element is assigned, this method ensures it uses the
     * Orbitron font and has the appropriate color for its numeric value.</p>
     * 
     * @param textClass the new Text element to be used for displaying the cell's value
     */
    public void setTextClass(Text textClass) {
        this.textClass = textClass;
        // Make sure externally injected texts also use our font
        double scale = rectangle.getWidth();
        applyOrbitronFont(this.textClass, scale);
        // Refresh color for consistency
        setColorByNumber(getNumber());
    }

    /**
     * Exchanges the contents and visual elements between this cell and another cell.
     * 
     * <p>This method handles the complete swap of cell contents including:
     * <ul>
     *   <li>Text content exchange using TextMaker utilities</li>
     *   <li>Removal and re-addition of visual elements from the scene graph</li>
     *   <li>Color updates for both cells based on their new values</li>
     *   <li>Proper handling of empty cells (value "0")</li>
     * </ul>
     * 
     * @param cell the other Cell with which to exchange contents
     */
    public void changeCell(Cell cell) {
        TextMaker.changeTwoText(textClass, cell.getTextClass());
        root.getChildren().remove(cell.getTextClass());
        root.getChildren().remove(textClass);

        if (!cell.getTextClass().getText().equals("0")) {
            root.getChildren().add(cell.getTextClass());
        }
        if (!textClass.getText().equals("0")) {
            root.getChildren().add(textClass);
        }
        setColorByNumber(getNumber());
        cell.setColorByNumber(cell.getNumber());
    }

    /**
     * Merges this cell with another cell by adding their values together.
     * 
     * <p>The merge operation:
     * <ul>
     *   <li>Adds the values of both cells</li>
     *   <li>Updates the target cell with the sum</li>
     *   <li>Sets this cell's value to "0" (empty)</li>
     *   <li>Updates visual styling for both cells</li>
     *   <li>Triggers the score callback with the merged value</li>
     *   <li>Removes this cell's text from the scene graph</li>
     * </ul>
     * 
     * @param cell the target cell that will receive the merged value
     */
    public void adder(Cell cell) {
        int mergedValue = cell.getNumber() + this.getNumber();
        cell.getTextClass().setText(mergedValue + "");
        textClass.setText("0");
        root.getChildren().remove(textClass);
        cell.setColorByNumber(cell.getNumber());
        setColorByNumber(getNumber());
        if (scoreCallback != null) {
            scoreCallback.accept(mergedValue);
        }
    }

    /**
     * Sets the visual appearance of the cell based on its numeric value.
     * 
     * <p>This method implements a sophisticated color scheme that:
     * <ul>
     *   <li>Uses a cool-to-warm color transition as values increase</li>
     *   <li>Maintains semi-transparency to allow background images to show through</li>
     *   <li>Automatically adjusts text color for optimal contrast</li>
     *   <li>Handles values from 0 (empty) up to 16384 and beyond</li>
     * </ul>
     * 
     * <p>Color progression:
     * <ul>
     *   <li>0-16: Cool blues and cyans</li>
     *   <li>32-64: Cool greens</li>
     *   <li>128-512: Warm ambers and oranges</li>
     *   <li>1024+: Hot reds and magentas</li>
     * </ul>
     * 
     * @param number the numeric value that determines the cell's visual appearance
     */
    public void setColorByNumber(int number) {
        // Palette: cool-to-warm ramp, all semi-transparent so the bg image peeks through.
        // Also flips text color to light on darker tiles for contrast.
        Color tileColor;
        Color textColor;

        switch (number) {
            case 0 -> tileColor = Color.rgb(224, 226, 226, 0.45);           // empty
            case 2 -> tileColor = Color.rgb(210, 240, 255, 0.55);            // pale cyan
            case 4 -> tileColor = Color.rgb(190, 225, 255, 0.58);
            case 8 -> tileColor = Color.rgb(170, 210, 255, 0.62);
            case 16 -> tileColor = Color.rgb(155, 195, 255, 0.66);
            case 32 -> tileColor = Color.rgb(200, 235, 200, 0.62);           // mint
            case 64 -> tileColor = Color.rgb(175, 225, 180, 0.66);
            case 128 -> tileColor = Color.rgb(255, 235, 170, 0.68);          // soft amber
            case 256 -> tileColor = Color.rgb(255, 220, 140, 0.72);
            case 512 -> tileColor = Color.rgb(255, 200, 120, 0.75);
            case 1024 -> tileColor = Color.rgb(255, 170, 110, 0.78);
            case 2048 -> tileColor = Color.rgb(255, 145, 100, 0.82);
            case 4096 -> tileColor = Color.rgb(255, 120, 95, 0.85);
            case 8192 -> tileColor = Color.rgb(255, 95, 90, 0.88);
            case 16384 -> tileColor = Color.rgb(255, 70, 85, 0.90);
            default -> {
                // For bigger numbers, keep deepening slightly
                double alpha = Math.min(0.92, 0.80 + 0.02 * Math.log(number) / Math.log(2));
                tileColor = Color.color(0.95, 0.25, 0.30, alpha);
            }
        }

        rectangle.setFill(tileColor);

        // Text contrast: dark for light tiles; light for mid/dark tiles
        // Heuristic: if alpha or warmth is high, prefer light text
        boolean useLightText =
                number >= 128 || tileColor.getOpacity() >= 0.75 || tileColor.getRed() > 0.9;
        textColor = useLightText
                ? Color.rgb(250, 252, 255, 0.95)
                : Color.rgb(30, 35, 45, 0.92);

        if (textClass != null) {
            textClass.setFill(textColor);
        }
    }

    /**
     * Returns the x-coordinate of this cell's rectangle.
     * 
     * @return the x-coordinate position in pixels
     */
    public double getX() {
        return rectangle.getX();
    }

    /**
     * Returns the y-coordinate of this cell's rectangle.
     * 
     * @return the y-coordinate position in pixels
     */
    public double getY() {
        return rectangle.getY();
    }

    /**
     * Returns the numeric value displayed in this cell.
     * 
     * @return the cell's numeric value, or 0 if the cell is empty
     * @throws NumberFormatException if the text content cannot be parsed as an integer
     */
    public int getNumber() {
        return Integer.parseInt(textClass.getText());
    }

    /**
     * Returns the Text element used to display the cell's value.
     * This method is package-private to support internal cell operations.
     * 
     * @return the Text element containing the cell's numeric display
     */
    private Text getTextClass() {
        return textClass;
    }
}