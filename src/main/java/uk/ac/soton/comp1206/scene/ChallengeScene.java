package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene
    implements NextPieceListener, RightClickedListener, LineClearedListener, GameLoopListener {

  private static final Logger logger = LogManager.getLogger(ChallengeScene.class);

  /** Game instance*/
  protected Game game;
  /** Multimedia instance to play audio/sound */
  Multimedia multimedia = new Multimedia();

  /** Visual representation of pieceboard*/
  protected PieceBoard pieceboard;
  /** Visual representation of the next pieceboard*/
  protected PieceBoard nextpieceboard;

  /** Visual representation of main game board*/
  protected GameBoard board;

  /** The rectangle to be animated, representating the timer*/
  protected Rectangle timerectangle;

  /** TimeLine dictating the animation keyframes*/
  protected Timeline timeline = new Timeline();

  /** the main container for placing the components into*/
  BorderPane mainPane = new BorderPane();

  /** Animation for the colour change of the timer rectangle */
  FillTransition fillTransition = new FillTransition();

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
  }

  /** Build the Challenge window */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();
    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    pieceboard =
        new PieceBoard(game.getPieceboard(), gameWindow.getWidth() / 4, gameWindow.getWidth() / 4);
    nextpieceboard =
        new PieceBoard(
            game.getnextpieceboard(), gameWindow.getWidth() / 6, gameWindow.getWidth() / 6);

    game.setNextPieceListener(this);
    game.setLineClearedListener(this);
    game.setGameLoopListener(this);
    pieceboard.setOnRightClicked(this);
    nextpieceboard.setOnMouseClicked(event -> game.swapcurrentpiece());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("menu-background");
    root.getChildren().add(challengePane);
    String path = "src/main/resources/music/game_start.wav";
    String pathtwo = "src/main/resources/music/game.wav";
    Media gamestart = new Media(new File(path).toURI().toString());
    Media gamemusic = new Media(new File(pathtwo).toURI().toString());
    multimedia.playgamemusic(gamestart, gamemusic);

    challengePane.getChildren().add(mainPane);
    VBox rightsidevbox = new VBox();
    mainPane.setRight(rightsidevbox);
    var score = new Text();
    var scoreLabel = new Text("Score");
    var highscore = new Text();
    var highscoreLabel = new Text("High Score");
    var level = new Text();
    var levelLabel = new Text("Level");
    var lives = new Text();
    var livesLabel = new Text("Lives");
    score.textProperty().bind(game.getScore().asString());
    level.textProperty().bind(game.getLevel().asString());
    lives.textProperty().bind(game.getLives().asString());
    score.getStyleClass().add("score");
    scoreLabel.getStyleClass().add("heading");
    highscore.getStyleClass().add("score");
    highscoreLabel.getStyleClass().add("heading");
    level.getStyleClass().add("level");
    levelLabel.getStyleClass().add("heading");
    lives.getStyleClass().add("lives");
    livesLabel.getStyleClass().add("heading");
    VBox scorevbox = new VBox(scoreLabel, score);
    scorevbox.setPadding(new Insets(5, 0, 0, 5));
    VBox informationvbox = new VBox(highscoreLabel, highscore, levelLabel, level);
    informationvbox.setAlignment(Pos.BOTTOM_CENTER);
    VBox pieceboardvbox = new VBox(pieceboard, nextpieceboard);
    pieceboardvbox.setAlignment(Pos.CENTER);
    pieceboard.setPadding(new Insets(0, 10, 10, 0));
    nextpieceboard.setPadding(new Insets(0, 10, 10, 0));
    VBox livesvbox = new VBox(livesLabel, lives);
    HBox tophbox = new HBox(scorevbox, livesvbox);
    tophbox.prefHeight(60);
    livesvbox.setPadding(new Insets(5, 5, 0, 0));
    livesvbox.setPrefWidth(800);
    livesvbox.setPrefHeight(60);
    livesvbox.setAlignment(Pos.TOP_RIGHT);
    mainPane.setTop(tophbox);

    if (!(getHighScore() > game.getScore().getValue())) {
      highscore.textProperty().bind(game.getScore().asString());
    } else {
      highscore.setText(String.valueOf(getHighScore()));
    }

    rightsidevbox.getChildren().addAll(informationvbox, pieceboardvbox);
    mainPane.setCenter(board);

    // Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  protected void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
  }

  /** Setup the game object and model */
  public void setupGame() {
    logger.info("Starting a new challenge");

    // Start new game
    game = new Game(5, 5);
  }

  /** Initialise the scene and start the game
   *  Handles keyboard inputs
   */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    game.start();
    scene.setOnKeyPressed(
        event -> {
          KeyCode keyCode = event.getCode();

          // Handle ESCAPE key press
          if (keyCode.equals(KeyCode.ESCAPE)) {
            gameWindow.cleanup();
            multimedia.stopmusicplayer();
            gameWindow.startMenu();
            game.stoptimer();
          }

          // Handle WASD key presses for moving selection
          switch (keyCode) {
            case W:
            case UP:
              board.moveSelection(0, -1);
              break;
            case A:
            case LEFT:
              board.moveSelection(-1, 0);
              break;
            case S:
            case DOWN:
              board.moveSelection(0, 1);
              break;
            case D:
            case RIGHT:
              board.moveSelection(1, 0);
              break;

            case ENTER:
            case X:
              blockClicked(board.getBlock(board.getSelectedCol(), board.getSelectedRow()));
              break;

            case SPACE:
            case R:
              game.swapcurrentpiece();
              break;

            case Q:
            case Z:
            case OPEN_BRACKET:
              logger.info("q,z,[");
              pieceboard.emptygrid();
              game.rotatecurrentpiececlockwise();
              break;

            case E:
            case C:
            case CLOSE_BRACKET:
              pieceboard.emptygrid();
              game.rotatecurrentpiece();
              break;
          }
        });
  }

  /**
   * Empties the pieceboard and displays the piece in the visual pieceboard
   * @param piece piece to be displayed
   */

  @Override
  public void nextpiece(GamePiece piece) {
    pieceboard.emptygrid();
    pieceboard.displaypiece(piece);
  }

  /**
   * Empties the nextpieceboard and displays the piece in the visual nextpieceboard
   * @param piece piece to be displayed
   */

  @Override
  public void followingpiece(GamePiece piece) {
    nextpieceboard.emptygrid();
    nextpieceboard.displaypiece(piece);
  }

  /**
   * Empties the pieceboard and rotates the piece
   */

  @Override
  public void onRightClicked() {
    pieceboard.emptygrid();
    game.rotatecurrentpiece();
  }

  /**
   * Fades out the blocks when a line is cleared
   */

  @Override
  public void onLineCleared(Set<Pair<Integer, Integer>> blockstodelete) {
    board.fadeOut(blockstodelete);
  }

  /**
   * Resets the animated timer and resets its animation with the passed in delay
   * @param delay
   */

  @Override
  public void timerstarted(int delay) {
    logger.info("new timer started");
    // Configure timer rectangle and timeline
    timerectangle.setWidth(800);
    timerectangle.setHeight(22);
    timeline.getKeyFrames().clear();
    timeline
        .getKeyFrames()
        .addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(timerectangle.widthProperty(), 800)),
            new KeyFrame(Duration.millis(delay), new KeyValue(timerectangle.widthProperty(), 0)));
    timerectangle.setStroke(Color.BLACK);
    timeline.setCycleCount(Animation.INDEFINITE);
    mainPane.setBottom(timerectangle);
    timeline.play();
    fillTransition.play();
  }

  @Override
  public void timerstopped(int delay) {
    logger.info("timer stopped");
    timeline.stop();
    fillTransition.pause();
    fillTransition.playFromStart();
    timerstarted(delay);
  }

  /**
   * Defines the timer, rectangle, and its associated animation
   * @param delay
   */

  @Override
  public void timercreated(int delay) {
    this.timerectangle = new Rectangle();
    timeline.getKeyFrames().clear();
    timeline
        .getKeyFrames()
        .addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(timerectangle.widthProperty(), 800)),
            new KeyFrame(Duration.millis(delay), new KeyValue(timerectangle.widthProperty(), 0)));
    Duration fillTransitionDuration =
        Duration.millis(delay * 1.5); // Adjust the multiplier as needed
    fillTransition.setDuration(fillTransitionDuration);
    fillTransition.setShape(timerectangle);
    fillTransition.setFromValue(Color.GREEN);
    fillTransition.setToValue(Color.RED);
    fillTransition.setAutoReverse(false); // Don't reverse the animation
    timerstarted(delay);
  }

  /**
   * Executes the score scene when the game ends
   *
   * @param game
   */
  @Override
  public void gameended(Game game) {
    logger.info("game ended");
    multimedia.stopmusicplayer();
    timeline.stop();
    gameWindow.startscores(game);
  }

  /**
   * Gets the current highest local score and displays it in the scene
   */

  public int getHighScore() {
    try (BufferedReader reader =
        new BufferedReader(new FileReader("src/main/resources/localscores.txt"))) {
      String line = reader.readLine();
      if (line != null) {
        String[] parts = line.split(":");
        if (parts.length == 2) {
          return Integer.parseInt(parts[1].trim());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0; // Default value if file is empty or an error occurs
  }
}
