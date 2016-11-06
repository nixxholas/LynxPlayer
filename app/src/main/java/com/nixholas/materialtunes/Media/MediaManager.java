package com.nixholas.materialtunes.Media;

import android.media.MediaPlayer;

import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.Song;

import java.util.ArrayList;

/**
 * Created by nixho on 02-Nov-16.
 */

public class MediaManager extends Thread {
    public enum MPPlayState { // MediaPlayer Play State
        NOREPEAT,
        REPEATALL,
        REPEATONE
    }

    public MediaPlayer mediaPlayer = new MediaPlayer();
    public boolean mediaPlayerIsPaused;
    public boolean isMediaPlayerIsShuffling;
    public MPPlayState PlayState;
    public int currentlyPlayingIndex;
    public volatile ArrayList<Song> songFiles = new ArrayList<>();
    public volatile ArrayList<Album> albumFiles = new ArrayList<>();

    public MediaManager() {
        mediaPlayerIsPaused = false;
        isMediaPlayerIsShuffling = false;
        PlayState = MPPlayState.NOREPEAT;
    }

    @Override
    public void run() {

    }

    }
