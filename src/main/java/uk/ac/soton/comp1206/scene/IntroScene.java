package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/** The Intro Scene refines the game upon launch through the animation of a logo. */
public class IntroScene extends BaseScene {
  Multimedia multimedia = new Multimedia();

  private static final Logger logger = LogManager.getLogger(IntroScene.class);

  /**
   * Create a new Intro Scene
   *
   * @param gameWindow the Game Window
   */
  public IntroScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Intro Scene");
    String path = "src/main/resources/sounds/intro.mp3";
    Media backgroundmusic = new Media(new File(path).toURI().toString());
    logger.info("playing background music");
    multimedia.playmenumusic(backgroundmusic);
  }

  /** Build the Instructions window */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.setStyle("-fx-background-color: black;");
    root.getChildren().add(menuPane);
    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);
    try {
      // Load the image
      Image ecsgamesimage =
          new Image(new FileInputStream("src/main/resources/images/ECSGames.png"));
      ImageView titleImage = new ImageView(ecsgamesimage);
      titleImage.setPreserveRatio(true);
      titleImage.setFitWidth(250);

      // Position the image in the center
      titleImage.setLayoutX((gameWindow.getWidth() - titleImage.getFitWidth()) / 2);
      titleImage.setLayoutY((gameWindow.getHeight() - titleImage.getFitHeight()) / 2);

      // Add the image to the black screen
      mainPane.setCenter(titleImage);

      // Fade in animation for the image
      FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), titleImage);
      fadeIn.setFromValue(0);
      fadeIn.setToValue(1);
      fadeIn.play();

      // Fade out animation for the image
      FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), titleImage);
      fadeOut.setFromValue(1);
      fadeOut.setToValue(0);
      fadeOut.setDelay(Duration.seconds(2)); // Wait for 2 seconds before starting fade out

      // After fade out, remove the black screen and show the rest of the menu
      fadeOut.setOnFinished(
          event -> {
            gameWindow.startMenu();
          });
      fadeOut.play();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /** Initialise the scene and start the game Handles keyboard inputs for escape */
  @Override
  public void initialise() {
    scene.setOnKeyPressed(
        new EventHandler<KeyEvent>() {
          @Override
          public void handle(KeyEvent keyEvent) {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
              multimedia.stopmusicplayer();
              gameWindow.startMenu();
            }
          }
        });
  }
}
