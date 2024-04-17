package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MultiplayerScene extends ChallengeScene{

    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    Label label;

    TextField userinput;

    Communicator communicator;

    Leaderboard leaderboard;

    VBox rightsidevbox;

    VBox pieceboardvbox;

    Boolean scoresbuilt = false;




    /**
     * Create a new Multi Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow, Communicator communicator) {
        super(gameWindow);
        this.communicator = communicator;
        List<Pair<String, Integer>> initiatenameandscores = new ArrayList<>();
        initiatenameandscores.add(new Pair<>("Example",0));
        leaderboard = new Leaderboard(initiatenameandscores, "local",communicator);

    }

    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new MultiplayerGame(5,5, communicator);
    }

    @Override
    public void build(){
        communicator.addListener((message) -> Platform.runLater(() -> this.receiveMessage(message)));
        sendscores();
        logger.info("Building " + this.getClass().getName());

        setupGame();
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        pieceboard = new PieceBoard(game.getPieceboard(), gameWindow.getWidth()/4, gameWindow.getWidth()/4);
        nextpieceboard = new PieceBoard(game.getnextpieceboard(), gameWindow.getWidth()/6, gameWindow.getWidth()/6);

        game.setNextPieceListener(this);
        game.setLineClearedListener(this);
        game.setGameLoopListener(this);
        pieceboard.setOnRightClicked(this);
        nextpieceboard.setOnMouseClicked(event -> game.swapcurrentpiece());



        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);
        String path = "src/main/resources/music/game_start.wav";
        String pathtwo = "src/main/resources/music/game.wav";
        Media gamestart = new Media(new File(path).toURI().toString());
        Media gamemusic = new Media(new File(pathtwo).toURI().toString());
        multimedia.playgamemusic(gamestart, gamemusic);




        challengePane.getChildren().add(mainPane);
        rightsidevbox = new VBox();
        mainPane.setRight(rightsidevbox);
        var score = new Text();
        var scoreLabel = new Text("Score");
        var lives = new Text();
        var livesLabel = new Text("Lives");
        score.textProperty().bind(game.getScore().asString());
        lives.textProperty().bind(game.getLives().asString());
        score.getStyleClass().add("score");
        scoreLabel.getStyleClass().add("heading");
        lives.getStyleClass().add("lives");
        livesLabel.getStyleClass().add("heading");
        VBox scorevbox = new VBox(scoreLabel,score);
        scorevbox.setPadding(new Insets(5,0,0,5));
        pieceboardvbox = new VBox(pieceboard,nextpieceboard);
        pieceboardvbox.setAlignment(Pos.CENTER);
        pieceboard.setPadding(new Insets(0,10,10,0));
        nextpieceboard.setPadding(new Insets(0,10,10,0));
        VBox livesvbox = new VBox(livesLabel,lives);
        HBox tophbox = new HBox(scorevbox,livesvbox);
        tophbox.prefHeight(60);
        livesvbox.setPadding(new Insets(5,5,0,0));
        livesvbox.setPrefWidth(800);
        livesvbox.setPrefHeight(60);
        livesvbox.setAlignment(Pos.TOP_RIGHT);
        mainPane.setTop(tophbox);



        rightsidevbox.getChildren().addAll(leaderboard,pieceboardvbox);
        VBox centrevbox = new VBox();
        centrevbox.setAlignment(Pos.CENTER);
        userinput = new TextField();
        userinput.setPromptText("Enter message");
        userinput.setVisible(false);
        centrevbox.setMargin(userinput,new Insets(0,89,0,89));
        label = new Label("Press / to receive/send messages");
        label.getStyleClass().add("messages");
        label.setTextFill(Color.WHITE);
        centrevbox.getChildren().addAll(board,label,userinput);
        mainPane.setCenter(centrevbox);
        userinput.setOnKeyPressed(
                event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        communicator.send("MSG "+userinput.getText());
                        userinput.clear();
                        userinput.deselect();
                    }
                });


        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);


    }

    public void receiveMessage(String message) {

        String[] commands = message.split(" ");
        String command = commands[0];

        switch(command){

            case "MSG" ->{
                String recievedcommand = message.replaceAll("MSG ","");
                String[] usermessages = recievedcommand.split(":");
                if (usermessages.length >= 2) {
                label.setText("<" + usermessages[0] + "> " + usermessages[1]);
                label.getStyleClass().add("messages");
                label.setTextFill(Color.WHITE);
                }else{
                    label.setText("<" + usermessages[0] + ">");
                    label.getStyleClass().add("messages");
                    label.setTextFill(Color.WHITE);
                }
            }

            case "SCORES" ->{
                if(!(scoresbuilt)){
                String[] lines = message.split("\\n");
                lines[0] = lines[0].replaceAll("SCORES ", "");
                loadScores(lines);
                scoresbuilt = true;
                }
            }




        }


    }


    @Override
    public void nextpiece(GamePiece piece) {
        pieceboard.emptygrid();
        pieceboard.displaypiece(piece);
    }

    public void sendscores(){
        communicator.send("SCORES");
    }

    public void loadScores(String[] scores){
        List<Pair<String, Integer>> nameandscores = new ArrayList<>();
        for (String line : scores) {
            String[] parts = line.split(":");
            String name = parts[0];
            Integer score = Integer.valueOf(parts[1]);
            nameandscores.add(new Pair<>(name,score));
            }
        leaderboard = new Leaderboard(nameandscores, "local", communicator);
        rightsidevbox.getChildren().clear();
        rightsidevbox.getChildren().addAll(leaderboard,pieceboardvbox);

    }








@Override
    public void initialise() {
        logger.info("Initialising Challenge");

        game.start();


        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SLASH) {
                logger.info("Pressed");
                userinput.setVisible(true);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                gameWindow.cleanup();
                multimedia.stopmusicplayer();
                gameWindow.startMenu();
                game.stoptimer();
            }
        });

    }

    @Override
    public void gameended(Game game) {
        logger.info("game ended");
        timeline.stop();
        gameWindow.startmultiscores(game,leaderboard.getScoreslist());
    }






}
