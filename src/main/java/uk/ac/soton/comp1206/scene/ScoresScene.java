package uk.ac.soton.comp1206.scene;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoreList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScoresScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    private final List<Pair<String, Integer>> scoresList = new ArrayList<>();

    Game gamestate;
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        this.gamestate = game;
        logger.info("Creating Scores Scene");
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
        List<Pair<String,Integer>> scores = loadScores();
        ScoreList scoredisplay = new ScoreList(scores);
        ScoreList scoredisplaytwo = new ScoreList(scores);
        scoredisplay.setMinWidth(300);
        scoredisplay.setMinHeight(300);
        scoredisplay.setAlignment(Pos.BOTTOM_CENTER);
        VBox entirescreenvbox = new VBox();
        entirescreenvbox.setPrefWidth(800);
        entirescreenvbox.setPrefHeight(400);
        HBox bottomhbox = new HBox();
        bottomhbox.setMinWidth(600);
        bottomhbox.setPrefHeight(300);
        scoredisplaytwo.setMinWidth(300);
        scoredisplaytwo.setMinHeight(300);


        TextField textentry = new TextField("Enter your name");
        try{
            Image tetrecsimage = new Image(new FileInputStream("src/main/resources/images/TetrECS.png"));
            ImageView imageView = new ImageView();
            imageView.setImage(tetrecsimage);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            entirescreenvbox.getChildren().addAll( imageView,textentry, bottomhbox);
            }catch(Exception e){
        }
        mainPane.setCenter(entirescreenvbox);
        bottomhbox.getChildren().addAll(scoredisplay,scoredisplaytwo);

    }

    public List<Pair<String,Integer>> loadScores(){
        try (BufferedReader filereader = new BufferedReader(new FileReader("src/main/resources/localscores.txt"))) {
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
