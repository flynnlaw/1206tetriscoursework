package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.List;


/**
 * ScoresList is a custom component that takes in a list of name, score pairs and adds them to its own vbox, to display in the game.
 */
public class ScoreList extends VBox {

  /** Set of colours for different name+score pairs */
  public static final Color[] COLOURS = {
    Color.DEEPPINK,
    Color.rgb(228, 37, 24),
    Color.ORANGE,
    Color.YELLOW,
    Color.LIME,
    Color.AQUA,
    Color.BLUE,
    Color.MEDIUMPURPLE,
    Color.PURPLE,
    Color.rgb(74, 65, 42)
  };

  /** Lists of name score pairs */
  protected ObservableList<Pair<String, Integer>> scores;

  /**
   * List of name, score pairs
   */
  protected List<Pair<String, Integer>> scoreslist;

  /**
   * Property of the scoreslist.
   */
  SimpleListProperty scoresProperty;

  /**
   * Build a new scoreList
   *
   * @param scoresList list of name, score pairs
   * @param localoronline whether the list is to be added to local or online list.
   */
  public ScoreList(List<Pair<String, Integer>> scoresList, String localoronline) {
    scores = FXCollections.observableArrayList(scoresList);
    scoresProperty = new SimpleListProperty<>(scores);
    setScoresProperty(localoronline);
    this.scoreslist = scoresList;
  }

  /**
   * Adds scores labels to the leaderboard
   *
   * @param localoronline whether it is to be added to local or online list (not applicable)
   */
  public void setScoresProperty(String localoronline) {
    Label scorelabel;
    int colour = 0;
    if (localoronline.equals("local")) {
      scorelabel = new Label("Local Scores");
    } else {
      scorelabel = new Label("Online Scores");
    }
    scorelabel.getStyleClass().add("heading");
    getChildren().add(scorelabel);
    for (Pair<String, Integer> score : scores) {
      Label label = new Label(score.getKey() + ": " + score.getValue());
      label.getStyleClass().add("scorelist");
      label.setWrapText(true);
      label.setTextFill(COLOURS[colour]);
      getChildren().add(label);
      colour++;
    }
  }

  /**
   * Changes list by clearing all children then re-adding new scores
   *
   * @param scoresList list of name, score pairs
   */
  public void changelist(List<Pair<String, Integer>> scoresList) {
    scores.clear();
    this.getChildren().clear();
    int colour = 0;
    Label scorelabel = new Label("Local Scores");
    scorelabel.getStyleClass().add("heading");
    getChildren().add(scorelabel);
    for (int i = scoresList.size() - 1; i >= 0; i--) {
      Pair<String, Integer> score = scoresList.get(i);
      Label label = new Label(score.getKey() + ": " + score.getValue());
      label.getStyleClass().add("scorelist");
      label.setWrapText(true);
      label.setTextFill(COLOURS[colour]);
      getChildren().add(label);
      colour++;
    }
  }

  /**
   * Returns parallel transition for the scorelist appearing
   *
   * @return parallel transition
   */
  public ParallelTransition getParallelTransition() {
    ParallelTransition transition = new ParallelTransition();
    Duration duration = Duration.seconds(0.2);
    for (int i = 1; i < getChildren().size(); i++) {
      FadeTransition fadeTransition = new FadeTransition(duration, getChildren().get(i));
      fadeTransition.setFromValue(0);
      fadeTransition.setToValue(1);
      fadeTransition.setCycleCount(1);
      fadeTransition.setDelay(Duration.seconds(0.1 * i));
      transition.getChildren().add(fadeTransition);
    }
    return transition;
  }

  /**
   * Returns the score list
   *
   * @return list of name, score pairs
   */
  public List<Pair<String, Integer>> getScoreslist() {
    return this.scoreslist;
  }
}
