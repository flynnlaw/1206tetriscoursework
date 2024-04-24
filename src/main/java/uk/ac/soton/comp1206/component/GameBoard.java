package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A GameBoard is a visual component to represent the visual GameBoard. It extends a GridPane to
 * hold a grid of GameBlocks.
 *
 * <p>The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming
 * block. It also be linked to an external grid, for the main game board.
 *
 * <p>The GameBoard is only a visual representation and should not contain game logic or model logic
 * in it, which should take place in the Grid.
 */
public class GameBoard extends GridPane {

  private static final Logger logger = LogManager.getLogger(GameBoard.class);

  /** Number of columns in the board */
  protected final int cols;

  /** Number of rows in the board */
  protected final int rows;

  /** The visual width of the board - has to be specified due to being a Canvas */
  protected final double width;

  /** The visual height of the board - has to be specified due to being a Canvas */
  protected final double height;

  /** The grid this GameBoard represents */
  final Grid grid;

  /** The blocks inside the grid */
  GameBlock[][] blocks;

  /** The listener to call when a specific block is clicked */
  private BlockClickedListener blockClickedListener;

  /** The listener to call when the pieceboard is rightclicked */
  private RightClickedListener rightClickedListener;

  /** Game instance used only for getting the current piece */
  private Game game;

  /** Row selected by keyboard binds */
  int selectedRow = 0;

  /** Column selected by keyboard binds */
  int selectedCol = 0;

  /**
   * Create a new GameBoard, based off a given grid, with a visual width and height.
   *
   * @param grid linked grid
   * @param width the visual width
   * @param height the visual height
   */
  public GameBoard(Grid grid, double width, double height) {
    this.cols = grid.getCols();
    this.rows = grid.getRows();
    this.width = width;
    this.height = height;
    this.grid = grid;

    // Build the GameBoard
    build();
    setOnMouseClicked(this::handleRightClick);
  }

  /**
   * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows,
   * along with the visual width and height.
   *
   * @param cols number of columns for internal grid
   * @param rows number of rows for internal grid
   * @param width the visual width
   * @param height the visual height
   */
  public GameBoard(int cols, int rows, double width, double height) {
    this.cols = cols;
    this.rows = rows;
    this.width = width;
    this.height = height;
    this.grid = new Grid(cols, rows);

    // Build the GameBoard
    build();
  }

  /**
   * Get a specific block from the GameBoard, specified by it's row and column
   *
   * @param x column
   * @param y row
   * @return game block at the given column and row
   */
  public GameBlock getBlock(int x, int y) {
    return blocks[x][y];
  }

  /**
   * Sets game instance so current piece can be accessed.
   *
   * @param game game instance
   */
  public void setGame(Game game) {
    this.game = game;
  }

