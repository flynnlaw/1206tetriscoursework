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

public class ScoreList extends VBox {


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
            Color.rgb(74,65,42)
    };
    private ObservableList<Pair<String, Integer>> scores;

    private List<Pair<String, Integer>> scoreslist;
    SimpleListProperty scoresProperty;

    public ScoreList(List<Pair<String, Integer>> scoresList, String localoronline) {
        scores = FXCollections.observableArrayList(scoresList);
        scoresProperty = new SimpleListProperty<>(scores);
        setScoresProperty(localoronline);
        this.scoreslist = scoresList;

    }

    public void setScoresProperty(String localoronline){
        Label scorelabel;
        int colour = 0;
    if (localoronline.equals("local")) {
      scorelabel = new Label("Local Scores");
        }
    else{
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

    public void changelist(List<Pair<String, Integer>> scoresList){
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

    public List<Pair<String, Integer>> getScoreslist(){return this.scoreslist;}

}
