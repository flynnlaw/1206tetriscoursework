package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class LobbyScene extends BaseScene{

    Communicator communicator = gameWindow.getCommunicator();
    VBox channelvbox = new VBox();
    private Timeline updateTimer;

    TextFlow chattextflow;
    TextFlow usernames;

    ScrollPane scroller;

    BorderPane chatwindow;

    String nickname;

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    @Override
    public void build() {
        communicator.addListener((message) -> Platform.runLater(() -> this.receiveMessage(message)));
        channelvbox.setPadding(new Insets(0,0,0,20));
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);
        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);
        VBox channelsandhostvbox = new VBox();
        channelsandhostvbox.setPrefSize(181,400);
        mainPane.setLeft(channelsandhostvbox);
        VBox hostnewgamevbox = new VBox();
        Label label = new Label("Host New Game");
        label.getStyleClass().add("smallheading");
        label.setTextFill(Color.WHITE);
        label.setPadding(new Insets(0,0,0,10));
        TextField newgamename = new TextField();
        newgamename.setVisible(false);
        label.setOnMouseClicked(mouseEvent -> newgamename.setVisible(true));
        newgamename.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Send command when Enter is pressed
                String gameName = newgamename.getText().trim();
                if (!gameName.isEmpty()) {
                    communicator.send("CREATE " + gameName);
                    // Clear the text after sending the command
                    newgamename.clear();
                    // Hide the TextFlow after sending the command
                    newgamename.setVisible(false);
                }
            }
        });
        hostnewgamevbox.getChildren().addAll(label,newgamename);
        hostnewgamevbox.setPrefSize(158,54);
        channelsandhostvbox.getChildren().addAll(hostnewgamevbox,channelvbox);
        HBox titleshbox = new HBox();
        titleshbox.setPrefSize(600,70);
        mainPane.setTop(titleshbox);
        chatwindow = new BorderPane();
        VBox leftvbox = new VBox();
        leftvbox.setPrefSize(46,271);
        VBox rightvbox = new VBox();
        rightvbox.setPrefSize(30,271);
        VBox bottomvbox = new VBox();
        bottomvbox.setPrefSize(442,41);
        chatwindow.setLeft(leftvbox);
        chatwindow.setRight(rightvbox);
        chatwindow.setBottom(bottomvbox);
//        leftvbox.setStyle("-fx-border-width: 0 1px 0 0; -fx-border-color: white;");


        mainPane.setCenter(chatwindow);
        requestchannels();
        initializeTimer();





    }

    private void initializeTimer() {
        // Create a timeline with a keyframe that repeats every 5 seconds
        updateTimer = new Timeline(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Request current channels using the communicator
                requestchannels();
            }
        }));

        // Set the cycle count to indefinite so that the timer repeats indefinitely
        updateTimer.setCycleCount(Timeline.INDEFINITE);

        // Start the timer
        updateTimer.play();
    }

    public void requestchannels() {
        communicator.send("LIST");
    }

    public void addchannelstovbox(String[] channels) {
        Set<String> existingChannels = new HashSet<>();

        // Add existing channel labels to the set
        for (Node node : channelvbox.getChildren()) {
            if (node instanceof Label) {
                existingChannels.add(((Label) node).getText());
            }
        }

        // Add new channels and remove non-existing channels
        for (String channel : channels) {
            // If channel already exists, skip
            if (existingChannels.contains(channel)) {
                continue;
            }

            // Create a new label for the new channel
            Label lobby = new Label(channel);
            lobby.getStyleClass().add("channelItem");
            lobby.setTextFill(Color.WHITE);
            lobby.setOnMouseClicked(mouseEvent -> this.makechatwindow(channel));
            channelvbox.getChildren().add(lobby);
        }

        // Remove labels associated with non-existing channels
        Iterator<Node> iterator = channelvbox.getChildren().iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node instanceof Label && !Arrays.asList(channels).contains(((Label) node).getText())) {
                iterator.remove();
            }
        }
    }

    public void makechatwindow(String channel){
        logger.info("chat");
        chattextflow = new TextFlow();
        scroller = new ScrollPane();
        scroller.getStyleClass().add("chatwindow");
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setContent(chattextflow);
        scroller.setFitToWidth(true);
        chatwindow.setCenter(scroller);
        chatwindow.getStyleClass().add("scroller");
        usernames = new TextFlow();
        usernames.setPrefSize(442,25);
        usernames.getStyleClass().add("scroller");
        usernames.setPadding(new Insets(0,0,0,48));
        chatwindow.setTop(usernames);
        updateTimer.stop();
        communicator.send("JOIN "+channel);
    }

    public void fillusernames(String[] users){
        Text usernameText;
        this.usernames.getChildren().clear();
        usernameText = new Text(nickname+" ");
        usernameText.setFill(Color.WHITE);
        usernameText.getStyleClass().add("usernamesbold");
        this.usernames.getChildren().add(usernameText);
        for (String username : users) {
            // If the username matches the user's own username, make it bold
            if (!username.equals(nickname)) {
                usernameText = new Text(username+" ");
                usernameText.setFill(Color.WHITE);
                usernameText.getStyleClass().add("usernames");
                this.usernames.getChildren().add(usernameText);
            }
            // Add the username Text to the TextFlow

        }

    }

    public void receiveMessage(String message) {

        String[] commands = message.split(" ");
        String command = commands[0];

        switch(command){

            case "CHANNELS" ->{
                String[] lines = message.split("\\n");
                lines[0] = lines[0].replaceAll("CHANNELS ", "");
                Platform.runLater(() -> addchannelstovbox(lines));
            }

            case "NICK" ->{
                nickname = commands[1];
            }

            case "USERS" ->{
                String[] usernames = commands[1].split("\\n");
                fillusernames(usernames);
            }

            case "MSG" ->{
                String[] usermessage = message.replaceAll("MSG ","").split(":");
                Text newmessage = new Text("<"+usermessage[0]+"> "+usermessage[1]+"\n");
                newmessage.getStyleClass().add("messages");
                newmessage.setFill(Color.WHITE);
                chattextflow.getChildren().add(newmessage);
            }

            case "JOIN" ->{
                makechatwindow(commands[1]);
            }

            case "ERROR" ->{
                logger.info("join");
            }


        }


    }




    @Override
    public void initialise() {

        scene.setOnKeyPressed(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent keyEvent) {
                        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                            communicator.send("PART");
                            gameWindow.startMenu();
                        }
                    }
                });
    }

}
