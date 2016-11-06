package com.nixholas.materialtunes.Media;

import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.net.Uri;
import android.util.Log;

import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.Song;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by nixho on 02-Nov-16.
 */

public class MediaManager {
    public enum MPPlayState { // MediaPlayer Play State
        NOREPEAT,
        REPEATALL,
        REPEATONE
    }

    public MediaPlayer mediaPlayer = new MediaPlayer();
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
