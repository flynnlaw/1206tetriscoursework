package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoreList;

import java.io.FileInputStream;
import java.util.List;

/**
 * The Multiplayer Score Scene displays the top 10 online scores and the scores of the people playing when the game is over
 */
public class MultiplayerScoreScene extends ScoresScene {

  private static final Logger logger = LogManager.getLogger(MultiplayerScoreScene.class);

  /** List of name, score pairs */
  List<Pair<String, Integer>> scores;

  /**
   * Create a new Multiplayer Score Scene
   * @param gameWindow the game Window
   * @param game game instance
   * @param scores scores to load
   */
  public MultiplayerScoreScene(
      GameWindow gameWindow, Game game, List<Pair<String, Integer>> scores) {
    super(gameWindow, game);
    this.scores = scores;
  }

  /** Build the Multiplayer Score Scene window */
  @Override
  public void build() {
    loadOnlineScores();
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);
    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);
    scoredisplay = new ScoreList(scores, "local");
    try {
      VBox topvbox = new VBox();
      Image tetrecsimage = new Image(new FileInputStream("src/main/resources/images/TetrECS.png"));
      ImageView imageView = new ImageView();
      imageView.setImage(tetrecsimage);
      imageView.setPreserveRatio(true);
      imageView.setFitHeight(300);
      imageView.setFitWidth(550);
      topvbox.setAlignment(Pos.CENTER);
      field = new TextField();
      HBox bottomhbox = new HBox();
      topvbox.setPrefSize(600, 400);
      field.setPrefSize(600, 20);
      bottomhbox.setPrefSize(600, 225);
      scoredisplay.setPrefSize(300, 225);
      scoredisplay.setMinSize(300, 225);
      scoredisplay.setAlignment(Pos.CENTER_RIGHT);
      scoredisplay.setMargin(scoredisplay, new Insets(200, 200, 200, 200));
      onlinescoredisplay.setPrefSize(300, 225);
      onlinescoredisplay.setAlignment(Pos.CENTER_RIGHT);
      mainPane.setCenter(topvbox);
      FlowPane flowpane = new FlowPane(bottomhbox);
      mainPane.setBottom(flowpane);
      bottomhbox.getChildren().addAll(scoredisplay, onlinescoredisplay);
      SequentialTransition sequentialTransition = new SequentialTransition();
      Button button = new Button("Submit");
      topvbox.getChildren().addAll(imageView, field, button);

      // Set duration for each fade transition
      Duration duration = Duration.seconds(0.2);

      // Add FadeTransition for each child node
      ParallelTransition parallelTransition = new ParallelTransition();

      // Add FadeTransitions for each child node in score display one
      for (int i = 1; i < scoredisplay.getChildren().size(); i++) {
        FadeTransition fadeTransition =
            new FadeTransition(Duration.seconds(0.2), scoredisplay.getChildren().get(i));
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setCycleCount(1);
        fadeTransition.setDelay(Duration.seconds(0.1 * i));
        parallelTransition.getChildren().add(fadeTransition);
      }

      // Add FadeTransitions for each child node in score display two
      for (int i = 1; i < onlinescoredisplay.getChildren().size(); i++) {
        FadeTransition fadeTransition =
            new FadeTransition(Duration.seconds(0.2), onlinescoredisplay.getChildren().get(i));
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setCycleCount(1);
        fadeTransition.setDelay(Duration.seconds(0.1 * i));
        parallelTransition.getChildren().add(fadeTransition);
      }

      field.setVisible(false);
      button.setVisible(false);
      if (gamestate.getScore().get() > getMinimumOnlineScore()) {
        field.setVisible(true);
        button.setVisible(true);
        scoredisplay.setVisible(false);
        onlinescoredisplay.setVisible(false);

        button.setOnAction(
            actionEvent -> {
              if (gamestate.getScore().get() > getMinimumOnlineScore()) {
                writeonlinescore(gamestate.getScore().getValue());
              }
              button.setVisible(false);
              field.setVisible(false);
              scoredisplay.setVisible(true);
              onlinescoredisplay.setVisible(true);

              ParallelTransition parallelTransitionifbuttonispressed = new ParallelTransition();
              parallelTransitionifbuttonispressed
                  .getChildren()
                  .addAll(scoredisplay.getParallelTransition().getChildren());
              parallelTransitionifbuttonispressed
                  .getChildren()
                  .addAll(onlinescoredisplay.getParallelTransition().getChildren());
              parallelTransitionifbuttonispressed.play();
            });
      } else {
        parallelTransition.play();
      }

    } catch (Exception e) {
      logger.info("file not found");
    }
  }

  /** Load scores by requesting scores from online, parsing the result and adding it to an online score list.*/
  public void loadOnlineScores() {
    communicator.send("HISCORES");
    communicator.addListener(
        new CommunicationsListener() {
          @Override
          public void receiveCommunication(String communication) {
            String[] commands = communication.split(" ");
            if (commands[0].equals("HISCORES")) {
              String[] lines = communication.split("\\n");
              lines[0] = lines[0].replaceAll("HISCORES ", "");
              for (String highscore : lines) {
                String[] parts = highscore.split(":");
                Pair<String, Integer> toadd =
                    new Pair<>(parts[0], Integer.parseInt(parts[1].trim()));
                onlinescorelist.add(toadd);
              }
              setonlinescoredisplay(onlinescorelist);
            }
          }
        });
  }
}
