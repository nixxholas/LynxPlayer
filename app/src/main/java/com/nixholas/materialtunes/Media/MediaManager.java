package com.nixholas.materialtunes.Media;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.SeekBar;

import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.Playlist;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.Media.Entities.Utils.PlaylistUtil;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.RemoteControlReceiver;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.nixholas.materialtunes.IntroActivity.preferenceHelper;
import static com.nixholas.materialtunes.MainActivity.getInstance;
import static com.nixholas.materialtunes.MainActivity.mediaControls_PlayPause;
import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.MainActivity.mediaSeekText_Maximum;
import static com.nixholas.materialtunes.MainActivity.mediaSeekText_Progress;
import static com.nixholas.materialtunes.MainActivity.persistentNotif;
import static com.nixholas.materialtunes.MainActivity.slideButton;
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
    // MediaSession allows interaction with media controllers, volume keys, media buttons, and transport controls
    //public MediaSession mMediaSession;
    public MediaPlayer mMediaPlayer = new MediaPlayer();
    public RemoteControlReceiver remoteControlReceiver;
    private MediaDB mediaDB;

    // MediaManager Sub Objects
    public boolean mediaPlayerIsPaused;
    public RepeatState repeatState = RepeatState.NOREPEAT; // 0 for none, 1 for repeat one, 2 for repeat all
    private Random shufflerRandomizer;
    private PlaybackState mPlaybackState;
    public int currentlyPlayingIndex;

    // MediaManager Resources
    /**
     * Main Queue of the whole program
     *
     * R3 == Reasons why LinkedList was used
     *
     * ArrayDeque is unable to have native support for shuffling.
     * Shuffling is key for any music player, because the user wants a
     * randomizer for his queue.
     *
     * Further support from: http://stackoverflow.com/questions/19258696/how-do-i-shuffle-a-deque
     *
     * R2 == Reasons why ArrayDeque was used
     *
     * 3x Faster than LinkedList
     * http://microbenchmarks.appspot.com/run/limpbizkit@gmail.com/com.publicobject.blog.TreeListBenchmark
     * http://stackoverflow.com/questions/6129805/what-is-the-fastest-java-collection-with-the-basic-functionality-of-a-queue
     *
     * R1 == Switched to ConcurrentLinkedQueue
     *
     * http://stackoverflow.com/questions/616484/how-to-use-concurrentlinkedqueue
     */
    public volatile LinkedList<Song> managerQueue = new LinkedList<>(); // We need a queue for the mediaplayer
    public ArrayList<Song> songFiles = new ArrayList<>();
    public ArrayList<Album> albumFiles = new ArrayList<>();
    public ArrayList<Playlist> playLists = new ArrayList<>();
    public ArrayList<Song> topPlayed = new ArrayList<>();

    // Playlist Helper
    public PlaylistUtil playlistUtil = new PlaylistUtil();

    public class ServiceBinder extends Binder {
        public MediaManager getService() {
            return MediaManager.this;
        }
    }

    private Binder mBinder = new MediaManager.ServiceBinder();

