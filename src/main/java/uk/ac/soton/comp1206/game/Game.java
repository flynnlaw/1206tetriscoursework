package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.media.Media;
import javafx.scene.shape.Line;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.util.*;
import java.io.File;

import javafx.util.Pair;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game{

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    protected final Grid pieceboard;

    protected final Grid nextpieceboard;

    private NextPieceListener nextPieceListener;

    private LineClearedListener lineClearedListener;

    private GameLoopListener gameLoopListener;

    Multimedia multimedia = new Multimedia();


    SimpleIntegerProperty score = new SimpleIntegerProperty(0);
    SimpleIntegerProperty level = new SimpleIntegerProperty(0);
    SimpleIntegerProperty lives = new SimpleIntegerProperty(0);
    SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    GamePiece currentPiece;

    GamePiece followingPiece;

    String rotatesoundpath = "src/main/resources/sounds/rotate.wav";
    String clearsoundpath = "src/main/resources/sounds/clear.wav";

    String failsoundpath = "src/main/resources/sounds/fail.wav";

    String placesoundpath = "src/main/resources/sounds/place.wav";
    String suiiisoundpath = "src/main/resources/sounds/suiii-loud.mp3";


    Media rotatesound = new Media(new File(rotatesoundpath).toURI().toString());
    Media clearsound = new Media(new File(clearsoundpath).toURI().toString());
    Media failsound = new Media(new File(failsoundpath).toURI().toString());

    Media placesound = new Media(new File(placesoundpath).toURI().toString());
    Media suiiisound = new Media(new File(suiiisoundpath).toURI().toString());

   private Timer timer;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
        this.pieceboard = new Grid(3,3);
        this.nextpieceboard = new Grid(3,3);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        currentPiece = spawnPiece();
        followingPiece = spawnPiece();
        updatepieceboard(currentPiece);
        nextPieceListener.followingpiece(followingPiece);
        starttimer();
        if (gameLoopListener != null) {
            gameLoopListener.timercreated(getTimerDelay());
        }
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        //Get the new value for this block
        int previousValue = grid.get(x,y);
        int newValue = previousValue + 1;
        if (newValue  > GamePiece.PIECES) {
            newValue = 0;
        }

        if(grid.canPlayPiece(currentPiece,x,y)){
            logger.info("piece true");
            grid.playPiece(currentPiece,x,y);
            multimedia.setaudioplayer(placesound);
            nextPiece();
            afterPiece();
            updatepieceboard(currentPiece);
            stoptimer();
            starttimer();
            gameLoopListener.timerstopped(getTimerDelay());
        }else{
            multimedia.setaudioplayer(failsound);
        }

    }

    public void blockchanged(GamePiece piece){

    }

    public void updatepieceboard(GamePiece gamePiece){
        int[][] pieceshape = gamePiece.getBlocks();
        for(int x=0;x<3;x++){
            for(int y=0;y<3;y++){
                if(!(pieceshape[x][y]==0)){
                    pieceboard.set(x, y, gamePiece.getValue());
                }
            }
        }

    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    public Grid getPieceboard(){return pieceboard;}

    public Grid getnextpieceboard(){return nextpieceboard;}


    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    public SimpleIntegerProperty getScore() {
        return score;
    }

    public SimpleIntegerProperty getLevel(){
        return level;
    }

    public SimpleIntegerProperty getLives() {
        return lives;
    }

    public SimpleIntegerProperty getMultiplier() {
        return multiplier;
    }

    public GamePiece spawnPiece(){
        pieceboard.emptygrid();
        Random randomnumber = new Random();
        int[][] blocks = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        GamePiece newpiece = null;
        return newpiece.createPiece(randomnumber.nextInt(15));
        //return newpiece.createPiece(0);
    }

    public void nextPiece(){
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        if (nextPieceListener != null) {
            nextPieceListener.nextpiece(currentPiece);
            nextPieceListener.followingpiece(followingPiece);
        }
    }


    public void afterPiece(){
        HashSet<Integer> rowstodelete = new HashSet<>();
        HashSet<Integer> columnstodelete = new HashSet<>();
        Set<Pair<Integer, Integer>> blocksdeleted = new HashSet<>();
        int beforesize = blocksdeleted.size();
        countrows(rowstodelete);
        countcolumns(columnstodelete);
        int aftersize = blocksdeleted.size();
        for (int row: rowstodelete){
            for (int column = 0; column < 5; column++) {
                grid.set(column,row,0);
                blocksdeleted.add(new Pair<>(column, row));
            }
            logger.info("row"+row+"deleted");
        }
        for (int column:columnstodelete){
            for (int row = 0; row < 5; row++) {
                grid.set(column,row,0);
                blocksdeleted.add(new Pair<>(column, row));
            }
            logger.info("column"+column+"deleted");
        }
        if(beforesize!=aftersize){
            multimedia.setaudioplayer(clearsound);
        }
        int prevscore = score.getValue();
        score(rowstodelete.size()+columnstodelete.size(), blocksdeleted.size());
        int newscore = score.getValue();
        if (prevscore!=newscore){
            multiplier.set(multiplier.getValue()+1);
            level.set((score.getValue()/1000));
        }else{
            multiplier.set(1);
        }
        checkempty();
        if(lineClearedListener!=null){
            lineClearedListener.onLineCleared(blocksdeleted);
        }


    }

    public void checkempty(){
    int count = 0;
    for (int x = 0; x < 5; x++) {
      for (int y = 0; y < 5; y++) {
        if ((grid.get(y, x) == 0)) {
          count++;
        }
      }
    }
    if (count == 25) {
      multimedia.setaudioplayer(suiiisound);
      logger.info("suiiii");
    }
  }

  public void countrows(HashSet<Integer> rowstodelete) {
        int count = 0;
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (!(grid.get(y, x) == 0)) {
                    count++;
                }
            }
            if(count==5){
                rowstodelete.add(x);
                logger.info("added"+x+"row to delete");
            }
            count=0;
        }
    }
    public void countcolumns(HashSet<Integer> columnstodelete){
        int count = 0;
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (!(grid.get(x, y) == 0)) {
                    count++;
                }
            }
            if (count==5){
                columnstodelete.add(x);
                logger.info("added"+x+"column to delete");
            }
            count=0;
        }
    }

    public void score(int nolines, int noblocks){
        score.set(score.getValue()+(nolines*noblocks*10*multiplier.getValue()));
    }


    public void swapcurrentpiece(){
        multimedia.setaudioplayer(rotatesound);
        pieceboard.emptygrid();
        nextpieceboard.emptygrid();
        logger.info("next piece grid clicked");
        GamePiece temp = currentPiece;
        currentPiece = followingPiece;
        followingPiece = temp;
        pieceboard.changedisplayedpiece(currentPiece);
        nextpieceboard.changedisplayedpiece(followingPiece);
    }
    public void rotatecurrentpiece(){
        multimedia.setaudioplayer(rotatesound);
        currentPiece.rotate();
        pieceboard.changedisplayedpiece(currentPiece);
    }

    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    public void setGameLoopListener(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    public int getTimerDelay(){
        int delay = 12000-(500*level.getValue());
        if (delay < 2500){
            delay = 2500;
        }
        return delay;
    }

    public void starttimer(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameLoop();
            }
        },getTimerDelay(),1);
    }

    public void stoptimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public void gameLoop(){
        if(lives.get()-1 < 0){
            stoptimer();
            Platform.runLater(() -> {
                gameLoopListener.gameended(this);
            });
        }else{
            lives.set(lives.get()-1);
            multiplier.set(1);
            nextPiece();
            if (gameLoopListener != null) {
                gameLoopListener.timerstopped(getTimerDelay());
            }
            stoptimer();
            starttimer();
        }
    }



}



