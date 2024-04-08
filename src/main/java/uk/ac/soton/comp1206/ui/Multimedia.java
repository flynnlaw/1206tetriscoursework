package uk.ac.soton.comp1206.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class Multimedia {

    Media media;

    MediaPlayer audioplayer;
    MediaPlayer musicplayer;

    public void setaudioplayer(Media media){
        audioplayer = new MediaPlayer(media);
        audioplayer.setAutoPlay(true);

    }

    public void playinloop(Media pmedia) {
        media = pmedia;
        musicplayer = new MediaPlayer(media);
        musicplayer.setAutoPlay(true);
        musicplayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                musicplayer.seek(Duration.ZERO);
                musicplayer.play();
            }
        });
    }

    public MediaPlayer getmusicplayer(Media media){
    return new MediaPlayer(media);
    }



    public void playmenumusic(Media media){
        playinloop(media);
    }
    public void playgamemusic(Media mediaone, Media mediatwo){
        media = mediaone;
        musicplayer = getmusicplayer(mediaone);
        musicplayer.play();
        musicplayer.setOnEndOfMedia(() -> {
            musicplayer.stop(); // Stop the first media player
            media=mediatwo;
            playinloop(media); // Start playing the second media player
        });

    }

    public void stopmusicplayer(){
        musicplayer.stop();
    }
}
