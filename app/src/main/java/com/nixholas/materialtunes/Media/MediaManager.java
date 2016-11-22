package com.nixholas.materialtunes.Media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.nixholas.materialtunes.MainActivity.mediaControls_PlayPause;
import static com.nixholas.materialtunes.MainActivity.persistentNotif;
import static com.nixholas.materialtunes.MainActivity.slideAlbumArt;
import static com.nixholas.materialtunes.MainActivity.slideButton;
import static com.nixholas.materialtunes.MainActivity.slideSongArtist;
import static com.nixholas.materialtunes.MainActivity.slideSongTitle;
import static com.nixholas.materialtunes.MainActivity.slidedAlbumArt;
import static com.nixholas.materialtunes.MainActivity.slidingUpPanelLayout;

/**
 * Created by nixho on 02-Nov-16.
 * <p>
 * 20 November 2016
 * Found the API Level 21 and beyond method for media playback
 * https://docs.google.com/presentation/d/1jlI8ty6jq8NdstefSeZeGrBq8yE2avBOXTBo175Kgyg/edit?usp=sharing
 */

public class MediaManager extends Service {
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    public class ServiceBinder extends Binder {

        public MediaManager getService() {
            return MediaManager.this;
        }
    }

    private Binder mBinder = new MediaManager.ServiceBinder();

    public MediaSession.Token getMediaSessionToken() {
        return mMediaSession.getSessionToken();
    }

    public enum RepeatState{
        NOREPEAT,
        REPEATONE,
        REPEATALL
    }

    private MediaSession mMediaSession;
    public MediaPlayer mMediaPlayer = new MediaPlayer();
    public boolean mediaPlayerIsPaused;
    public RepeatState repeatState = RepeatState.NOREPEAT; // 0 for none, 1 for repeat one, 2 for repeat all
    private Random shufflerRandomizer;
    public boolean isMediaPlayerIsShuffling;
    private PlaybackState mPlaybackState;
    public int currentlyPlayingIndex;
    public volatile ArrayList<Song> songFiles = new ArrayList<>();
    public volatile ArrayList<Album> albumFiles = new ArrayList<>();

    public MediaManager(MainActivity mainActivity) {
        //Log.e("onCreate: MediaManager", "Working");
        mediaPlayerIsPaused = false;
        isMediaPlayerIsShuffling = false;
        shufflerRandomizer = new Random();
        mPlaybackState = new PlaybackState.Builder()
                .setState(PlaybackState.STATE_NONE, 0, 1.0f)
                .build();
        mMediaSession = new MediaSession(mainActivity, "mSession");
        mMediaSession.setCallback(mMediaSessionCallback);
        mMediaSession.setActive(true);
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setPlaybackState(mPlaybackState);

        /**
         * Some management methods for the MediaPlayer Object
         *
         * @param mediaPlayer http://stackoverflow.com/questions/10529226/notify-once-the-audio-is-finished-playing
         */
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // Check to make sure it's not hidden
                if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }

                //Log.e("OnPrepared", "Working");

                mMediaPlayer.start();
                mPlaybackState = new PlaybackState.Builder()
                        .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
                        .build();
                mMediaSession.setPlaybackState(mPlaybackState);
                persistentNotif.updateNotification(getCurrent());

            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //Log.e("Completed", "Yep");