  /** Build the GameBoard by creating a block at every x and y column and row */
  protected void build() {
    logger.info("Building grid: {} x {}", cols, rows);

    setMaxWidth(width);
    setMaxHeight(height);

    setGridLinesVisible(true);

    blocks = new GameBlock[cols][rows];

    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        createBlock(x, y);
      }
    }
  }

  /**
   * Create a block at the given x and y position in the GameBoard
   *
   * @param x column
   * @param y row
   */
  protected GameBlock createBlock(int x, int y) {
    var blockWidth = width / cols;
    var blockHeight = height / rows;

    // Create a new GameBlock UI component
    GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight, false);

    // Add to the GridPane
    add(block, x, y);

    // Add to our block directory
    blocks[x][y] = block;

    // Link the GameBlock component to the corresponding value in the Grid
    block.bind(grid.getGridProperty(x, y));

    // Add a mouse click handler to the block to trigger GameBoard blockClicked method
    block.setOnMouseClicked((e) -> blockClicked(e, block));
    block.setOnMouseEntered(mouseEvent -> selectBlock(block.getX(), block.getY()));
    block.setOnMouseExited(mouseEvent -> resethoveredstate());

    return block;
  }

  /**
   * Set the listener to handle an event when a block is clicked
   *
   * @param listener listener to add
   */
  public void setOnBlockClick(BlockClickedListener listener) {
    this.blockClickedListener = listener;
  }

  /**
   * Triggered when a block is clicked. Call the attached listener.
   *
   * @param event mouse event
   * @param block block clicked on
   */
  protected void blockClicked(MouseEvent event, GameBlock block) {
    logger.info("Block clicked: {}", block);

    if (blockClickedListener != null) {
      blockClickedListener.blockClicked(block);
    }
  }

  /**
   * Set the listener to handle an event when the pieceboard is clicked
   *
   * @param rightClickedListener listener to add
   */
  public void setOnRightClicked(RightClickedListener rightClickedListener) {
    this.rightClickedListener = rightClickedListener;
  }

  /**
   * Triggered when the pieceboard(s) are clicked. Call the attached listener.
   *
   * @param event the mouse event
   */
  private void handleRightClick(MouseEvent event) {
    if (rightClickedListener != null) {
      rightClickedListener.onRightClicked();
      logger.info("clck");
    }
  }

  /**
   * fade out every block that is deleted.
   *
   * @param blockstodelete set of indivdual blockstodelete (+therefore fade out)
   */
  public void fadeOut(Set<Pair<Integer, Integer>> blockstodelete) {
    for (Pair<Integer, Integer> block : blockstodelete) {
      getBlock(block.getKey(), block.getValue()).fadeOut();
    }
  }

  /**
   * Returns number of columns
   *
   * @return number of columns
   */
  public double getCols() {
    return cols;
  }

  /**
   * Returns number of rows
   *
   * @return number of rows
   */
  public double getRows() {
    return rows;
  }

  /**
   * Returns the gameboard width
   *
   * @return the width of gameboard
   */
  public double getgameboardwidth() {
    return width;
  }

  /**
   * Returns the gameboard height
   *
   * @return the height of gameboard
   */
  public double getgameboardheight() {
    return height;
  }

  /**
   * moves the block selected by the keyboard
   *
   * @param deltaX moves selected block by deltaX
   * @param deltaY moves selected block by deltaY
   */
  public void moveSelection(int deltaX, int deltaY) {
    // Calculate the new row and column index for the selection
    int newRow = Math.max(0, Math.min(rows - 1, selectedRow + deltaY));
    int newCol = Math.max(0, Math.min(cols - 1, selectedCol + deltaX));

    // Clear the current selection
    clearSelection();

    // Update the selection to the new row and column
    selectBlock(newCol, newRow);
  }

  /**
   * Calls the hover method on the specific block
   *
   * @param col column selected
   * @param row row selected
   */
  private void selectBlock(int col, int row) {
    // Highlight the selected block or perform any other selection-related action
    makeblockhover(game.getGamePiece(), col, row);
    selectedCol = col;
    selectedRow = row;
  }

  /** When the block is exited, the exited method is called. */
  private void clearSelection() {
    // Clear the highlight from the currently selected block
    if (selectedCol >= 0 && selectedRow >= 0 && selectedCol < cols && selectedRow < rows) {
      resethoveredstate();
    }
  }

  /** Returns the selected row */
  public int getSelectedRow() {
    return selectedRow;
  }

  /** Returns the selected column */
  public int getSelectedCol() {
    return selectedCol;
  }

  /**
   * Displays a visual representation of where the block is to be placed
   *
   * @param gamePiece piece to be displayed
   * @param x x coordinate of selected block
   * @param y y coordinate of selected block
   */
  public void makeblockhover(GamePiece gamePiece, int x, int y) {
    if (game != null && game.getGamePiece() != null) {
      boolean playable = grid.canPlayPiece(gamePiece, x, y);
      logger.info(playable);
      int[][] pieceshape = gamePiece.getBlocks();
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          if (!(pieceshape[i][j] == 0)) {
            int positionx = i - 1;
            int positiony = j - 1;
            try {
              blocks[x + positionx][y + positiony].hover(gamePiece, playable);
            } catch (IndexOutOfBoundsException e) {
              continue;
            }
          }
        }
      }
    }
  }

  /** Resets all blocks in the board to neutral */
  public void resethoveredstate() {
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        blocks[i][j].exited();
      }
    }
  }
}
