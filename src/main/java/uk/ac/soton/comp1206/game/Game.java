package uk.ac.soton.comp1206.game;

import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;

import java.util.HashSet;
import java.util.Random;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

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


    SimpleIntegerProperty score = new SimpleIntegerProperty(0);
    SimpleIntegerProperty level = new SimpleIntegerProperty(0);
    SimpleIntegerProperty lives = new SimpleIntegerProperty(3);
    SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    GamePiece currentPiece;

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
        updatepieceboard(currentPiece);
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
            nextPiece();
            afterPiece();
            updatepieceboard(currentPiece);
        }

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
        //return newpiece.createPiece(3);
    }

    public void nextPiece(){
        currentPiece = spawnPiece();
    }

    public void afterPiece(){
        HashSet<Integer> rowstodelete = new HashSet<>();
        HashSet<Integer> columnstodelete = new HashSet<>();
        HashSet<SimpleIntegerProperty[][]> blocksdeleted = new HashSet<>();
        countrows(rowstodelete);
        countcolumns(columnstodelete);
        for (int row: rowstodelete){
            for (int column = 0; column < 5; column++) {
                grid.set(column,row,0);
                blocksdeleted.add(new SimpleIntegerProperty[column][row]);
            }
            logger.info("row"+row+"deleted");
        }
        for (int column:columnstodelete){
            for (int row = 0; row < 5; row++) {
                grid.set(column,row,0);
                blocksdeleted.add(new SimpleIntegerProperty[column][row]);
            }
            logger.info("column"+column+"deleted");
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


    }

    public void countrows(HashSet<Integer> rowstodelete){
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


}
