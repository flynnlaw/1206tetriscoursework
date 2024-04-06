package uk.ac.soton.comp1206.scene;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class InstructionsScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

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
    HBox hbox3 = new HBox();
    hbox.setAlignment(Pos.CENTER);
    hbox.setPadding(new Insets(0, 5, 5, 5));
    hbox.setSpacing(5);
    hbox2.setPadding(new Insets(0, 5, 5, 5));
    hbox2.setSpacing(5);
    hbox3.setPadding(new Insets(0, 5, 5, 5));
    hbox3.setSpacing(5);
    hbox2.setAlignment(Pos.CENTER);
    hbox3.setAlignment(Pos.CENTER);
    vbox.getChildren().addAll(hbox, hbox2, hbox3);
    mainPane.setBottom(vbox);

    for(int i=0;i<5;i++){
      PieceBoard piece = new PieceBoard(new Grid(3,3), gameWindow.getWidth()/10,gameWindow.getHeight()/10,i);
      hbox.getChildren().add(piece);
    }
    for(int i=5;i<10;i++){
      PieceBoard piece = new PieceBoard(new Grid(3,3), gameWindow.getWidth()/10,gameWindow.getHeight()/10,i);
      hbox2.getChildren().add(piece);
    }
    for(int i=10;i<15;i++){
      PieceBoard piece = new PieceBoard(new Grid(3,3), gameWindow.getWidth()/10,gameWindow.getHeight()/10,i);
      hbox3.getChildren().add(piece);
    }

  }

        @Override
    public void initialise() {
    scene.setOnKeyPressed(
        new EventHandler<KeyEvent>() {
          @Override
          public void handle(KeyEvent keyEvent) {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
              gameWindow.startMenu();
            }
          }
        });
    }

}
