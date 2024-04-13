package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.Shadow;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.rgb(228, 37, 24),
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    GraphicsContext gc = getGraphicsContext2D();

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
//            this.setOnMouseEntered(event -> {
//                        hover();});
//            this.setOnMouseExited(event -> {
//                exited();});
//    this.setOnMouseEntered(
//        event -> {
//          paintColor(COLOURS[value.get()]);
//          System.out.println("enetred");
//        });
//    this.setOnMouseExited(
//        event -> {
//          paintColor(COLOURS[value.get()]);
//          System.out.println("Exited");
//        });
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
    }

    /**
     * Paint this canvas empty
     */

    private void paintEmpty() {

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.38));
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.GREY);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {

        //Clear
        gc.clearRect(0,0,width,height);

    // Colour fill
    if (!(colour.equals(Color.TRANSPARENT))) {
      gc.setFill(colour);
      // gc.setFill(Color.LIGHTBLUE);
      gc.fillRect(0, 0, width, height);
        }else{
        paintEmpty();
    }

        //Border
        gc.setStroke(Color.GREY);
        gc.strokeRect(0,0,width,height);
    }

    private void paintHoveredColor(Paint colour) {


        //Clear
        gc.clearRect(0,0,width,height);

        // Colour fill
        if(colour.equals(Color.TRANSPARENT)){
            gc.setFill(Color.WHITE.deriveColor(0,1,1,0.38));
        }else{
      Color color = (Color) colour;
      gc.setFill(color.deriveColor(0, 0.3, 1, 1));
    }

    //        gc.setFill(Color.LIGHTBLUE);
    gc.fillRect(0, 0, width, height);

        //Border
        gc.setStroke(Color.GREY);
        gc.strokeRect(0,0,width,height);
    }



    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    public void hover(){
        paintHoveredColor(COLOURS[value.get()]);
    }

    public void exited(){paintColor(COLOURS[value.get()]);
    }

    public void fadeOut() {

        AnimationTimer timer = new AnimationTimer() {

            private long startTime = -1;
            private final long duration = 1000000000L; // 1 second in nanoseconds

            @Override
            public void handle(long now) {
                if (startTime < 0) {
                    startTime = now;
                }

                long elapsed = now - startTime;
                if (elapsed >= duration) {
                    stop(); // Stop the animation when duration is reached
                } else {
                    double opacity = 1 - (double) elapsed / duration;
                    gc.clearRect(0, 0, width, height);
                    gc.setFill(Color.rgb(0, 255, 0, opacity));
                    gc.fillRect(0, 0, width, height);
                }
                gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.21));
                gc.fillRect(0,0, width, height);
                gc.setStroke(Color.GREY);
                gc.strokeRect(0,0,width,height);
            }
        };
        timer.start();
    }

    }









