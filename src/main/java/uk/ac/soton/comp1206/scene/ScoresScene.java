package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
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
import java.util.Comparator;
import java.util.List;

public class ScoresScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    private final List<Pair<String, Integer>> scoresList = new ArrayList<>();

    TextField field;

    ScoreList scoredisplay;

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
        scoredisplay = new ScoreList(scores);
        ScoreList scoredisplaytwo = new ScoreList(scores);
        try{
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
            topvbox.setPrefSize(600,400);
            field.setPrefSize(600,20);
            bottomhbox.setPrefSize(600,225);
            scoredisplay.setPrefSize(300,225);
            scoredisplay.setAlignment(Pos.CENTER_RIGHT);
            scoredisplaytwo.setPrefSize(300,225);
            scoredisplaytwo.setAlignment(Pos.CENTER_RIGHT);
            mainPane.setCenter(topvbox);
            FlowPane flowpane = new FlowPane(bottomhbox);
            mainPane.setBottom(flowpane);
            bottomhbox.getChildren().addAll(scoredisplay,scoredisplaytwo);
            SequentialTransition sequentialTransition = new SequentialTransition();
            Button button = new Button("Submit");
            topvbox.getChildren().addAll(imageView,field,button);

            // Set duration for each fade transition
            Duration duration = Duration.seconds(0.2);

            // Add FadeTransition for each child node
            for (int i = 1; i < scoredisplay.getChildren().size(); i++) {
                FadeTransition fadeTransition = new FadeTransition(duration, scoredisplay.getChildren().get(i));
                fadeTransition.setFromValue(0);
                fadeTransition.setToValue(1);
                fadeTransition.setCycleCount(1);
                fadeTransition.setDelay(Duration.seconds(0.1)); // Delay each transition by i seconds
                sequentialTransition.getChildren().add(fadeTransition);
            }
            field.setVisible(false);
            button.setVisible(false);
            if(gamestate.getScore().get()>getMinimumScore()){
                field.setVisible(true);
                button.setVisible(true);
                scoredisplay.setVisible(false);
                scoredisplaytwo.setVisible(false);

                button.setOnAction(actionEvent -> {
                    addtoscores();
                    button.setVisible(false);
                    field.setVisible(false);
                    scoredisplay.setVisible(true);
                    scoredisplaytwo.setVisible(true);

                    ParallelTransition parallelTransition = new ParallelTransition();
                    parallelTransition.getChildren().addAll(scoredisplay.getParallelTransition().getChildren());
                    parallelTransition.getChildren().addAll(scoredisplaytwo.getParallelTransition().getChildren());
                    parallelTransition.play();
                    writeScores(scoresList);
                });
            }else{
                sequentialTransition.play();
            }



        }catch(Exception e){
            logger.info("file not found");
        }




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

    public int getMinimumScore(){
        return scoresList.get(scoresList.size()-1).getValue();
    }

    public void addtoscores(){
        changescores(field.getText());
    }

    public void changescores(String name){
        scoresList.add(new Pair<>(name, gamestate.getScore().get()));
        scoresList.sort(Comparator.comparing(Pair::getValue));
        scoresList.remove(scoresList.get(0));
        scoredisplay.changelist(scoresList);
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
