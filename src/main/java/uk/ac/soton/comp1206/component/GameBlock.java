package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.*;
import javafx.scene.paint.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * <p>Extends Canvas and is responsible for drawing itself.
 *
 * <p>Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * <p>The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

  private static final Logger logger = LogManager.getLogger(GameBlock.class);

  /** The set of colours for different pieces */
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

  /** Composition of gameBoard that block belongs to */
  private final GameBoard gameBoard;

  private final double width;
  private final double height;

  /** The column this block exists as in the grid */
  private final int x;

  /** The row this block exists as in the grid */
  private final int y;

  /** The value of this block (0 = empty, otherwise specifies the colour to render as) */
  private final IntegerProperty value = new SimpleIntegerProperty(0);

  /** Graphics for block */
  GraphicsContext gc = getGraphicsContext2D();

  /** Whether a middle dot should be drawn on the block */
  boolean middleDot;

  /** Transitions for the fading in and fading out of potential piece placement */
  FadeTransition fadeInTransition;

  FadeTransition fadeOutTransition;

  FadeTransition colourFade;

  /**
   * Create a new single Game Block
   *
   * @param gameBoard the board this block belongs to
   * @param x the column the block exists in
   * @param y the row the block exists in
   * @param width the width of the canvas to render
   * @param height the height of the canvas to render
   * @param middleDot whether to draw a middle dot or not
   */
  public GameBlock(
      GameBoard gameBoard, int x, int y, double width, double height, boolean middleDot) {
    this.gameBoard = gameBoard;
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;
    this.middleDot = middleDot;

    // A canvas needs a fixed width and height
    setWidth(width);
    setHeight(height);

    // Do an initial paint
    paint();

    // When the value property is updated, call the internal updateValue method
    value.addListener(this::updateValue);
  }

  /**
   * When the value of this block is updated,
   *
   * @param observable what was updated
   * @param oldValue the old value
   * @param newValue the new value
   */
  private void updateValue(
      ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
    paint();
  }

  /** Handle painting of the block canvas */
  public void paint() {
    // If the block is empty, paint as empty
    if (value.get() == 0) {
      paintEmpty();
    } else {
      // If the block is not empty, paint with the colour represented by the value
      paintColor(COLOURS[value.get()]);
    }
  }

  /** Paint this canvas empty */
  private void paintEmpty() {

    // Clear
    gc.clearRect(0, 0, width, height);
    setOpacity(1);

    // Fill
    gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.38));
    gc.fillRect(0, 0, width, height);

    // Border
    gc.setStroke(Color.GREY);
    gc.strokeRect(0, 0, width, height);
  }

  /**
   * Paint this canvas with the given colour Further adds texture by colouring triangles with
   * differing opaqueness
   *
   * @param colour the colour to paint
   */
  private void paintColor(Paint colour) {
    gc = getGraphicsContext2D();
    if (fadeInTransition != null) {
      fadeInTransition.stop();
    }
    if (fadeOutTransition != null) {
      fadeOutTransition.stop();
    }

    // Clear
    gc.clearRect(0, 0, width, height);

    // Colour fill
    if (!(colour.equals(Color.TRANSPARENT))) {
      gc.setFill(colour);
      // gc.setFill(Color.LIGHTBLUE);
      gc.fillRect(0, 0, width, height);
      setOpacity(1);

      // Slighty lighter shade
      gc.setFill(Color.color(1, 1, 1, 0.2));
      gc.fillPolygon(new double[] {0, width, 0}, new double[] {0, height, height}, 3);

      gc.setFill(Color.color(1, 1, 1, 0.4));
      gc.fillRect(0, 0, 3, height); // Left accent
      gc.fillRect(0, 0, width, 3); // Top accent

      gc.setFill(Color.color(0, 0, 0, 0.4));
      gc.fillRect(0, height - 3, width, height); // Bottom accent
      gc.fillRect(width - 3, 0, width, height); // Right accent

      if (middleDot && x == 1 && y == 1 && Math.ceil(width) == 67) {
        gc.setFill(Color.color(1, 1, 1, 0.7));
        gc.fillOval(width / 4, height / 4, width / 2, height / 2);
      }

    } else {
      paintEmpty();
    }

    // Border
    gc.setStroke(Color.GREY);
    gc.strokeRect(0, 0, width, height);
  }

  /**
   * Updates the colour of the block to be more oqaque for hover indication Also fades the colour of
   * the block in and out
   *
   * @param colour the colour to paint
   */
  private void paintHoveredColor(Paint colour, boolean playable) {

    gc = this.getGraphicsContext2D();

    // Create FadeTransitions for fading in and out
    fadeInTransition = new FadeTransition(Duration.millis(1000), this);
    fadeInTransition.setFromValue(0.38); // Starting opacity
    fadeInTransition.setToValue(1); // Ending opacity
    fadeInTransition.setAutoReverse(true); // Fade in and out continuously
    fadeInTransition.setCycleCount(FadeTransition.INDEFINITE); // Repeat indefinitely

    fadeOutTransition = new FadeTransition(Duration.millis(1000), this);
    fadeOutTransition.setFromValue(1); // Starting opacity
    fadeOutTransition.setToValue(0.38); // Ending opacity
    fadeOutTransition.setAutoReverse(true); // Fade in and out continuously
    fadeOutTransition.setCycleCount(FadeTransition.INDEFINITE);// Repeat indefinitely

    // If the block is not playable, adjust the color to be more red
    if (!playable) {
      gc.setFill(Color.rgb(255, 0, 0, 0.2)); // Semi-transparent red
      gc.fillRect(0, 0, width, height);
    } else {
      // Apply hover effect to the original color
      Color originalColor = (Color) colour;
      Color hoveredColor =
          originalColor.deriveColor(0, 0.3, 1, 0.6); // Adjust the hover effect as needed
      gc.setFill(hoveredColor);
      // Start both fade in and fade out transitions

    }

    // Fill the rectangle
    gc.fillRect(0, 0, width, height);

    // Draw the border
    gc.setStroke(Color.GREY);
    gc.strokeRect(0, 0, width, height);
    fadeInTransition.play();
    fadeOutTransition.play();
  }

  /**
   * Get the column of this block
   *
   * @return column number
   */
  public int getX() {
    return x;
  }

  /**
   * Get the row of this block
   *
   * @return row number
   */
  public int getY() {
    return y;
  }

  /**
   * Get the current value held by this block, representing it's colour
   *
   * @return value
   */
  public int getValue() {
    return this.value.get();
  }

  /**
   * Bind the value of this block to another property. Used to link the visual block to a
   * corresponding block in the Grid.
   *
   * @param input property to bind the value to
   */
  public void bind(ObservableValue<? extends Number> input) {
    value.bind(input);
  }

  /** Paint the hovered colour on the GameBlock
   * @param piece piece to hover
   * @param playable whether the piece can be played or not
   */
  public void hover(GamePiece piece, boolean playable) {
    paint();
    paintHoveredColor(COLOURS[piece.getValue()], playable);
  }

  /** Reset the colour back to original after the block is exited Stops all animations */
  public void exited() {
    if (fadeInTransition != null) {
      fadeInTransition.stop();
    }
    if (fadeOutTransition != null) {
      fadeOutTransition.stop();
    }
    paintColor(COLOURS[value.get()]);
  }

  /** Fades the block out when a line is cleared */
  public void fadeOut() {

    AnimationTimer timer =
        new AnimationTimer() {

          private long startTime = -1;
          private final long duration = 1000000000L / 2; // 1 second in nanoseconds

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
            gc.fillRect(0, 0, width, height);
            gc.setStroke(Color.GREY);
            gc.strokeRect(0, 0, width, height);
          }
        };
    timer.start();
  }
}
