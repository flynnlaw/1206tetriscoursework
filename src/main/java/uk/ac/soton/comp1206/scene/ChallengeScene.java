package uk.ac.soton.comp1206.scene;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.StreamCorruptedException;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;

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

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);
        VBox vbox = new VBox();
        mainPane.setRight(vbox);
        var board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
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
        scoreLabel.getStyleClass().add("score");
        multiplier.getStyleClass().add("score");
        multiplierLabel.getStyleClass().add("score");
        level.getStyleClass().add("score");
        levelLabel.getStyleClass().add("score");
        vbox.getChildren().add(scoreLabel);
        vbox.getChildren().add(score);
        vbox.getChildren().add(multiplierLabel);
        vbox.getChildren().add(multiplier);
        vbox.getChildren().add(levelLabel);
        vbox.getChildren().add(level);
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
    }

}
