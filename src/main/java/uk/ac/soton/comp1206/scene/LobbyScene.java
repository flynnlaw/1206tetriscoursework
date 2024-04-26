package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.io.File;
import java.util.*;

/**
 * The Lobby Scene houses components for joining online games and chatting with users in the lobby.
 */
public class LobbyScene extends BaseScene {

  /** Communicator instance for sending messages to the server */
  Communicator communicator = gameWindow.getCommunicator();

  /** VBox used for displaying available channels */
  VBox channelvbox = new VBox();

  /** Timer used for pinging the server every 5 seconds for new games */
  private Timeline updateTimer;

  /** Multimedia class for playing audio/sound */
  Multimedia multimedia = new Multimedia();

  /** Set of channels that are open */
  private Set<String> openedChannels = new HashSet<>();

  /** TextFlow for the chat within each channel */
  TextFlow chattextflow;

  /** TextFlow for the usernames in each channel */
  TextFlow usernames;

  /** Allows the textflow to have a scrollbar when messages fill the textflow */
  ScrollPane scroller;

  /** BorderPane that chat window components are added to */
  BorderPane chatwindow;

  /** Nickname of the user of this client */
  String nickname;

  /** Main Pane that all components are added to */
  BorderPane mainPane;

  /** Value indicating whether the user is currently in a channel */
  Boolean inchannel = false;

  /** VBox housing the chat functions */
  VBox chatboxvbox;

  /** VBox holding the text input */
  VBox bottomvbox;

  /** HBox holding the game buttons */
  HBox buttonhbox;

  /** Button allowing the game to start */
  Button startgamebutton;

  private static final Logger logger = LogManager.getLogger(LobbyScene.class);

  /**
   * Create a new Lobby Scene
   *
   * @param gameWindow the game Window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
    String path = "src/main/resources/music/menu.mp3";
    String path1 = "src/main/resources/music/suiiianthem.mp3";
    Media backgroundmusic = new Media(new File(path).toURI().toString());
    multimedia.playmenumusic(backgroundmusic);
  }

  @Override
  public void build() {
    initializeTimer();
    communicator.addListener((message) -> Platform.runLater(() -> this.receiveMessage(message)));
    channelvbox.setPadding(new Insets(0, 0, 0, 20));
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);
    mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);
    VBox channelsandhostvbox = new VBox();
    channelsandhostvbox.setPrefSize(181, 400);
    mainPane.setLeft(channelsandhostvbox);
    VBox hostnewgamevbox = new VBox();
    Label label = new Label("Host New Game");
    label.getStyleClass().add("smallheading");
    label.setTextFill(Color.WHITE);
    label.setPadding(new Insets(0, 0, 0, 10));
    TextField newgamename = new TextField();
    newgamename.setVisible(false);
    label.setOnMouseClicked(mouseEvent -> newgamename.setVisible(true));
    newgamename.setOnKeyPressed(
        event -> {
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
    hostnewgamevbox.getChildren().addAll(label, newgamename);
    hostnewgamevbox.setPrefSize(158, 54);
    channelsandhostvbox.getChildren().addAll(hostnewgamevbox, channelvbox);
    HBox titleshbox = new HBox();
    titleshbox.setPrefSize(600, 70);
    mainPane.setTop(titleshbox);

    //        leftvbox.setStyle("-fx-border-width: 0 1px 0 0; -fx-border-color: white;");

    requestchannels();
  }

  /** Starts the timer Every 5 seconds, it requests for new channels */
  private void initializeTimer() {
    updateTimer =
        new Timeline(
            new KeyFrame(
                Duration.seconds(5),
                event -> {
                  // Code to execute every 5 seconds
                  logger.info("Timer fired, requesting channels...");
                  requestchannels();
                }));
    updateTimer.setCycleCount(Timeline.INDEFINITE); // Repeat indefinitely
    updateTimer.play(); // Start the timer
  }

  /** Sends a channel request to the server */
  public void requestchannels() {
    communicator.send("LIST");
  }

  /**
   * Adds channels passed in to the vbox, adds a listener to each added channel to open its chat
   * window
   *
   * @param channels channels received
   */
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

