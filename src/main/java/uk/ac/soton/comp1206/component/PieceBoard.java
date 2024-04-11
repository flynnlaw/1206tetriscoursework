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

    private static final Logger logger = LogManager.getLogger(PieceBoard.class);


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

    protected GameBlock createBlock(int x, int y) {
        var blockWidth = getgameboardwidth() / getCols();
        var blockHeight = getgameboardheight() / getRows();

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));

        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));

        return block;
    }








}