                try {
                    if (repeatState == RepeatState.REPEATALL) {
                        /**
                         * Under the hood changes
                         */
                        Song nextSong = getNext(); // Call and get the next song with shuffling check
                        Uri sArtworkUri = Uri
                                .parse("content://media/external/audio/albumart");
                        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, nextSong.getAlbumId());

                        mMediaPlayer.reset(); // Reset the mediaPlayer first
                        mMediaPlayer.setDataSource("file://" + nextSong.getDataPath()); // Set the path via the next song
                        mMediaPlayer.prepareAsync(); // prepare and play

                        mPlaybackState = new PlaybackState.Builder()
                                .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
                                .build();
                        mMediaSession.setPlaybackState(mPlaybackState);

                        /**
                         * User Interface Changes
                         */
                        slideSongTitle.setText(nextSong.getTitle());
                        slideSongArtist.setText(nextSong.getArtistName());
                        Glide.with(getApplicationContext()).load(albumArtUri).placeholder(R.drawable.untitled_album).into(slideAlbumArt);
                        Glide.with(getApplicationContext()).load(albumArtUri).placeholder(R.drawable.untitled_album).into(slidedAlbumArt);
                    } else if (repeatState == RepeatState.NOREPEAT) {
                        /**
                         * Under The Hood changes
                         */
                        Song currentSong = songFiles.get(currentlyPlayingIndex); // Get the current song that just ended
                        mMediaPlayer.reset(); // Reset the player first
                        Uri audioUri = Uri.parse("file://" + currentSong.getDataPath()); // Get the path of the song
                        mMediaPlayer.setDataSource(getApplicationContext(), audioUri); // Set it again

                        mPlaybackState = new PlaybackState.Builder()
                                .setState(PlaybackState.STATE_NONE, 0, 1.0f)
                                .build();
                        mMediaSession.setPlaybackState(mPlaybackState);

                        // Update the UI
                        slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
                    } // No need to perform an else for REPEATONE

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private MediaSession.Callback mMediaSessionCallback = new MediaSession.Callback() {

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            //Uri uri = extras.getParcelable(PARAM_TRACK_URI);
            //onPlayFromUri(uri, null);
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {

            try {
                switch (mPlaybackState.getState()) {
                    case PlaybackState.STATE_PLAYING:
                    case PlaybackState.STATE_PAUSED:
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(MediaManager.this, uri);
                        mMediaPlayer.prepare();
                        mPlaybackState = new PlaybackState.Builder()
                                .setState(PlaybackState.STATE_CONNECTING, 0, 1.0f)
                                .build();
                        mMediaSession.setPlaybackState(mPlaybackState);
                        break;
                    case PlaybackState.STATE_NONE:
                        mMediaPlayer.setDataSource(MediaManager.this, uri);
                        mMediaPlayer.prepare();
                        mPlaybackState = new PlaybackState.Builder()
                                .setState(PlaybackState.STATE_CONNECTING, 0, 1.0f)
                                .build();
                        mMediaSession.setPlaybackState(mPlaybackState);
                        break;

                }
            } catch (IOException e) {

            }

        }

        @Override
        public void onPlay() {
            switch (mPlaybackState.getState()) {
                case PlaybackState.STATE_PAUSED:
                    mMediaPlayer.start();
                    mPlaybackState = new PlaybackState.Builder()
                            .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
                            .build();
                    mMediaSession.setPlaybackState(mPlaybackState);
                    persistentNotif.updateNotification(getCurrent());
                    break;

            }
        }

        @Override
        public void onPause() {
            switch (mPlaybackState.getState()) {
                case PlaybackState.STATE_PLAYING:
                    mMediaPlayer.pause();
                    mPlaybackState = new PlaybackState.Builder()
                            .setState(PlaybackState.STATE_PAUSED, 0, 1.0f)
                            .build();
                    mMediaSession.setPlaybackState(mPlaybackState);
                    persistentNotif.updateNotification(getCurrent());
                    break;

            }
        }

        @Override
        public void onRewind() {
            switch (mPlaybackState.getState()) {
                case PlaybackState.STATE_PLAYING:
                    mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - 10000);
                    break;

            }
        }

        @Override
        public void onFastForward() {
            switch (mPlaybackState.getState()) {
                case PlaybackState.STATE_PLAYING:
                    mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 10000);
                    break;

            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public PlaybackState getmPlaybackState() {
        return mPlaybackState;
    }

    public Song getCurrent() {
        return songFiles.get(currentlyPlayingIndex);
    }

    public Song getNext() {
        if (isMediaPlayerIsShuffling) {
            int newSong = currentlyPlayingIndex;
            while (newSong == currentlyPlayingIndex) {
                newSong = shufflerRandomizer.nextInt(songFiles.size());
            }
            currentlyPlayingIndex = newSong;
        } else {
            currentlyPlayingIndex++;
            if (currentlyPlayingIndex == songFiles.size()) {
                currentlyPlayingIndex = 0;
            }
        }

        return songFiles.get(currentlyPlayingIndex);
    }

    public Song getPrevious() {
        if (isMediaPlayerIsShuffling) {
            int newSong = currentlyPlayingIndex;
            while (newSong == currentlyPlayingIndex) {
                newSong = shufflerRandomizer.nextInt(songFiles.size());
            }
            currentlyPlayingIndex = newSong;
        } else {
            if (currentlyPlayingIndex == 0) {
                currentlyPlayingIndex = songFiles.size();
            } else {
                currentlyPlayingIndex--;
            }
        }

        return songFiles.get(currentlyPlayingIndex);
    }

    public RepeatState getRepeatState() {
        return repeatState;
    }

    public void setRepeatState(RepeatState repeatState) {
        this.repeatState = repeatState;
    }

}