      // Add event handler only if makechatwindow hasn't been called for this channel
      if (!openedChannels.contains(channel)) {
        lobby.setOnMouseClicked(
            mouseEvent -> {
              communicator.send("JOIN " + channel);
              if (!(inchannel)) {
                this.makechatwindow(channel);
                lobby.setTextFill(Color.YELLOW);
                // Add the channel to the set to indicate makechatwindow has been called
                openedChannels.add(channel);
              }
            });
      }

      // Add the label to channelvbox
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

  /**
   * Constructs a chat window for the channel passed in
   *
   * @param channel the channel to join
   */
  public void makechatwindow(String channel) {
    chatwindow = new BorderPane();
    mainPane.setCenter(chatwindow);
    bottomvbox = new VBox();
    chatboxvbox = new VBox();
    VBox buttonvbox = new VBox();
    buttonhbox = new HBox();
    buttonvbox.getChildren().add(buttonhbox);
    bottomvbox.getChildren().addAll(chatboxvbox, buttonvbox);
    bottomvbox.setPrefSize(442, 160);
    chatwindow.setBottom(bottomvbox);
    chatwindow.setMargin(chatwindow, new Insets(0, 10, 0, 100));
    logger.info("chat");
    chattextflow = new TextFlow();
    scroller = new ScrollPane();
    scroller.getStyleClass().add("chatwindow");
    scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroller.setContent(chattextflow);
    scroller.setFitToWidth(true);
    scroller.setPadding(new Insets(10, 0, 0, 10));
    scroller.setPrefSize(332, 200);
    chatwindow.setCenter(scroller);
    chatwindow.getStyleClass().add("scroller");
    usernames = new TextFlow();
    usernames.setPrefSize(442, 25);
    usernames.getStyleClass().add("scroller");
    usernames.setPadding(new Insets(0, 0, 0, 10));
    chatwindow.setTop(usernames);
    updateTimer.stop();
    Text intro =
        new Text(
            """
        Welcome to the lobby
        Type /nick [your nickname] to change your nickname

        """);
    intro.getStyleClass().add("messages");
    intro.setFill(Color.WHITE);
    chattextflow.getChildren().add(intro);

    // Create the user input text field
    TextField userinput = new TextField();
    userinput.setPadding(new Insets(0, 0, 0, 10));
    userinput.setPromptText("Enter message");

    scroller.getPrefWidth();
    userinput.setPrefSize(500, 30);
    chatboxvbox.getChildren().add(userinput);
    chatboxvbox.getStyleClass().add("scroller");

    // Set an event handler for the user input text field
    userinput.setOnKeyPressed(
        event -> {
          if (event.getCode() == KeyCode.ENTER) {
            // Send command when Enter is pressed
            String text = userinput.getText().trim();
            if (text.split(" ")[0].toUpperCase().equals("/NICK")) {
              communicator.send("NICK " + text.replaceAll("(?i)/nick\\s*", ""));
              userinput.clear();
            } else {
              communicator.send("MSG " + text);
              userinput.clear();
            }
          }
        });

    startgamebutton = new Button("Start Game");
    Button leavegamebutton = new Button("Leave Game");
    buttonhbox.setMargin(leavegamebutton, new Insets(10, 0, 0, 345));
    buttonhbox.setMargin(startgamebutton, new Insets(10, 0, 0, 0));
    startgamebutton.setVisible(false);
    buttonhbox.getChildren().addAll(startgamebutton, leavegamebutton);
    leavegamebutton.setOnMouseClicked(
        mouseEvent -> {
          communicator.send("PART");
          requestchannels();
          updateTimer.play();
          mainPane.setCenter(null);
          inchannel = false;
          for (Node node : channelvbox.getChildren()) {
            // Check if the node is a label
            if (node instanceof Label) {
              // Update the text color to white
              ((Label) node).setTextFill(Color.WHITE);
            }
          }
        });

    startgamebutton.setOnMouseClicked(
        mouseEvent -> {
          communicator.send("START");
          multimedia.stopmusicplayer();
          gameWindow.startMultiplayerChallenge(communicator);
        });

    inchannel = true;
    String path = "src/main/resources/sounds/message.wav";
    Media messagerecieved = new Media(new File(path).toURI().toString());
    multimedia.setaudioplayer(messagerecieved);
  }

