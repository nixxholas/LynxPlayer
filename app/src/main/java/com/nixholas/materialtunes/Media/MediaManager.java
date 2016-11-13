package com.nixholas.materialtunes.Media;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.Preferences;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by nixho on 02-Nov-16.
 */

public class MediaManager {
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    public enum MPPlayState { // MediaPlayer Play State
        NOREPEAT,
        REPEATALL,
        REPEATONE
    }

    public MediaPlayer mediaPlayer = new MediaPlayer();
    private Notification mNotification;
    public boolean mediaPlayerIsPaused;
    private Random shufflerRandomizer;
    public boolean isMediaPlayerIsShuffling;
    public MPPlayState PlayState;
    public int currentlyPlayingIndex;
    public volatile ArrayList<Song> songFiles = new ArrayList<>();
    public volatile ArrayList<Album> albumFiles = new ArrayList<>();

    public MediaManager() {
        mediaPlayerIsPaused = false;
        isMediaPlayerIsShuffling = false;
        shufflerRandomizer = new Random();
        PlayState = MPPlayState.NOREPEAT;
    }

    public Song getCurrent() {
        return songFiles.get(currentlyPlayingIndex);
    }

    public Song getNext() {
        if(isMediaPlayerIsShuffling){
            int newSong = currentlyPlayingIndex;
            while(newSong == currentlyPlayingIndex){
                newSong = shufflerRandomizer.nextInt(songFiles.size());
            }
            currentlyPlayingIndex = newSong;
        }
        else{
            currentlyPlayingIndex++;
            if(currentlyPlayingIndex == songFiles.size()) {
                currentlyPlayingIndex = 0;
            }
        }

        return songFiles.get(currentlyPlayingIndex);
    }

    public Song getPrevious() {
        if(isMediaPlayerIsShuffling){
            int newSong = currentlyPlayingIndex;
            while(newSong == currentlyPlayingIndex){
                newSong = shufflerRandomizer.nextInt(songFiles.size());
            }
            currentlyPlayingIndex = newSong;
        }
        else{
            if (currentlyPlayingIndex == 0) {
                currentlyPlayingIndex = songFiles.size();
            } else {
                currentlyPlayingIndex--;
            }
        }

        return songFiles.get(currentlyPlayingIndex);
    }

}
