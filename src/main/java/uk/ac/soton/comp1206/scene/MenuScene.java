package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    Multimedia multimedia = new Multimedia();

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        System.out.println(System.getProperty("user.dir"));


        String path = "src/main/resources/music/menu.mp3";
        Media backgroundmusic = new Media(new File(path).toURI().toString());
        logger.info("playing background music");
        multimedia.playinloop(backgroundmusic);

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        try{
        Image tetrecsimage = new Image(new FileInputStream("src/main/resources/images/TetrECS.png"));
        ImageView imageView = new ImageView();
        imageView.setImage(tetrecsimage);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(300);
        imageView.setFitWidth(550);
        root.getChildren().add(imageView);
        imageView.setX(100.0);
        RotateTransition animation = new RotateTransition();
        animation.setNode(imageView);
        animation.setDuration(Duration.millis(1000));
        animation.setCycleCount(TranslateTransition.INDEFINITE);
        animation.setByAngle(360);
        }catch(Exception e){
            logger.info("file not found");
        }

        //For now, let us just add a button that starts the game. I'm sure you'll do something way better.
        var button = new Button("Play");
        mainPane.setBottom(button);

        //Bind the button action to the startGame method in the menu
        button.setOnAction(this::startGame);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        logger.info("stopping background music");
        multimedia.stopmusicplayer();
        gameWindow.startChallenge();
    }

}
