package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Next Piece Listener is called when pieces in the game are changed, whether by the player
 * placing a piece or the pieces being exchanged.
 */
public interface NextPieceListener {

  /** Handle when a piece has changed
   * @param piece piece to update
   */
  void nextpiece(GamePiece piece);

  /** Handle when a piece has changed
   * @param piece piece to update
   */
  void followingpiece(GamePiece piece);
}
