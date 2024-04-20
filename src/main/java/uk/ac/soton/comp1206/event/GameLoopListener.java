package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

/** The Game Loop Listener is used when the timer in the game has run out */
public interface GameLoopListener {

  /** Sets the dimensions of the timer rectangle, and adds it to the Challenge Scene */
  void timerstarted(int delay);

  /** Resets the timer by stopping the animations and restarting them */
  void timerstopped(int delay);

  /**
   * Defines the timer rectangle and the animation attached to it. Called at game initialisation.
   */
  void timercreated(int delay);

  /** Stops the music, timer animation and launches the Scores Scene when the game ends. */
  void gameended(Game game);
}