//    public MediaSession.Token getMediaSessionToken() {
//        return mMediaSession.getSessionToken();
//    }

    public enum RepeatState {
        NOREPEAT,
        REPEATONE,
        REPEATALL
    }

    public void initializeMediaDB(Context context) {
        mediaDB = new MediaDB(context);
    }

    public MediaManager(final MainActivity mainActivity) {
        //Log.e("onCreate: MediaManager", "Working");
        audioManager = (AudioManager) mainActivity.getSystemService(Context.AUDIO_SERVICE);

        //mediaDB = new MediaDB(mainActivity.getApplicationContext()); // Instantiate the SQLite Object

        /**
         * Temporary fix for AOBException for getCurrent
         */
        //this.managerQueue.addAll(songFiles);

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
                    // Phone is on idle, so we shall start playing.
                    mMediaPlayer.start();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager mgr = (TelephonyManager) mainActivity.getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        mediaPlayerIsPaused = false;
        shufflerRandomizer = new Random();
        mPlaybackState = new PlaybackState.Builder()
                .setState(PlaybackState.STATE_NONE, 0, 1.0f)
                .build();
//        mMediaSession = new MediaSession(mainActivity, "mSession");
//
//        mMediaSession.setCallback(new MediaSession.Callback() {
//            @Override
//            public void onCommand(String command, Bundle args, ResultReceiver cb) {
//                super.onCommand(command, args, cb);
//            }
//
//            @Override
//            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
//                return super.onMediaButtonEvent(mediaButtonIntent);
//            }
//
//            @Override
//            public void onPrepare() {
//                super.onPrepare();
//            }
//
//            @Override
//            public void onPrepareFromMediaId(String mediaId, Bundle extras) {
//                super.onPrepareFromMediaId(mediaId, extras);
//            }
//
//            @Override
//            public void onPrepareFromSearch(String query, Bundle extras) {
//                Log.e("onPlayFromSearch", "Running");
//                Uri uri = extras.getParcelable("PARAM_TRACK_URI");
//                onPlayFromUri(uri, null);
//            }
//
//            @Override
//            public void onPrepareFromUri(Uri uri, Bundle extras) {
//                super.onPrepareFromUri(uri, extras);
//            }
//
//            @Override
//            public void onPlay() {
//                Log.e("onPlay", "Running");
//                switch (mPlaybackState.getState()) {
//                    case PlaybackState.STATE_PAUSED:
//                        mMediaPlayer.start();
//                        mPlaybackState = new PlaybackState.Builder()
//                                .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
//                                .build();
//                        mMediaSession.setPlaybackState(mPlaybackState);
//                        persistentNotif.updateNotification();
//                        break;
//
//                }
//            }
//
//            @Override
//            public void onPlayFromSearch(String query, Bundle extras) {
//                super.onPlayFromSearch(query, extras);
//            }
//
//            @Override
//            public void onPlayFromMediaId(String mediaId, Bundle extras) {
//                super.onPlayFromMediaId(mediaId, extras);
//            }
//
//            @Override
//            public void onPlayFromUri(Uri uri, Bundle extras) {
//                try {
//                    Log.e("onPlayFromUri", "Running");
//
//                    switch (mPlaybackState.getState()) {
//                        case PlaybackState.STATE_PLAYING:
//                        case PlaybackState.STATE_PAUSED:
//                            mMediaPlayer.reset();
//                            mMediaPlayer.setDataSource(MediaManager.this, uri);
//                            mMediaPlayer.prepare();
//                            mPlaybackState = new PlaybackState.Builder()
//                                    .setState(PlaybackState.STATE_CONNECTING, 0, 1.0f)
//                                    .build();
//                            mMediaSession.setPlaybackState(mPlaybackState);
//                            break;
//                        case PlaybackState.STATE_NONE:
//                            mMediaPlayer.setDataSource(MediaManager.this, uri);
//                            mMediaPlayer.prepare();
//                            mPlaybackState = new PlaybackState.Builder()
//                                    .setState(PlaybackState.STATE_CONNECTING, 0, 1.0f)
//                                    .build();
//                            mMediaSession.setPlaybackState(mPlaybackState);
//                            break;
//
//                    }
//                } catch (IOException e) {
//
//                }
//            }
//
//            @Override
//            public void onSkipToQueueItem(long id) {
//                super.onSkipToQueueItem(id);
//            }
//
//            @Override
//            public void onPause() {
//                Log.e("onPause", "Running");
//                switch (mPlaybackState.getState()) {
//                    case PlaybackState.STATE_PLAYING:
//                        mMediaPlayer.pause();
//                        mPlaybackState = new PlaybackState.Builder()
//                                .setState(PlaybackState.STATE_PAUSED, 0, 1.0f)
//                                .build();
//                        mMediaSession.setPlaybackState(mPlaybackState);
//                        persistentNotif.updateNotification();
//                        break;
//
//                }
//            }
//
//            @Override
//            public void onSkipToNext() {
//                super.onSkipToNext();
//            }
//
//            @Override
//            public void onSkipToPrevious() {
//                super.onSkipToPrevious();
//            }
//
//            @Override
//            public void onFastForward() {
//                Log.e("onFastForward", "Running");
//                switch (mPlaybackState.getState()) {
//                    case PlaybackState.STATE_PLAYING:
//                        mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 10000);
//                        break;
//                }
//            }
//
//            @Override
//            public void onRewind() {
//                Log.e("onRewind", "Running");
//                switch (mPlaybackState.getState()) {
//                    case PlaybackState.STATE_PLAYING:
//                        mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - 10000);
//                        break;
//
//                }
//            }
//
//            @Override
//            public void onStop() {
//                super.onStop();
//            }
//
//            @Override
//            public void onSeekTo(long pos) {
//                super.onSeekTo(pos);
//            }
//
//            @Override
//            public void onSetRating(Rating rating) {
//                super.onSetRating(rating);
//            }
//
//            @Override
//            public void onCustomAction(String action, Bundle extras) {
//                super.onCustomAction(action, extras);
//            }
//        });
//
//        mMediaSession.setActive(true);
//        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
//                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
//        mMediaSession.setPlaybackState(mPlaybackState);

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
                    //mMediaSession.setPlaybackState(mPlaybackState);

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
                    mediaSeekText_Maximum.setText(String.format(Locale.ENGLISH, "%02d:%02d",
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
                                    mediaSeekText_Progress.setText(String.format(Locale.ENGLISH, "%02d:%02d",
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

                // Finally, bump the counter in the SQLite table

                // Debugging Purposes Only
                // Log.d("checkWithDB on " + getCurrent().getTitle(),
                // mediaDB.checkMediaCountIfExists(getCurrent().getId(), getCurrent().getTitle()) + "");

                if (!mediaDB.checkMediaCountIfExists(getCurrent().getId(), getCurrent().getTitle())) {
                    Log.d("mediaDBCheck", "This song does not exist in the DB");
                    mediaDB.addSongToMediaCount(getCurrent());
                } else {
                    // Since it exists, give it's row an increment in the playcount column
                    Log.d("mediaDBCheck", "This song exists in the DB");
                    mediaDB.incrementMediaCount(getCurrent());
                }

                updateTopPlayed(); // finally, update the top played list
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
                        Song currentSong = managerQueue.get(currentlyPlayingIndex); // Get the current song that just ended
                        mMediaPlayer.reset(); // Reset the player first
                        Uri audioUri = Uri.parse("file://" + currentSong.getDataPath()); // Get the path of the song
                        mMediaPlayer.setDataSource(MainActivity.getInstance().getApplicationContext(), audioUri); // Set it again

                        mPlaybackState = new PlaybackState.Builder()
                                .setState(PlaybackState.STATE_NONE, 0, 1.0f)
                                .build();
                        //mMediaSession.setPlaybackState(mPlaybackState);

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

    public PlaybackState getmPlaybackState() {
        return mPlaybackState;
    }

    //    public Song getCurrent() {
    //        return songFiles.get(currentlyPlayingIndex);
    //    }

    public Song getCurrent() {
        // If the MediaManager has already been playing
        if (!managerQueue.isEmpty()) {
            return managerQueue.get(currentlyPlayingIndex);
        } else {
            return songFiles.get(currentlyPlayingIndex);
        }
    }

    public Song getNext() {
        Log.d("getNext()", "Running getNext()");

        if (preferenceHelper.getShuffle()) {
            int newSong = currentlyPlayingIndex;
            if (managerQueue.size() > 1) { // Don't let a Deadlock happen
                while (newSong == currentlyPlayingIndex) {
                    newSong = shufflerRandomizer.nextInt(managerQueue.size());
                }
            }
            currentlyPlayingIndex = newSong;
        } else {
            currentlyPlayingIndex++;
            if (currentlyPlayingIndex == managerQueue.size()) {
                currentlyPlayingIndex = 0;
            }
        }

        return managerQueue.get(currentlyPlayingIndex);
    }

    public Song getPrevious() {
        if (preferenceHelper.getShuffle()) {
            int newSong = currentlyPlayingIndex;
            if (managerQueue.size() > 1) { // Don't let a Deadlock happen
                while (newSong == currentlyPlayingIndex) {
                    newSong = shufflerRandomizer.nextInt(managerQueue.size());
                }
            }
            currentlyPlayingIndex = newSong;
        } else {
            if (currentlyPlayingIndex == 0) {
                currentlyPlayingIndex = managerQueue.size();
            } else {
                currentlyPlayingIndex--;
            }
        }

        return managerQueue.get(currentlyPlayingIndex);
    }

    /**
     * This method is made such that all the songs are queued since the user is playing a song
     * directly from the songs tab
     * @param currentSong
     */
    public void putAllOnQueue(Song currentSong) {
        // Let's first put the current song into the queue
        managerQueue.add(currentSong);

        for (Song s : songFiles) {
            if (s.getId() == currentSong.getId()) {
                // Do nothing
            } else {
                // Add the song to the queue
                managerQueue.add(s);
            }
        }
    }

    public RepeatState getRepeatState() {
        return repeatState;
    }

    public void setRepeatState(RepeatState repeatState) {
        this.repeatState = repeatState;
    }

    public void purgeSong(long songId) {
        for (int i = 0; i < managerQueue.size(); i++) {
            if (managerQueue.get(i).getId() == songId) {
                managerQueue.remove(i);
                break;
            }
        }
    }

    public void purgeList(long playlistId) {
        for (int i = 0; i < playLists.size(); i++) {
            if (playLists.get(i).getPlaylistId() == playlistId) {
                playLists.remove(i);
                break;
            }
        }
    }

    public ArrayList<Song> getAlbumSongs(long albumId) {
        ArrayList<Song> result = new ArrayList<>();

        for (Song s : songFiles) {
            if (s.getAlbumId() == albumId) {
                result.add(s);
            }
        }

        return result;
    }

    public boolean findDuplicateAlbum(Album album) {
        for (Album a : albumFiles) {
            if (a.getArtistName().equals(album.getArtistName()) &&
                    a.getTitle().equals(album.getTitle())) { // If we really find a dupe
                for (Song s : songFiles) { // Set all the existing songs
                    if (s.getAlbumId() == album.getId()) { // To the existing album
                        s.setAlbumId(a.getId());
                        s.setAlbumName(a.getTitle());
                    }
                }
                return true; // Then return true
            }
        }

        return false;
    }

    public void updateTopPlayed() {
        if (!topPlayed.isEmpty()) { // Clear the list first before we repopulate.
            topPlayed.clear();
        }

        HashMap<Long, Long> count = mediaDB.retrieveCountFromDB();
        // Then we update the the database with the songfiles
        for (Song s : songFiles) {
            s.setCount(count.get(s.getId()));
        }

        // Finally, update the playlist via the Database
        for (Long mediaStoreId : mediaDB.retrieveTopPlayed()) {
            for (Song s : songFiles) {
                if (mediaStoreId == s.getId()) {
                    topPlayed.add(s);
                }
            }
        }
    }

    public void updateAllPlayCount() {
        HashMap<Long, Long> count = mediaDB.retrieveCountFromDB();

        for (Song s : songFiles) {
            s.setCount(count.get(s.getId()));
        }
    }
}
