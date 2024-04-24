package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.media.Media;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.util.*;
import java.io.File;

import javafx.util.Pair;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

  private static final Logger logger = LogManager.getLogger(Game.class);

  /** Number of rows */
  protected final int rows;

  /** Number of columns */
  protected final int cols;

  /** The grid model linked to the game */
  protected final Grid grid;

  /** Game representation of the pieceboard */
  protected final Grid pieceboard;

  /** Game representation of the pieceboard storing the piece after the next one to be placed */
  protected final Grid nextpieceboard;

  /** The listener to call when a piece is placed */
  protected NextPieceListener nextPieceListener;

  /** The listener to call when a line is cleared in game */
  protected LineClearedListener lineClearedListener;

  /** The listener to call when the timer runs out or is reset in game */
  protected GameLoopListener gameLoopListener;

  /** Allows sound effects/background music to be played */
  Multimedia multimedia = new Multimedia();

  /** Score property of the game */
  SimpleIntegerProperty score = new SimpleIntegerProperty(0);

  /** Level property of the game */
  SimpleIntegerProperty level = new SimpleIntegerProperty(0);

  /** Lives property of the game */
  SimpleIntegerProperty lives = new SimpleIntegerProperty(999);

  /** Multiplier property of the game */
  SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

  /** The next piece to be placed down */
  GamePiece currentPiece;

  /** The piece that will be placed after the currentPiece */
  GamePiece followingPiece;

  /** The path to the sound played when rotating a piece */
  String rotatesoundpath = "src/main/resources/sounds/rotate.wav";

  /** The path to the sound played when clearing a line */
  String clearsoundpath = "src/main/resources/sounds/clear.wav";

  /** The path to the sound played when you fail in the game */
  String failsoundpath = "src/main/resources/sounds/fail.wav";

  /** The path to the sound played when a piece is placed */
  String placesoundpath = "src/main/resources/sounds/place.wav";

  /** The path to the sound played when you fully clear the board */
  String suiiisoundpath = "src/main/resources/sounds/suiii-loud.mp3";

  /** Media pertaining to each path */
  Media rotatesound = new Media(new File(rotatesoundpath).toURI().toString());

  Media clearsound = new Media(new File(clearsoundpath).toURI().toString());
  Media failsound = new Media(new File(failsoundpath).toURI().toString());
  Media placesound = new Media(new File(placesoundpath).toURI().toString());
  Media suiiisound = new Media(new File(suiiisoundpath).toURI().toString());

  /** Timer linked to the game loop */
  protected Timer timer;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    // Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
    this.pieceboard = new Grid(3, 3);
    this.nextpieceboard = new Grid(3, 3);
  }

  /** Start the game */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
  }

  /** Initialise a new game and set up anything that needs to be done at the start */
  public void initialiseGame() {
    logger.info("Initialising game");
    currentPiece = spawnPiece();
    followingPiece = spawnPiece();
    updatepieceboard(currentPiece);
    nextPieceListener.followingpiece(followingPiece);
    starttimer();
    if (gameLoopListener != null) {
      gameLoopListener.timercreated(getTimerDelay());
    }
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    // Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    // Get the new value for this block
    int previousValue = grid.get(x, y);
    int newValue = previousValue + 1;
    if (newValue > GamePiece.PIECES) {
      newValue = 0;
    }

    if (grid.canPlayPiece(currentPiece, x, y)) {
      logger.info("piece true");
      grid.playPiece(currentPiece, x, y);
      multimedia.setaudioplayer(placesound);
      nextPiece();
      afterPiece();
      updatepieceboard(currentPiece);
      stoptimer();
      starttimer();
      gameLoopListener.timerstopped(getTimerDelay());
    } else {
      multimedia.setaudioplayer(failsound);
    }
  }

  /**
   * Update the games representation of the pieceboard
   *
   * @param gamePiece the game piece to place onto the pieceboard
   */
  public void updatepieceboard(GamePiece gamePiece) {
    int[][] pieceshape = gamePiece.getBlocks();
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        if (!(pieceshape[x][y] == 0)) {
          pieceboard.set(x, y, gamePiece.getValue());
        }
      }
    }
  }

  /**
   * Generates a random new piece to be placed
   *
   * @return the generated game piece
   */
  public GamePiece spawnPiece() {
    pieceboard.emptygrid();
    Random randomnumber = new Random();
    int[][] blocks = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
    GamePiece newpiece = null;
    return newpiece.createPiece(randomnumber.nextInt(15));
    // return newpiece.createPiece(0);
  }

  /**
   * Updates the pieces in the game with new pieces after a piece has been placed Further pings the
   * challenge scene to update the visualisation of the pieceboards.
   */
  public void nextPiece() {
    currentPiece = followingPiece;
    followingPiece = spawnPiece();
    if (nextPieceListener != null) {
      nextPieceListener.nextpiece(currentPiece);
      nextPieceListener.followingpiece(followingPiece);
    }
  }

  /**
   * Calculates the rows and blocks to delete from the grid, and updates the score, level and
   * mutliplier of the game. Further notifies the challenge scene to fade out each block
   */
  public void afterPiece() {
    HashSet<Integer> rowstodelete = new HashSet<>();
    HashSet<Integer> columnstodelete = new HashSet<>();
    Set<Pair<Integer, Integer>> blocksdeleted = new HashSet<>();
    int beforesize = blocksdeleted.size();
    countrows(rowstodelete);
    countcolumns(columnstodelete);
    int aftersize = blocksdeleted.size();
    for (int row : rowstodelete) {
      for (int column = 0; column < 5; column++) {
        grid.set(column, row, 0);
        blocksdeleted.add(new Pair<>(column, row));
      }
      logger.info("row" + row + "deleted");
    }
    for (int column : columnstodelete) {
      for (int row = 0; row < 5; row++) {
        grid.set(column, row, 0);
        blocksdeleted.add(new Pair<>(column, row));
      }
      logger.info("column" + column + "deleted");
    }
    if (!(columnstodelete.isEmpty()) || !(rowstodelete.isEmpty())) {
      String path = "src/main/resources/sounds/clear.wav";
      Media linecleared = new Media(new File(path).toURI().toString());
      multimedia.setaudioplayer(linecleared);
    }
    if (beforesize != aftersize) {
      multimedia.setaudioplayer(clearsound);
    }
    int prevscore = score.getValue();
    score(rowstodelete.size() + columnstodelete.size(), blocksdeleted.size());
    int newscore = score.getValue();
    if (prevscore != newscore) {
      multiplier.set(multiplier.getValue() + 1);
      Integer levelbefore = level.getValue();
      level.set((score.getValue() / 1000));
      Integer levelafter = level.getValue();
      if (levelbefore != levelafter) {
        String path = "src/main/resources/sounds/level.wav";
        Media levelup = new Media(new File(path).toURI().toString());
        multimedia.setaudioplayer(levelup);
      }
    } else {
      multiplier.set(1);
    }
    checkempty();
    if (lineClearedListener != null) {
      lineClearedListener.onLineCleared(blocksdeleted);
    }
  }

  /** Check the grid is empty */
  public void checkempty() {
    int count = 0;
    for (int x = 0; x < 5; x++) {
      for (int y = 0; y < 5; y++) {
        if ((grid.get(y, x) == 0)) {
          count++;
        }
      }
    }
    if (count == 25) {
      multimedia.setaudioplayer(suiiisound);
      logger.info("suiiii");
    }
  }

  /**
   * Counts the number of full rows in the game grid
   *
   * @param rowstodelete Set of rows to delete
   */
  public void countrows(HashSet<Integer> rowstodelete) {
    int count = 0;
    for (int x = 0; x < 5; x++) {
      for (int y = 0; y < 5; y++) {
        if (!(grid.get(y, x) == 0)) {
          count++;
        }
      }
      if (count == 5) {
        rowstodelete.add(x);
        logger.info("added" + x + "row to delete");
      }
      count = 0;
    }
  }

  /**
   * Counts the number of full columns in the game grid
   *
   * @param columnstodelete Set of columns to delete
   */
  public void countcolumns(HashSet<Integer> columnstodelete) {
    int count = 0;
    for (int x = 0; x < 5; x++) {
      for (int y = 0; y < 5; y++) {
        if (!(grid.get(x, y) == 0)) {
          count++;
        }
      }
      if (count == 5) {
        columnstodelete.add(x);
        logger.info("added" + x + "column to delete");
      }
      count = 0;
    }
  }

  /**
   * Given a number of lines and individual blocks deleted, uodates the score accordingly.
   *
   * @param nolines number of lines
   * @param noblocks number of blocks
   */
  public void score(int nolines, int noblocks) {
    score.set(score.getValue() + (nolines * noblocks * 10 * multiplier.getValue()));
  }

  /**
   * Swaps the piece held in the pieceboard with the piece held in the nextpieceboard Further pings
   * the challenge scene to swap their representations.
   */
  public void swapcurrentpiece() {
    multimedia.setaudioplayer(rotatesound);
    pieceboard.emptygrid();
    nextpieceboard.emptygrid();
    logger.info("next piece grid clicked");
    GamePiece temp = currentPiece;
    currentPiece = followingPiece;
    followingPiece = temp;
    pieceboard.changedisplayedpiece(currentPiece);
    nextpieceboard.changedisplayedpiece(followingPiece);
  }

  /**
   * Rotates the piece in the games grid anticlockwise Further pings the challenge scene to rotate
   * its representation as well.
   */
  public void rotatecurrentpiece() {
    multimedia.setaudioplayer(rotatesound);
    currentPiece.rotate();
    pieceboard.changedisplayedpiece(currentPiece);
  }

  /**
   * Rotates the piece in the games grid clockwise Further pings the challenge scene to rotate its
   * representation as well.
   */
  public void rotatecurrentpiececlockwise() {
    multimedia.setaudioplayer(rotatesound);
    currentPiece.rotateClockwise();
    pieceboard.changedisplayedpiece(currentPiece);
  }

  /**
   * Gets the current length of the timer according to current level.
   *
   * @return delau of timer
   */
  public int getTimerDelay() {
    int delay = 12000 - (500 * level.getValue());
    if (delay < 2500) {
      delay = 2500;
    }
    return delay;
  }

  /**
   * Starts a new timer, with length of the delay When it runs out it calls the gameLoop() method
   */
  public void starttimer() {
    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            gameLoop();
          }
        },
        getTimerDelay(),
        1);
  }

  /** Resets the timer */
  public void stoptimer() {
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
  }

  /**
   * Decrements lives, resets mutliplier to 1, changes the piece, and resets the timer Further pings
   * the challenge scene to reset the timer animation If the user runs out of lives, it will stop
   * the game and load the scores scene
   */
  public void gameLoop() {
    if (lives.get() - 1 < 0) {
      stoptimer();
      Platform.runLater(
          () -> {
            gameLoopListener.gameended(this);
            String path = "src/main/resources/sounds/explode.wav";
            Media gameend = new Media(new File(path).toURI().toString());
            multimedia.setaudioplayer(gameend);
          });
    } else {
      lives.set(lives.get() - 1);
      String path = "src/main/resources/sounds/lifelose.wav";
      Media lifelost = new Media(new File(path).toURI().toString());
      multimedia.setaudioplayer(lifelost);
      multiplier.set(1);
      nextPiece();
      if (gameLoopListener != null) {
        gameLoopListener.timerstopped(getTimerDelay());
      }
      stoptimer();
      starttimer();
    }
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
  }

  /**
   * Get the games representation of the pieceboard
   *
   * @return pieceboard grid
   */
  public Grid getPieceboard() {
    return pieceboard;
  }

  /**
   * Gets the games representation of the pieceboard holding the piece after the current piece.
   *
   * @return next pieceboard grid
   */
  public Grid getnextpieceboard() {
    return nextpieceboard;
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
   * Get the current score in the game
   *
   * @return score
   */
  public SimpleIntegerProperty getScore() {
    return score;
  }

  /**
   * Get the current level in the game
   *
   * @return level
   */
  public SimpleIntegerProperty getLevel() {
    return level;
  }

  /**
   * Get the current lives in the game
   *
   * @return lives
   */
  public SimpleIntegerProperty getLives() {
    return lives;
  }

  public GamePiece getGamePiece() {
    return currentPiece;
  }

  /**
   * Set the listener to handle an event when a piece is clicked
   *
   * @param nextPieceListener listener to add
   */
  public void setNextPieceListener(NextPieceListener nextPieceListener) {
    this.nextPieceListener = nextPieceListener;
  }

  /**
   * Set the listener to handle an event when a line is cleared
   *
   * @param lineClearedListener listener to add
   */
  public void setLineClearedListener(LineClearedListener lineClearedListener) {
    this.lineClearedListener = lineClearedListener;
  }

  /**
   * Set the listener to handle an event when the timer runs out
   *
   * @param gameLoopListener listener to add
   */
  public void setGameLoopListener(GameLoopListener gameLoopListener) {
    this.gameLoopListener = gameLoopListener;
  }
}
