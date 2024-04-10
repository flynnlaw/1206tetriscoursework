package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener, RightClickedListener, LineClearedListener {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;
    Multimedia multimedia = new Multimedia();

    private PieceBoard pieceboard;
    private PieceBoard nextpieceboard;

    private GameBoard board;



    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");

    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        pieceboard = new PieceBoard(game.getPieceboard(), gameWindow.getWidth()/4, gameWindow.getWidth()/4);
        nextpieceboard = new PieceBoard(game.getnextpieceboard(), gameWindow.getWidth()/6, gameWindow.getWidth()/6);

        game.setNextPieceListener(this);
        game.setLineClearedListener(this);
        pieceboard.setOnRightClicked(this);
        nextpieceboard.setOnMouseClicked(event -> game.swapcurrentpiece());






        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

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

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);
        VBox vbox = new VBox();
        VBox scorevbox = new VBox();
        mainPane.setRight(vbox);
        var score = new Text();
        var scoreLabel = new Text("Score");
        var multiplier = new Text();
        var multiplierLabel = new Text("Multiplier");
        var level = new Text();
        var levelLabel = new Text("Level");
        score.textProperty().bind(game.getScore().asString());
        multiplier.textProperty().bind(game.getMultiplier().asString());
        level.textProperty().bind(game.getLevel().asString());
        score.getStyleClass().add("score");
        scoreLabel.getStyleClass().add("title");
        multiplier.getStyleClass().add("score");
        multiplierLabel.getStyleClass().add("score");
        level.getStyleClass().add("score");
        levelLabel.getStyleClass().add("score");
        scorevbox.getChildren().add(scoreLabel);
        scorevbox.getChildren().add(score);
        mainPane.setTop(scorevbox);
        vbox.getChildren().add(multiplierLabel);
        vbox.getChildren().add(multiplier);
        vbox.getChildren().add(levelLabel);
        vbox.getChildren().add(level);
        vbox.getChildren().add(pieceboard);
        vbox.getChildren().add(nextpieceboard);
        mainPane.setCenter(board);



        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }


    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        scene.setOnKeyPressed(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent keyEvent) {
                        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                            gameWindow.cleanup();
                            multimedia.stopmusicplayer();
                            gameWindow.startMenu();
                        }
                    }
                });


    }


    @Override
    public void nextpiece(GamePiece piece) {
        pieceboard.displaypiece(piece);

    }
    @Override
    public void followingpiece(GamePiece piece){
        nextpieceboard.emptygrid();
        nextpieceboard.displaypiece(piece);
    }

    @Override
    public void onRightClicked() {
        pieceboard.emptygrid();
        game.rotatecurrentpiece();
    }

    @Override
    public void onLineCleared(Set<Pair<Integer, Integer>> blockstodelete) {
        board.fadeOut(blockstodelete);
    }
}
