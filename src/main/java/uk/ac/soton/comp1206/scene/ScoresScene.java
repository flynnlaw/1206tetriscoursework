package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;
import uk.ac.soton.comp1206.ui.ScoreList;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The Score Scene displays the top 10 online scores and the top 10 local scores when the game is over.
 */

public class ScoresScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ScoresScene.class);

  /** List of name, score pairs from the local score file */
  protected final List<Pair<String, Integer>> scoresList = new ArrayList<>();

  /** List of name, score pairs from the server */
  protected final List<Pair<String, Integer>> onlinescorelist = new ArrayList<>();

  /** Communicator instance for sending server requests */
  Communicator communicator = gameWindow.getCommunicator();

  /** Field for entering name when user has beaten a local/online score */
  TextField field;

  /** Display showing local scores */
  ScoreList scoredisplay;

  /** Display showing online scores */
  ScoreList onlinescoredisplay;

  /** Multimedia for playing audio/sounds */
  Multimedia multimedia = new Multimedia();

  /** State of the game at the end, passed into constructor */
  Game gamestate;

  /**
   * Create a new Multiplayer Score Scene
   *
   * @param gameWindow the game Window
   * @param game game state
   */
  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.gamestate = game;
    logger.info("Creating Scores Scene");
    String path = "src/main/resources/music/end.wav";
    Media gameend = new Media(new File(path).toURI().toString());
    multimedia.setaudioplayer(gameend);
  }

  /** Build the Score Scene window */
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
    List<Pair<String, Integer>> scores = loadScores();
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
      field.setPromptText("Your score has beaten a local score, enter your name!");
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
      if (gamestate.getScore().get() > getMinimumScore()
          || gamestate.getScore().get() > getMinimumOnlineScore()) {
        field.setVisible(true);
        button.setVisible(true);
        scoredisplay.setVisible(false);
        onlinescoredisplay.setVisible(false);

        button.setOnAction(
            actionEvent -> {
              addtoscores("local");
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
              writeScores(scoresList);
            });
      } else {
        parallelTransition.play();
      }

    } catch (Exception e) {
      logger.info("file not found");
    }
  }

  /**
   * Loads scores from a local file and returns them as a list of name, score pairs
   *
   * @return list of name, score pairs
   */
  public List<Pair<String, Integer>> loadScores() {
    try (BufferedReader filereader =
        new BufferedReader(new FileReader("src/main/resources/localscores.txt"))) {
      String line;
      while ((line = filereader.readLine()) != null) {
        String[] parts = line.split(":");
        if (parts.length == 2) {
          String name = parts[0];
          int score = Integer.parseInt(parts[1].trim()); // Trim to remove leading/trailing spaces
          scoresList.add(new Pair<>(name, score));
        } else {
          System.err.println("Invalid line format: " + line);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return scoresList;
  }

  /**
   * Write local scores to a "localscores.txt" file
   *
   * @param scoresList name, score pairs from the game
   */
  public void writeScores(List<Pair<String, Integer>> scoresList) {
    File file = new File("src/main/resources/localscores.txt");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      // Write each score in the list to the file
      for (int i = scoresList.size() - 1; i >= 0; i--) {
        Pair<String, Integer> score = scoresList.get(i);
        writer.write(score.getKey() + ":" + score.getValue());
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Returns minimum score from local file
   * @return minimum score
   */
  public int getMinimumScore() {
    return scoresList.get(scoresList.size() - 1).getValue();
  }

  /** Returns minimum score from server
   * @return minimum online score
   */
  public int getMinimumOnlineScore() {
    return onlinescoredisplay.getScoreslist().get(9).getValue();
  }

  /**
   * Adds the score passed in into the scoreslist
   *
   * @param localoronline to be added to either local or online list
   */
  public void addtoscores(String localoronline) {
    changescores(field.getText(), localoronline);
  }

  /**
   * Inserts new name, score pair into scoreslist, sorts the list and removes the lowest value
   *
   * @param name Name to put in scorelist
   * @param localoronline to be added to either local or online list
   */
  public void changescores(String name, String localoronline) {
    if (localoronline.equals("local")) {
      scoresList.add(new Pair<>(name, gamestate.getScore().get()));
      scoresList.sort(Comparator.comparing(Pair::getValue));
      scoresList.remove(scoresList.get(0));
      scoredisplay.changelist(scoresList);
    } else {
      onlinescorelist.add(new Pair<>(name, gamestate.getScore().get()));
      onlinescorelist.sort(Comparator.comparing(Pair::getValue));
      onlinescorelist.remove(onlinescorelist.get(0));
      onlinescoredisplay.changelist(onlinescorelist);
    }
  }

  /**
   * Ping server for high scores, parse received message and adds the scores to the online scorelist
   */
  public void loadOnlineScores() {
    communicator.send("HISCORES");
    communicator.addListener(
        new CommunicationsListener() {
          @Override
          public void receiveCommunication(String communication) {
            String[] lines = communication.split("\\n");
            lines[0] = lines[0].replaceAll("HISCORES ", "");
            for (String highscore : lines) {
              String[] parts = highscore.split(":");
              Pair<String, Integer> toadd = new Pair<>(parts[0], Integer.parseInt(parts[1].trim()));
              onlinescorelist.add(toadd);
            }
            setonlinescoredisplay(onlinescorelist);
          }
        });
  }

  /**
   * Initialises observable list with the online scoreslist passed in
   *
   * @param list list to be made observable
   */
  public void setonlinescoredisplay(List<Pair<String, Integer>> list) {
    onlinescoredisplay = new ScoreList(list, "online");
  }

  /**
   * Sends communicator message to server with new high score after game ends
   *
   * @param score score to send
   */
  public void writeonlinescore(int score) {
    String name = field.getText();
    communicator.send("HISCORE <" + name + ">:<" + score + ">");
    addtoscores("online");
  }

  /** Initialise the scores scene Handle keyboard inputs for exit */
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
