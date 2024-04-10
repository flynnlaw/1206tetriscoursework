package uk.ac.soton.comp1206.event;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;

import java.util.HashSet;
import java.util.Set;


public interface LineClearedListener {
    void onLineCleared(Set<Pair<Integer, Integer>> blockstodelete);
}
