package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.ArrayList;
import java.util.List;

public class Leaderboard extends ScoreList {

    private final Communicator communicator;
    private final ArrayList<String> deadPeople = new ArrayList<>();

    public Leaderboard(List<Pair<String, Integer>> scoresList, String localOrOnline, Communicator communicator) {
        super(scoresList, localOrOnline);
        this.communicator = communicator;
        communicator.addListener((message) -> Platform.runLater(() -> this.receiveMessage(message)));
    }

    public void setScoresProperty(String localOrOnline) {
        int colour = 0;
        for (Pair<String, Integer> score : scores) {
            Text text = new Text(score.getKey() + ": " + score.getValue());
            text.getStyleClass().add("scorelist");
            text.setFill(COLOURS[colour]);
            getChildren().add(text);
            colour++;
        }
    }

    public void changescores(String[] namesAndScores) {
        List<Pair<String, Integer>> entries = new ArrayList<>();
        for (String line : namesAndScores) {
            String[] parts = line.split(":");
            String player = parts[0];
            int score = Integer.parseInt(parts[1]);
            entries.add(new Pair<>(player, score));
        }
        entries.sort((entry1, entry2) -> entry2.getValue() - entry1.getValue());
        this.getChildren().clear();
        int colour = 0;
        for (Pair<String, Integer> score : entries) {
            Text text = new Text(score.getKey() + ": " + score.getValue());
            text.getStyleClass().add("scorelist");
            text.setFill(COLOURS[colour]);
            getChildren().add(text);
            colour++;
        }
    }

    public void updateLeaderboard() {
        // Clear existing entries
        getChildren().clear();

        // Re-add entries based on the updated scores list
        int colour = 0;
        for (Pair<String, Integer> score : scores) {
            Text text = new Text(score.getKey() + ": " + score.getValue());
            text.getStyleClass().add("scorelist");
            if (deadPeople.contains(score.getKey())) {
                text.setStrikethrough(true);
            } else {
                text.setFill(COLOURS[colour]);
            }
            getChildren().add(text);
            colour++;
        }
    }

    public void receiveMessage(String message) {
        String[] commands = message.split(" ");
        String command = commands[0];

        switch (command) {
            case "SCORES" -> {
                String[] lines = message.split("\\n");
                lines[0] = lines[0].replaceAll("SCORES ", "");
                changescores(lines);
            }
            case "DIE" -> {
                String playerName = commands[1];
                deadPeople.add(playerName);
                updateLeaderboard();
            }
        }
    }

    @Override
    public List<Pair<String, Integer>> getScoreslist() {
        return super.getScoreslist();
    }
}