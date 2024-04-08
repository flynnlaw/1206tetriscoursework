package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;

public interface NextPieceListener {

    void nextpiece(GamePiece piece);

    void followingpiece(GamePiece piece);

}
