package com.nixholas.materialtunes.Media;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.Song;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Created by nixho on 02-Nov-16.
 */

public class MediaManager extends Thread {
    public MediaPlayer mediaPlayer = new MediaPlayer();
    public volatile ArrayList<Song> songFiles = new ArrayList<>();
    public volatile ArrayList<Album> albumFiles = new ArrayList<>();

    public MediaManager() {
    }


    @Override
    public void run() {

    }

    }
