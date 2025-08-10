package com.example.demo.model;

import java.io.InputStream;
import java.util.function.Consumer;

import com.example.demo.utils.TextMaker;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Cell {
    private Rectangle rectangle;
    private Group root;
    private Text textClass;
    private boolean modify = false;
    private Consumer<Integer> scoreCallback;

    // Cached font (loaded once)
    private static Font ORBITRON_REGULAR;

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

    // ---------- Font helpers ----------
    private static void ensureOrbitronLoaded(double sizeHint) {
        if (ORBITRON_REGULAR == null) {
            String path = "/com/example/demo/fonts/Orbitron-VariableFont_wght.ttf";
            try (InputStream is = Cell.class.getResourceAsStream(path)) {
                if (is != null) {
                    // load with size hint; we’ll override actual size per-cell anyway
                    ORBITRON_REGULAR = Font.loadFont(is, sizeHint > 0 ? sizeHint : 24);
                }
            } catch (Exception ignored) {}
            if (ORBITRON_REGULAR == null) {
                ORBITRON_REGULAR = Font.font("Arial", sizeHint > 0 ? sizeHint : 24);
            }
        }
    }

    private void applyOrbitronFont(Text t, double scale) {
        // Size the number relative to cell size (comfortable & legible)
        double size = Math.max(14, scale * 0.45);
        ensureOrbitronLoaded(size);
        t.setFont(Font.font(ORBITRON_REGULAR.getFamily(), size));
        // Default color; we’ll adjust per number in setColorByNumber
        t.setFill(Color.rgb(30, 35, 45, 0.92));
    }

    // ---------- External API ----------
    public void setScoreCallback(Consumer<Integer> callback) {
        this.scoreCallback = callback;
    }

    public void setModify(boolean modify) {
        this.modify = modify;
    }

    public boolean getModify() {
        return modify;
    }

    public void setTextClass(Text textClass) {
        this.textClass = textClass;
        // Make sure externally injected texts also use our font
        double scale = rectangle.getWidth();
        applyOrbitronFont(this.textClass, scale);
        // Refresh color for consistency
        setColorByNumber(getNumber());
    }

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

    // ---------- Visual styling ----------
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

    // ---------- Getters ----------
    public double getX() {
        return rectangle.getX();
    }

    public double getY() {
        return rectangle.getY();
    }

    public int getNumber() {
        return Integer.parseInt(textClass.getText());
    }

    private Text getTextClass() {
        return textClass;
    }
}