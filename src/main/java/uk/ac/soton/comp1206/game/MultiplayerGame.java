package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class MultiplayerGame extends Game{

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    Communicator communicator;

    Queue<GamePiece> piecequeue;
    Boolean started = false;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        this.communicator = communicator;
        communicator.addListener((message) -> Platform.runLater(() -> this.receiveMessage(message)));
        this.piecequeue = new LinkedList<>();

    }

    @Override
    public void initialiseGame() {
        logger.info("Initialising game");
        generatestartingpieces();
        }

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
            generatenextpiece();
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

    public GamePiece spawnPiece(int value){
        int[][] blocks = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        GamePiece newpiece = null;
        return newpiece.createPiece(value);
    }

    @Override
    public void nextPiece(){
        currentPiece = followingPiece;
        followingPiece = piecequeue.poll();
        if (nextPieceListener != null) {
            nextPieceListener.nextpiece(currentPiece);
            nextPieceListener.followingpiece(followingPiece);
        }
    }

    @Override
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
        communicator.send("SCORE "+newscore);
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

    public void generatestartingpieces(){
        communicator.send("PIECE");
        communicator.send("PIECE");
        communicator.send("PIECE");
        communicator.send("PIECE");
        communicator.send("PIECE");
    }

    public void generatenextpiece(){communicator.send("PIECE");}


    public void recievestartingpieces(int value){
        piecequeue.offer(spawnPiece(value));
        if(!(started)&&piecequeue.size()>4){
            currentPiece = piecequeue.poll();
            followingPiece = piecequeue.poll();
            updatepieceboard(currentPiece);
            nextPieceListener.followingpiece(followingPiece);
            starttimer();
            if (gameLoopListener != null) {
                gameLoopListener.timercreated(getTimerDelay());
            }
            started=true;
        }


    }

    public void receiveMessage(String message) {

        String[] commands = message.split(" ");
        String command = commands[0];

        switch(command){

            case "PIECE" ->{
                recievestartingpieces(Integer.parseInt(commands[1]));
//                piecequeue.offer(spawnPiece(Integer.parseInt(commands[1])));
            }

            case "SCORE" ->{

            }


        }


    }


    public void gameLoop(){
        generatenextpiece();
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
