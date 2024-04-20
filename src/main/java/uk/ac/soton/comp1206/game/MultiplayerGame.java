package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

import java.io.File;
import java.util.*;

/**
 * The MultiplayerGame class handles the main logic, state and properties of the TetrECS game.
 * Methods to manipulate the game state and to handle actions made by the player should take place
 * inside this class. Methods include communicator messages to handle multiplayer.
 */
public class MultiplayerGame extends Game {

  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

  /** Instance of communicator to send messages */
  Communicator communicator;

  /** Queue containing the pieces of the game */
  Queue<GamePiece> piecequeue;

  /** Value to pause the starting of the game until the piecequeue has enough pieces */
  Boolean started = false;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
    communicator.addListener((message) -> Platform.runLater(() -> this.receiveMessage(message)));
    this.piecequeue = new LinkedList<>();
  }

  /** Initialise a new game and set up anything that needs to be done at the start */
  @Override
  public void initialiseGame() {
    logger.info("Initialising game");
    generatestartingpieces();
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
      generatenextpiece();
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

  public GamePiece spawnPiece(int value) {
    int[][] blocks = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
    GamePiece newpiece = null;
    return newpiece.createPiece(value);
  }

  /**
   * Updates the pieces in the game with new pieces after a piece has been placed Further pings the
   * challenge scene to update the visualisation of the pieceboards.
   */
  @Override
  public void nextPiece() {
    currentPiece = followingPiece;
    followingPiece = piecequeue.poll();
    if (nextPieceListener != null) {
      nextPieceListener.nextpiece(currentPiece);
      nextPieceListener.followingpiece(followingPiece);
    }
  }

  /**
   * Calculates the rows and blocks to delete from the grid, and updates the score, level and
   * mutliplier of the game. Further notifies the challenge scene to fade out each block
   */
  @Override
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
    communicator.send("SCORE " + newscore);
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

  /** Requests starting pieces from the server */
  public void generatestartingpieces() {
    communicator.send("PIECE");
    communicator.send("PIECE");
    communicator.send("PIECE");
    communicator.send("PIECE");
    communicator.send("PIECE");
  }

  /** Requests another piece from the server */
  public void generatenextpiece() {
    communicator.send("PIECE");
  }

  /**
   * Adds pieces to the piecequeue, and delay the start of the game until the piecequeue has reached
   * adequate size
   *
   * @param value value of the piece to be generated
   */
  public void recievestartingpieces(int value) {
    piecequeue.offer(spawnPiece(value));
    if (!(started) && piecequeue.size() > 4) {
      currentPiece = piecequeue.poll();
      followingPiece = piecequeue.poll();
      updatepieceboard(currentPiece);
      nextPieceListener.followingpiece(followingPiece);
      starttimer();
      if (gameLoopListener != null) {
        gameLoopListener.timercreated(getTimerDelay());
      }
      started = true;
    }
  }

  /**
   * Receives each message and splits each message into its command.
   *
   * @param message message received.
   */
  public void receiveMessage(String message) {

    String[] commands = message.split(" ");
    String command = commands[0];

    switch (command) {
      case "PIECE" -> {
        recievestartingpieces(Integer.parseInt(commands[1]));
        //                piecequeue.offer(spawnPiece(Integer.parseInt(commands[1])));
      }
    }
  }

  /**
   * Decrements lives, resets mutliplier to 1, changes the piece, and resets the timer Further pings
   * the challenge scene to reset the timer animation If the user runs out of lives, it will stop
   * the game and load the scores scene
   */
  public void gameLoop() {
    generatenextpiece();
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
}
