package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

import java.util.Random;

/** The board that shows the next/after that piece in the game. */
public class PieceBoard extends GameBoard {

  private static final Logger logger = LogManager.getLogger(PieceBoard.class);

  /**
   * Creates a new Pieceboard, given off a given grid, width and height.
   *
   * @param grid grid to base board off
   * @param width width of board
   * @param height height of board
   */
  public PieceBoard(Grid grid, double width, double height) {
    super(grid, width, height);
  }

  /**
   * Creates a new Pieceboard, given off a given grid, width, height and value Then fills the
   * pieceboard with the piece specified.
   *
   * @param grid grid to base board off
   * @param width width of board
   * @param height height of board
   * @param value value of piece to load into board
   */
  public PieceBoard(Grid grid, double width, double height, int value) {
    super(grid, width, height);
    GamePiece piece = spawnPiece(value);
    int[][] gridneeded = piece.getBlocks();
    for (int j = 0; j < 3; j++) {
      for (int k = 0; k < 3; k++) {
        if (!(gridneeded[j][k] == 0)) {
          grid.set(j, k, value + 1);
        }
      }
    }
  }

  /**
   * Returns a new gamepiece given a value
   *
   * @param value the specific value assigned to the gamepiece to be generated
   * @return the gamepiece
   */
  public GamePiece spawnPiece(int value) {
    Random randomnumber = new Random();
    int[][] blocks = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
    GamePiece newpiece = null;
    return newpiece.createPiece(value);
  }

  /**
   * Displays a new piece onto the pieceboard
   *
   * @param piece the piece to be displayed
   */
  public void displaypiece(GamePiece piece) {
    int[][] gridneeded = piece.getBlocks();
    int value = piece.getValue();
    for (int j = 0; j < 3; j++) {
      for (int k = 0; k < 3; k++) {
        if (!(gridneeded[j][k] == 0)) {
          grid.set(j, k, value);
        }
      }
    }
  }

  /** Emptys the pieceboard grid, for example, when the piece is rotated. */
  public void emptygrid() {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        grid.set(i, j, 0);
      }
    }
  }

  /**
   * Create a block at the given x and y position in the GameBoard Removes the hover functionality
   * for the pieceboard.
   *
   * @param x column
   * @param y row
   */
  protected GameBlock createBlock(int x, int y) {
    var blockWidth = getgameboardwidth() / getCols();
    var blockHeight = getgameboardheight() / getRows();
    GameBlock block;

    // Create a new GameBlock UI component
    if (x == 1 && y == 1) {
      block = new GameBlock(this, x, y, blockWidth, blockHeight, true);
    } else {
      block = new GameBlock(this, x, y, blockWidth, blockHeight, false);
    }

    // Add to the GridPane
    add(block, x, y);

    // Add to our block directory
    blocks[x][y] = block;

    // Link the GameBlock component to the corresponding value in the Grid
    block.bind(grid.getGridProperty(x, y));

    // Add a mouse click handler to the block to trigger GameBoard blockClicked method
    block.setOnMouseClicked((e) -> blockClicked(e, block));

    return block;
  }
}
