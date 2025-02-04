package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 *
 * <p>Each value inside the Grid is an IntegerProperty can be bound to enable modification and
 * display of the contents of the grid.
 *
 * <p>The Grid contains functions related to modifying the model, for example, placing a piece
 * inside the grid.
 *
 * <p>The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

  /** The number of columns in this grid */
  private final int cols;

  /** The number of rows in this grid */
  private final int rows;

  /** The grid is a 2D arrow with rows and columns of SimpleIntegerProperties. */
  private final SimpleIntegerProperty[][] grid;

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    // Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];

    // Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) {
    try {
      // Get the value held in the property at the x and y index provided
      return grid[x][y].get();
    } catch (ArrayIndexOutOfBoundsException e) {
      // No such index
      return -1;
    }
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * Calculates whether the gamePiece passed through can be played at block coordinates (x,y)
   *
   * @param gamePiece piece to play
   * @param x x coordinate of block
   * @param y y coordinate of block
   * @return true/false
   */
  public boolean canPlayPiece(GamePiece gamePiece, int x, int y) {
    int[][] pieceshape = gamePiece.getBlocks();
    boolean returnvalue = true;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (!(pieceshape[i][j] == 0)) {
          int positionx = 1 - i;
          int positiony = j - 1;
          if (!(get(x - positionx, y + positiony) == 0)) {
            returnvalue = false;
          }
        }
      }
    }
    return returnvalue;
  }

  /**
   * Plays the piece by writing the piece to the games grid
   *
   * @param gamePiece piece to play
   * @param x x coordinate of block
   * @param y y coordinate of block
   */
  public void playPiece(GamePiece gamePiece, int x, int y) {
    int[][] pieceshape = gamePiece.getBlocks();
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (!(pieceshape[i][j] == 0)) {
          int positionx = i - 1;
          int positiony = j - 1;
          set(x + positionx, y + positiony, gamePiece.getValue());
        }
      }
    }
  }

  /** Empties the games grid (used for pieceboards only) */
  public void emptygrid() {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        set(i, j, 0);
      }
    }
  }

  /**
   * Changes the piece on the pieceboard with the passed in piece
   *
   * @param piece piece to pass
   */
  public void changedisplayedpiece(GamePiece piece) {
    int[][] gridneeded = piece.getBlocks();
    int value = piece.getValue();
    for (int j = 0; j < 3; j++) {
      for (int k = 0; k < 3; k++) {
        if (!(gridneeded[j][k] == 0)) {
          set(j, k, value);
        }
      }
    }
  }
}
