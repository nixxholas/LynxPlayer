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
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;
import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.nixholas.materialtunes.MainActivity.getInstance;
import static com.nixholas.materialtunes.MainActivity.mediaControls_PlayPause;
import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.MainActivity.mediaSeekText_Maximum;
import static com.nixholas.materialtunes.MainActivity.mediaSeekText_Progress;
import static com.nixholas.materialtunes.MainActivity.persistentNotif;
import static com.nixholas.materialtunes.MainActivity.slideAlbumArt;
import static com.nixholas.materialtunes.MainActivity.slideButton;
import static com.nixholas.materialtunes.MainActivity.slideSongArtist;
import static com.nixholas.materialtunes.MainActivity.slideSongTitle;
import static com.nixholas.materialtunes.MainActivity.slidedAlbumArt;
import static com.nixholas.materialtunes.MainActivity.slidedSeekBar;
import static com.nixholas.materialtunes.MainActivity.slided_SongArtist;
import static com.nixholas.materialtunes.MainActivity.slided_SongTitle;
import static com.nixholas.materialtunes.MainActivity.slidingSeekBar;
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

    public MediaManager(final MainActivity mainActivity) {
        //Log.e("onCreate: MediaManager", "Working");
        mediaPlayerIsPaused = false;
        isMediaPlayerIsShuffling = false;
        shufflerRandomizer = new Random();
        mPlaybackState = new PlaybackState.Builder()
                .setState(PlaybackState.STATE_NONE, 0, 1.0f)
                .build();
        mMediaSession = new MediaSession(mainActivity, "mSession");
        MediaSession.Callback mMediaSessionCallback = new MediaSession.Callback() {

            @Override
            public void onPlayFromSearch(String query, Bundle extras) {
                Log.e("onPlayFromSearch", "Running");
                //Uri uri = extras.getParcelable(PARAM_TRACK_URI);
                //onPlayFromUri(uri, null);
            }

            @Override
            public void onPlayFromUri(Uri uri, Bundle extras) {
                try {
                    Log.e("onPlayFromUri", "Running");

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
                Log.e("onPlay", "Running");
                switch (mPlaybackState.getState()) {
                    case PlaybackState.STATE_PAUSED:
                        mMediaPlayer.start();
                        mPlaybackState = new PlaybackState.Builder()
                                .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
                                .build();
                        mMediaSession.setPlaybackState(mPlaybackState);
                        persistentNotif.updateNotification();
                        break;

                }
            }

            @Override
            public void onPause() {
                Log.e("onPause", "Running");
                switch (mPlaybackState.getState()) {
                    case PlaybackState.STATE_PLAYING:
                        mMediaPlayer.pause();
                        mPlaybackState = new PlaybackState.Builder()
                                .setState(PlaybackState.STATE_PAUSED, 0, 1.0f)
                                .build();
                        mMediaSession.setPlaybackState(mPlaybackState);
                        persistentNotif.updateNotification();
                        break;

                }
            }

            @Override
            public void onRewind() {
                Log.e("onRewind", "Running");
                switch (mPlaybackState.getState()) {
                    case PlaybackState.STATE_PLAYING:
                        mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - 10000);
                        break;

                }
            }

            @Override
            public void onFastForward() {
                Log.e("onFastForward", "Running");
                switch (mPlaybackState.getState()) {
                    case PlaybackState.STATE_PLAYING:
                        mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 10000);
                        break;
                }
            }
        };
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

                    mediaPlayer.start();
                    mPlaybackState = new PlaybackState.Builder()
                            .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
                            .build();
                    mMediaSession.setPlaybackState(mPlaybackState);
                    persistentNotif.updateNotification();

                    // Get a handler that can be used to post to the main thread
                    // http://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread
                    final Handler mainHandler = new Handler(getInstance().getMainLooper());

                    //Log.e("OnPrepared", "Working");

                    slided_SongTitle.setText(getCurrent().getTitle());
                    slided_SongArtist.setText(getCurrent().getArtistName());

                    // http://stackoverflow.com/questions/17168215/seekbar-and-media-player-in-android
                    //Log.e("MaxDuration", getCurrent().getDuration() + "");
                    slidingSeekBar.setMax(getCurrent().getDuration()); // Set the max duration
                    slidedSeekBar.setMax(getCurrent().getDuration());
                    mediaSeekText_Maximum.setText(getCurrent().getDuration() + "");

                    // What if the user wants to scrub the time
                    // http://stackoverflow.com/questions/35407578/how-to-control-the-audio-using-android-media-player-seek-bar
                    slidedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser)
                                mMediaPlayer.seekTo(progress);
                            //seekBar.setProgress(progress);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    Runnable progressRunnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                            /*if (mediaManager.mMediaPlayer != null) {
                                //Log.d("ProgRunnable", "Running"); // Debugging Purposes only
                                int mCurrentPosition = mediaManager.mMediaPlayer.getCurrentPosition() / 1000;
                                slidingSeekBar.setProgress(mCurrentPosition);
                                slidedSeekBar.setProgress(mCurrentPosition);
                            }
                            mainHandler.postDelayed(this, 1000);*/

                                if (mediaManager.mMediaPlayer != null) {
                                    // http://stackoverflow.com/questions/35027321/seek-bar-and-media-player-and-a-time-of-a-track
                                    //set seekbar progress
                                    slidingSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                                    slidedSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                                    // Set current time
                                    mediaSeekText_Progress.setText(mMediaPlayer.getCurrentPosition() + "");

                                    //mediaSeekText_Progress.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

                                    //repeat yourself that again in 100 miliseconds
                                    mainHandler.postDelayed(this, 100);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mainHandler.post(progressRunnable);
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
                        mMediaPlayer.setDataSource(MainActivity.getInstance().getApplicationContext(), audioUri); // Set it again

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