  /**
   * Populates the usernames given in the passed in array into the username texeflow
   *
   * @param users the users in the channel
   */
  public void fillusernames(String[] users) {
    Text usernameText;
    this.usernames.getChildren().clear();
    usernameText = new Text(nickname + " ");
    usernameText.setFill(Color.WHITE);
    usernameText.getStyleClass().add("usernamesbold");
    this.usernames.getChildren().add(usernameText);
    for (String username : users) {
      // If the username matches the user's own username, make it bold
      if (!username.equals(nickname)) {
        usernameText = new Text(username + " ");
        usernameText.setFill(Color.WHITE);
        usernameText.getStyleClass().add("usernames");
        this.usernames.getChildren().add(usernameText);
      }
    }
  }

  /**
   * Removes text class from the usernames textflow when someone has left the lobby
   *
   * @param player the player to delete
   */
  public void removeUsername(String player) {
    Iterator<Node> iterator = usernames.getChildren().iterator();
    while (iterator.hasNext()) {
      Node node = iterator.next();
      if (node instanceof Text) {
        Text text = (Text) node;
        if (text.getText().trim().equals(player)) {
          iterator.remove();
        }
      }
    }
  }

  /**
   * Receives a message from the server and does a different function depending on the type.
   *
   * @param message message received
   */
  public void receiveMessage(String message) {

    String[] commands = message.split(" ");
    String command = commands[0];

    switch (command) {
      case "CHANNELS" -> {
        String[] lines = message.split("\\n");
        lines[0] = lines[0].replaceAll("CHANNELS ", "");
        Platform.runLater(() -> addchannelstovbox(lines));
      }

      case "NICK" -> {
        String[] playername = commands[1].split(":");
        if (!(playername.length == 2)) {
          nickname = playername[0];
        }
      }

      case "USERS" -> {
        String[] usernames = commands[1].split("\\n");
        fillusernames(usernames);
      }

      case "MSG" -> {
        String[] usermessage = message.replaceAll("MSG ", "").split(":");
        if (usermessage.length >= 2) {
          Text newmessage = new Text("<" + usermessage[0] + "> " + usermessage[1] + "\n");
          newmessage.getStyleClass().add("messages");
          newmessage.setFill(Color.WHITE);
          chattextflow.getChildren().add(newmessage);
        } else {
          Text newmessage = new Text("<" + usermessage[0] + ">\n");
          newmessage.getStyleClass().add("messages");
          newmessage.setFill(Color.WHITE);
          chattextflow.getChildren().add(newmessage);
        }
        String path = "src/main/resources/sounds/message.wav";
        Media messagerecieved = new Media(new File(path).toURI().toString());
        multimedia.setaudioplayer(messagerecieved);
      }

      case "JOIN" -> {
        if (!(inchannel)) {
          makechatwindow(commands[1]);
          inchannel = true;
          requestchannels();
          logger.info("joined " + commands[1]);
        }
      }

      case "ERROR" -> {
        String errormessage = message.replaceAll("ERROR ", "");
        var alertbox = new Alert(Alert.AlertType.ERROR, errormessage);
        alertbox.showAndWait();
      }

      case "HOST" -> {
        startgamebutton.setVisible(true);
      }

      case "START" -> {
        gameWindow.startMultiplayerChallenge(communicator);
        multimedia.stopmusicplayer();
      }

      case "DIE" -> {
        removeUsername(commands[1]);
      }
    }
  }

  /** Initialise the scene and start the game Handles keyboard inputs for escape */
  @Override
  public void initialise() {
    scene.setOnKeyPressed(
        new EventHandler<KeyEvent>() {
          @Override
          public void handle(KeyEvent keyEvent) {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
              if (inchannel) {
                communicator.send("PART");
              }
              updateTimer.stop();
              multimedia.stopmusicplayer();
              gameWindow.startMenu();
            }
          }
        });
  }
}
