package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener, RightClickedListener, LineClearedListener, GameLoopListener {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;
    Multimedia multimedia = new Multimedia();

    private PieceBoard pieceboard;
    private PieceBoard nextpieceboard;

    private GameBoard board;

    private Rectangle timerectangle;

    private Timeline timeline;

    BorderPane mainPane = new BorderPane();



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
        game.setGameLoopListener(this);
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




        challengePane.getChildren().add(mainPane);
        VBox rightsidevbox = new VBox();
        mainPane.setRight(rightsidevbox);
        var score = new Text();
        var scoreLabel = new Text("Score");
        var multiplier = new Text();
        var multiplierLabel = new Text("Multiplier");
        var level = new Text();
        var levelLabel = new Text("Level");
        var lives = new Text();
        var livesLabel = new Text("Lives");
        score.textProperty().bind(game.getScore().asString());
        multiplier.textProperty().bind(game.getMultiplier().asString());
        level.textProperty().bind(game.getLevel().asString());
        lives.textProperty().bind(game.getLives().asString());
        score.getStyleClass().add("score");
        scoreLabel.getStyleClass().add("heading");
        multiplier.getStyleClass().add("score");
        multiplierLabel.getStyleClass().add("heading");
        level.getStyleClass().add("level");
        levelLabel.getStyleClass().add("heading");
        lives.getStyleClass().add("lives");
        livesLabel.getStyleClass().add("heading");
        VBox scorevbox = new VBox(scoreLabel,score);
        scorevbox.setPadding(new Insets(5,0,0,5));
        VBox informationvbox = new VBox(multiplierLabel,multiplier,levelLabel,level);
        informationvbox.setAlignment(Pos.BOTTOM_CENTER);
        VBox pieceboardvbox = new VBox(pieceboard,nextpieceboard);
        pieceboardvbox.setAlignment(Pos.CENTER);
        pieceboard.setPadding(new Insets(0,10,10,0));
        nextpieceboard.setPadding(new Insets(0,10,10,0));
        VBox livesvbox = new VBox(livesLabel,lives);
        HBox tophbox = new HBox(scorevbox,livesvbox);
        tophbox.prefHeight(60);
        livesvbox.setPadding(new Insets(5,5,0,0));
        livesvbox.setPrefWidth(800);
        livesvbox.setPrefHeight(60);
        livesvbox.setAlignment(Pos.TOP_RIGHT);
        mainPane.setTop(tophbox);



        rightsidevbox.getChildren().addAll(informationvbox,pieceboardvbox);
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
                            game.stoptimer();
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

    @Override
    public void timerstarted(int delay) {
            // Configure timer rectangle and timeline
        this.timerectangle = new Rectangle();
            timerectangle.setWidth(800);
            timerectangle.setHeight(22);
            timerectangle.setFill(Color.GREEN);
            timerectangle.setStroke(Color.BLACK);
            this.timeline = new Timeline();

            timeline.getKeyFrames().clear(); // Clear previous keyframes if any
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(timerectangle.widthProperty(), 800)),
                    new KeyFrame(Duration.millis(delay), new KeyValue(timerectangle.widthProperty(), 0))
            );
            timeline.setCycleCount(Animation.INDEFINITE);
        mainPane.setBottom(timerectangle);
            timeline.play();

    }

    @Override
    public void timerstopped() {
        timeline.stop();
        timerectangle.setWidth(200);
        timerectangle.setFill(Color.GREEN);
        timeline.play();
    }

}
