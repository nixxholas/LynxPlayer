package com.nixholas.materialtunes.Media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;
import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.List;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.RemoteControlReceiver;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
import static com.nixholas.materialtunes.IntroActivity.preferenceHelper;
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
import static com.nixholas.materialtunes.UI.MediaControlUpdater.mediaControlsOnClickNext;

/**
 * Created by nixho on 02-Nov-16.
 * <p>
 * 20 November 2016
 * Found the API Level 21 and beyond method for media playback
 * https://docs.google.com/presentation/d/1jlI8ty6jq8NdstefSeZeGrBq8yE2avBOXTBo175Kgyg/edit?usp=sharing
 */

public class MediaManager extends Service {
    // Action Strings
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    // Main Objects for MediaManager
    public AudioManager audioManager;
    private MediaSessionCompat mMediaSession;
    public MediaPlayer mMediaPlayer = new MediaPlayer();
    public RemoteControlReceiver remoteControlReceiver;

    // MediaManager Sub Objects
    public boolean mediaPlayerIsPaused;
    public RepeatState repeatState = RepeatState.NOREPEAT; // 0 for none, 1 for repeat one, 2 for repeat all
    private Random shufflerRandomizer;
    private PlaybackStateCompat mPlaybackState;
    public int currentlyPlayingIndex;

    // MediaManager Resources
    public volatile ArrayList<Song> songFiles = new ArrayList<>();
    public volatile ArrayList<Album> albumFiles = new ArrayList<>();
    public volatile ArrayList<List> playLists = new ArrayList<>();

    public class ServiceBinder extends Binder {
        public MediaManager getService() {
            return MediaManager.this;
        }
    }

    private Binder mBinder = new MediaManager.ServiceBinder();

    public MediaSessionCompat.Token getMediaSessionToken() {
        return mMediaSession.getSessionToken();
    }

    public enum RepeatState {
        NOREPEAT,
        REPEATONE,
        REPEATALL
    }

    public MediaManager(final MainActivity mainActivity) {
        //Log.e("onCreate: MediaManager", "Working");
        audioManager = (AudioManager) mainActivity.getSystemService(Context.AUDIO_SERVICE);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    mMediaPlayer.pause();
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    mMediaPlayer.start();
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        mediaPlayerIsPaused = false;
        shufflerRandomizer = new Random();
        mPlaybackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                .build();
        mMediaSession = new MediaSessionCompat(mainActivity, "mSession");
        MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

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
                            mPlaybackState = new PlaybackStateCompat.Builder()
                                    .setState(PlaybackStateCompat.STATE_CONNECTING, 0, 1.0f)
                                    .build();
                            mMediaSession.setPlaybackState(mPlaybackState);
                            break;
                        case PlaybackState.STATE_NONE:
                            mMediaPlayer.setDataSource(MediaManager.this, uri);
                            mMediaPlayer.prepare();
                            mPlaybackState = new PlaybackStateCompat.Builder()
                                    .setState(PlaybackStateCompat.STATE_CONNECTING, 0, 1.0f)
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
                        mPlaybackState = new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
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
                        mPlaybackState = new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
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
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
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
                    mPlaybackState = new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                            .build();
                    mMediaSession.setPlaybackState(mPlaybackState);

                    // Get a handler that can be used to post to the main thread
                    // http://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread
                    final Handler mainHandler = new Handler(getInstance().getMainLooper());

                    //Log.e("OnPrepared", "Working");
                    long songDuration = getCurrent().getDuration();

                    slided_SongTitle.setText(getCurrent().getTitle());
                    slided_SongArtist.setText(getCurrent().getArtistName());

                    // http://stackoverflow.com/questions/17168215/seekbar-and-media-player-in-android
                    //Log.e("MaxDuration", getCurrent().getDuration() + "");
                    slidingSeekBar.setMax((int) songDuration); // Set the max duration
                    slidedSeekBar.setMax((int) songDuration);

                    // Retrieve the length of the song and set it into the Maximum Text View
                    //mediaSeekText_Maximum.setText(getCurrent().getDuration() + "");
                    // http://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java
                    mediaSeekText_Maximum.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(songDuration),
                            TimeUnit.MILLISECONDS.toSeconds(songDuration) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration))
                    ));

                    // What if the user wants to scrub the time
                    // http://stackoverflow.com/questions/35407578/how-to-control-the-audio-using-android-media-player-seek-bar
                    slidedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser)
                                mMediaPlayer.seekTo(progress);
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

                                    long currentPosition = mMediaPlayer.getCurrentPosition();
                                    // Set current time
                                    //mediaSeekText_Progress.setText(mMediaPlayer.getCurrentPosition() + "");
                                    //mediaSeekText_Progress.setText(msecondsToString(mMediaPlayer.getCurrentPosition()));
                                    mediaSeekText_Progress.setText(String.format("%02d:%02d",
                                            TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                                            TimeUnit.MILLISECONDS.toSeconds(currentPosition) -
                                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition))
                                    ));

                                    //repeat yourself that again in 100 miliseconds
                                    mainHandler.postDelayed(this, 100);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mainHandler.post(progressRunnable);

                persistentNotif.updateNotification();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //Log.e("Completed", "Yep");

                try {
                    if (repeatState == RepeatState.REPEATALL) {
                        // Since it's repeat all, naturally it mimics an onClick Next..
                        mediaControlsOnClickNext(MainActivity.getInstance().getCurrentFocus());
                    } else if (repeatState == RepeatState.NOREPEAT) {
                        /**
                         * Under The Hood changes
                         */
                        Song currentSong = songFiles.get(currentlyPlayingIndex); // Get the current song that just ended
                        mMediaPlayer.reset(); // Reset the player first
                        Uri audioUri = Uri.parse("file://" + currentSong.getDataPath()); // Get the path of the song
                        mMediaPlayer.setDataSource(MainActivity.getInstance().getApplicationContext(), audioUri); // Set it again

                        mPlaybackState = new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
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

    /**
     * http://codereview.stackexchange.com/questions/59784/integer-seconds-to-formated-string-mmss
     * @param pTime
     * @return
     */
    private String msecondsToString(long pTime) {
        final long min = TimeUnit.MILLISECONDS.toMinutes(pTime);
        final long sec = TimeUnit.MILLISECONDS.toSeconds(pTime - (min * 60));

        final String strMin = placeZeroIfNeeded(min);
        final String strSec = placeZeroIfNeeded(sec);
        return String.format("%s:%s",strMin,strSec);
    }

    private String placeZeroIfNeeded(long number) {
        return (number >=10)? Long.toString(number):String.format("0%s",Long.toString(number));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public PlaybackStateCompat getmPlaybackState() {
        return mPlaybackState;
    }

    public Song getCurrent() {
        return songFiles.get(currentlyPlayingIndex);
    }

    public Song getNext() {
        Log.d("getNext()", "Running getNext()");

        if (preferenceHelper.getShuffle()) {
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
        if (preferenceHelper.getShuffle()) {
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
