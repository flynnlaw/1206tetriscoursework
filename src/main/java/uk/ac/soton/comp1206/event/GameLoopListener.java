package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

public interface GameLoopListener {

    void timerstarted(int delay);
    void timerstopped(int delay);

    void timercreated(int delay);

    void gameended(Game game);
}
