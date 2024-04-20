package uk.ac.soton.comp1206.event;

/**
 * The Right Clicked Listener is called when the pieceboard is right clicked, allowing for the
 * rotation/exchange of pieces.
 */
public interface RightClickedListener {

  /** Handles a right click */
  void onRightClicked();
}
