package com.nixholas.materialtunes.Media;

import android.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nixho on 02-Nov-16.
 */

public class MediaManager extends Thread {
    private static final String SD_PATH = new String("/sdcard/");
    private volatile ArrayList<File> mediaFiles = new ArrayList<>();
    private MediaPlayer mediaPlayer = new MediaPlayer();

    public MediaManager() {}

    @Override
    public void run() {

    }
}
