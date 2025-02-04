package uk.ac.soton.comp1206.scene;

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

/**
 * The Instructions Scene introduces the game to players, introducing the controls and the potential game pieces the players will encounter.
 */

public class InstructionsScene extends BaseScene {
  Multimedia multimedia = new Multimedia();

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  /**
   * Create a new Instruction Scene
   * @param gameWindow the Game Window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
    String path = "src/main/resources/music/menu.mp3";
    String path1 = "src/main/resources/music/suiiianthem.mp3";
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
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);
    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);
    try {
      Image tetrecsimage =
          new Image(new FileInputStream("src/main/resources/images/Instructions.png"));
      ImageView imageView = new ImageView();
      imageView.setImage(tetrecsimage);
      imageView.setPreserveRatio(true);
      imageView.setFitHeight(400);
      imageView.setFitWidth(gameWindow.getWidth());
      menuPane.setAlignment(Pos.TOP_CENTER);
      menuPane.getChildren().add(imageView);
      imageView.setX(100.0);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    VBox vbox = new VBox();
    HBox hbox = new HBox();
    HBox hbox2 = new HBox();
    hbox.setAlignment(Pos.CENTER);
    hbox.setPadding(new Insets(0, 5, 5, 5));
    hbox.setSpacing(5);
    hbox2.setPadding(new Insets(0, 5, 5, 5));
    hbox2.setSpacing(5);
    hbox2.setAlignment(Pos.CENTER);
    Label label = new Label("Example blocks: ");
    label.getStyleClass().add("messages");
    label.setTextFill(Color.WHITE);
    vbox.getChildren().addAll(label, hbox, hbox2);
    vbox.setMargin(label, new Insets(0, 0, 0, 340));
    mainPane.setBottom(vbox);

    for (int i = 0; i < 7; i++) {
      PieceBoard piece =
          new PieceBoard(new Grid(3, 3), gameWindow.getWidth() / 10, gameWindow.getWidth() / 10, i);
      hbox.getChildren().add(piece);
    }
    for (int i = 7; i < 15; i++) {
      PieceBoard piece =
          new PieceBoard(new Grid(3, 3), gameWindow.getWidth() / 10, gameWindow.getWidth() / 10, i);
      hbox2.getChildren().add(piece);
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
