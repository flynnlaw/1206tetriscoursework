package uk.ac.soton.comp1206.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

/** Multimedia is a class that allows the game to play sound effects/background music when called */
public class Multimedia {

  /** Media class configured for each file */
  Media media;

  /** Plays audio */
  MediaPlayer audioplayer;

  /** Plays music */
  MediaPlayer musicplayer;

  /**
   * Sets audio player to media and plays
   *
   * @param media media class
   */
  public void setaudioplayer(Media media) {
    audioplayer = new MediaPlayer(media);
    audioplayer.setAutoPlay(true);
  }

  /**
   * Plays the passed in media on loop
   *
   * @param pmedia media class
   */
  public void playinloop(Media pmedia) {
    media = pmedia;
    musicplayer = new MediaPlayer(media);
    musicplayer.setAutoPlay(true);
    musicplayer.setOnEndOfMedia(
        new Runnable() {
          @Override
          public void run() {
            musicplayer.seek(Duration.ZERO);
            musicplayer.play();
          }
        });
  }

  /**
   * Returns media player with media defined
   *
   * @param media media class
   * @return media player
   */
  public MediaPlayer getmusicplayer(Media media) {
    return new MediaPlayer(media);
  }

  /**
   * Plays music in loop
   *
   * @param media media class
   */
  public void playmenumusic(Media media) {
    playinloop(media);
  }

  /**
   * Given two media, play one until completion then play the other on loop
   *
   * @param mediaone media to play once
   * @param mediatwo media to play on loop
   */
  public void playgamemusic(Media mediaone, Media mediatwo) {
    media = mediaone;
    musicplayer = getmusicplayer(mediaone);
    musicplayer.play();
    musicplayer.setOnEndOfMedia(
        () -> {
          musicplayer.stop(); // Stop the first media player
          media = mediatwo;
          playinloop(media); // Start playing the second media player
        });
  }

  /** Stop player */
  public void stopmusicplayer() {
    musicplayer.stop();
  }
}
