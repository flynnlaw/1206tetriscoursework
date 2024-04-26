package uk.ac.soton.comp1206.event;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * The Line Cleared Listener is called when a entire row/column of blocks is made in the game and is
 * therefore has to be removed.
 */
public interface LineClearedListener {

  /** Called when a line is cleared in the game
   * @param blockstodelete set of indivdual blocks to fade out
   */
  void onLineCleared(Set<Pair<Integer, Integer>> blockstodelete);
}
