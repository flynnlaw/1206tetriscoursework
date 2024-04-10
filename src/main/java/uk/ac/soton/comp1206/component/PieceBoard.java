package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

import java.util.Random;

public class PieceBoard extends GameBoard{

    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    public PieceBoard(Grid grid, double width, double height, int value){
        super(grid, width, height);
            GamePiece piece = spawnPiece(value);
            int[][] gridneeded = piece.getBlocks();
            for(int j=0;j<3;j++){
                for(int k=0;k<3;k++){
                    if(!(gridneeded[j][k]==0)){
                        grid.set(j,k,value+1);
                    }
                }
            }
    }

    public GamePiece spawnPiece(int value){
        Random randomnumber = new Random();
        int[][] blocks = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        GamePiece newpiece = null;
        return newpiece.createPiece(value);
    }

    public void displaypiece(GamePiece piece){
        int[][] gridneeded = piece.getBlocks();
        int value = piece.getValue();
        for(int j=0;j<3;j++){
            for(int k=0;k<3;k++){
                if(!(gridneeded[j][k]==0)){
                    grid.set(j,k,value);
                }
            }
        }
        GameBlock middle = getBlock(1,1);


    }

    public void emptygrid(){
        for (int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                grid.set(i,j,0);
            }
        }
    }







}






