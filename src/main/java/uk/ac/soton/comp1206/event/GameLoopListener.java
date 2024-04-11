package uk.ac.soton.comp1206.event;

public interface GameLoopListener {

    void timerstarted(int delay);
    void timerstopped();
}
