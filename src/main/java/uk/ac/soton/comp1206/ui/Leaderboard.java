package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The Leaderboard is a custom component that holds the current scores of active players in the multiplayer challenge scene.
 */

public class Leaderboard extends ScoreList {

  /** Communicator instance for sending and receiving messages from the server */
  private final Communicator communicator;
  /** List of players that have ended their game*/
  private final HashSet<String> deadPeople = new HashSet<>();

  List<Pair<String, Integer>> entries = new ArrayList<>();
  /**
   * Create a new Leaderboard
   * @param scoresList list of name, score pairs
   * @param localOrOnline whether it is to be added to local or online list
   * @param communicator instance of communicator to send/receive messages
   */
  public Leaderboard(
      List<Pair<String, Integer>> scoresList, String localOrOnline, Communicator communicator) {
    super(scoresList, localOrOnline);
    this.communicator = communicator;
    communicator.addListener((message) -> Platform.runLater(() -> this.receiveMessage(message)));
  }

  /**
   * Adds scores labels to the leaderboard
   *
   * @param localOrOnline whether it is to be added to local or online list (not applicable)
   */
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

  /**
   * Changes scores in the leaderboard by clearing all labels and adding them again
   * @param namesAndScores name, score pairs
   */
  public void changescores(String[] namesAndScores) {
    getChildren().clear();
    entries.clear();
    for (String line : namesAndScores) {
      String[] parts = line.split(":");
      String player = parts[0];
      int score = Integer.parseInt(parts[1]);
      var lives = parts[2];
      if(lives.equals("DEAD")){
        deadPeople.add(parts[0]);
      }
      entries.add(new Pair<>(player, score));
    }
    entries.sort((entry1, entry2) -> entry2.getValue() - entry1.getValue());
    this.getChildren().clear();
    int colour = 0;
    for (Pair<String, Integer> score : entries) {
      Text text = new Text(score.getKey() + ": " + score.getValue());
      if (deadPeople.contains(score.getKey())) {
        text.setStrikethrough(true);
      } else {
        text.setFill(COLOURS[colour]);
      }
      text.getStyleClass().add("scorelist");
      getChildren().add(text);
      colour++;
    }
  }

  /**
   * Update the leaderboard when a player dies
   */
  public void updateLeaderboard() {
    // Clear existing entries
    getChildren().clear();

    // Re-add entries based on the updated scores list
    int colour = 0;
    for (Pair<String, Integer> score : entries) {
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


  /**
   * Receive messages from the server and execute differently depending on the command
   * @param message message from the server
   */
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

  /**
   * Return scores list
   * @return scores list
   */
  @Override
  public List<Pair<String, Integer>> getScoreslist() {
    return entries;
  }
}
