package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.io.File;
import java.io.FileInputStream;

/** The main menu of the game. Provides a gateway to the rest of the game. */
public class MenuScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  Multimedia multimedia = new Multimedia();

  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }

  /** Build the menu layout */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    String path = "src/main/resources/music/menu.mp3";
    String path1 = "src/main/resources/music/suiiianthem.mp3";
    Media backgroundmusic = new Media(new File(path).toURI().toString());
    logger.info("playing background music");
    multimedia.playmenumusic(backgroundmusic);

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);
    FlowPane flowPane = new FlowPane();
    flowPane.setPrefSize(200, 200);

    flowPane.setRowValignment(VPos.TOP);
    flowPane.setAlignment(Pos.TOP_CENTER);
    mainPane.setBottom(flowPane);

    try {
      Image tetrecsimage = new Image(new FileInputStream("src/main/resources/images/TetrECS.png"));
      ImageView imageView = new ImageView();
      imageView.setImage(tetrecsimage);
      imageView.setPreserveRatio(true);
      imageView.setFitHeight(300);
      imageView.setFitWidth(550);
      mainPane.setCenter(imageView);
      Timeline logorotate =
          new Timeline(
              new KeyFrame(Duration.ZERO, new KeyValue(imageView.rotateProperty(), 0)),
              new KeyFrame(Duration.seconds(3), new KeyValue(imageView.rotateProperty(), 10)),
              new KeyFrame(Duration.seconds(6), new KeyValue(imageView.rotateProperty(), 0)),
              new KeyFrame(Duration.seconds(9), new KeyValue(imageView.rotateProperty(), -10)));
      logorotate.setAutoReverse(true);
      logorotate.setCycleCount(Animation.INDEFINITE);
      logorotate.play();
    } catch (Exception e) {
      logger.info("file not found");
    }

    // For now, let us just add a button that starts the game. I'm sure you'll do something way
    // better.
    var singleplayerbutton = new Button("Single Player");
    var multiplayerbutton = new Button("Multi Player");
    var instructionsbutton = new Button("Instructions");
    VBox vbox = new VBox();
    vbox.getChildren().addAll(singleplayerbutton, multiplayerbutton, instructionsbutton);
    vbox.setAlignment(Pos.TOP_CENTER);
    flowPane.getChildren().add(vbox);

    singleplayerbutton.getStyleClass().add("buttonintro");
    multiplayerbutton.getStyleClass().add("buttonintro");
    instructionsbutton.getStyleClass().add("buttonintro");
    singleplayerbutton.setTextFill(Color.WHITE);
    multiplayerbutton.setTextFill(Color.WHITE);
    instructionsbutton.setTextFill(Color.WHITE);
    singleplayerbutton.setOnMouseEntered(
        mouseEvent -> singleplayerbutton.setTextFill(Color.YELLOW));
    multiplayerbutton.setOnMouseEntered(mouseEvent -> multiplayerbutton.setTextFill(Color.YELLOW));
    instructionsbutton.setOnMouseEntered(
        mouseEvent -> instructionsbutton.setTextFill(Color.YELLOW));
    singleplayerbutton.setOnMouseExited(mouseEvent -> singleplayerbutton.setTextFill(Color.WHITE));
    multiplayerbutton.setOnMouseExited(mouseEvent -> multiplayerbutton.setTextFill(Color.WHITE));
    instructionsbutton.setOnMouseExited(mouseEvent -> instructionsbutton.setTextFill(Color.WHITE));

    // Bind the button action to the startGame method in the menu
    singleplayerbutton.setOnAction(this::startGame);
    multiplayerbutton.setOnAction(this::gotomultiplayer);
    instructionsbutton.setOnAction(this::gotoinstructions);
  }

  /** Initialise the menu */
  @Override
  public void initialise() {
    scene.setOnKeyPressed(
        event -> {
          KeyCode keyCode = event.getCode();

          // Handle ESCAPE key press
          if (keyCode.equals(KeyCode.ESCAPE)) {
            gameWindow.cleanup();
            multimedia.stopmusicplayer();
            Platform.exit();
          }
        });
  }

  /**
   * Handle when the Start Game button is pressed
   *
   * @param event event
   */
  private void startGame(ActionEvent event) {
    logger.info("stopping background music");
    multimedia.stopmusicplayer();
    gameWindow.startChallenge();
  }

  /**
   * Handle when the How to Play button is pressed
   *
   * @param event event
   */
  private void gotoinstructions(ActionEvent event) {
    gameWindow.startinstructions();
    multimedia.stopmusicplayer();
  }

  /**
   * Handle when the Multiplayer button is pressed
   *
   * @param event event
   */
  private void gotomultiplayer(ActionEvent event) {
    multimedia.stopmusicplayer();
    gameWindow.startlobby();
  }
}
