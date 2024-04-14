package uk.ac.soton.comp1206.ui;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;
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
    SimpleListProperty scoresProperty;

    public ScoreList(List<Pair<String, Integer>> scoresList) {
        scores = FXCollections.observableArrayList(scoresList);
        scoresProperty = new SimpleListProperty<>(scores);
        setScoresProperty();
    }

    public void setScoresProperty(){
        int colour = 0;
        for (Pair<String, Integer> score : scores) {
            Label label = new Label(score.getKey() + ": " + score.getValue());
            label.getStyleClass().add("scorelist");
            label.setTextFill(COLOURS[colour]);
            getChildren().add(label);
            colour++;
        }
    }

}
