package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

/** The Game Loop Listener is used when the timer in the game has run out */
public interface GameLoopListener {

  /** Sets the dimensions of the timer rectangle, and adds it to the Challenge Scene
   * @param delay delay of the timer
   */
  void timerstarted(int delay);

  /** Resets the timer by stopping the animations and restarting them
   *  @param delay delay of the timer
   */
  void timerstopped(int delay);

  /**
   * Defines the timer rectangle and the animation attached to it. Called at game initialisation.
   * @param delay delay of the timer
   */
  void timercreated(int delay);

  /** Stops the music, timer animation and launches the Scores Scene when the game ends.
   * @param game game instance
   */
  void gameended(Game game);
}
