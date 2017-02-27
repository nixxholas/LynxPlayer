package com.nixholas.materialtunes.Media;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.Playlist;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.Media.Entities.Utils.PlaylistUtil;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.RemoteControlReceiver;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static com.nixholas.materialtunes.MainActivity.getInstance;
import static com.nixholas.materialtunes.MainActivity.mediaControls_PlayPause;
import static com.nixholas.materialtunes.MainActivity.mediaSeekText_Maximum;
import static com.nixholas.materialtunes.MainActivity.mediaSeekText_Progress;
import static com.nixholas.materialtunes.MainActivity.persistentNotif;
import static com.nixholas.materialtunes.MainActivity.preferenceHelper;
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
    public MediaPlayer mMediaPlayer;
    public RemoteControlReceiver remoteControlReceiver;
    private MediaDB mediaDB;

    // MediaManager Sub Objects
    public boolean mediaPlayerIsPaused;
    public RepeatState repeatState = RepeatState.NOREPEAT; // 0 for none, 1 for repeat one, 2 for repeat all
    private Random shufflerRandomizer;
    private PlaybackState mPlaybackState;
    public int currentlyPlayingIndex; // This is binded with the managerQueue, so that we'll know what is up.

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
    private LinkedList<Song> managerQueue = new LinkedList<>(); // We need a queue for the mediaplayer
    private Stack<Song> historyStack = new Stack<>();
    protected ArrayList<Song> songFiles = new ArrayList<>();
    protected ArrayList<Album> albumFiles = new ArrayList<>();
    protected ArrayList<Playlist> playLists = new ArrayList<>();
    protected ArrayList<Song> topPlayed = new ArrayList<>();

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

    /**
     * Creating an enum for the repeat state validation is more efficient than utilizing the
     * shared preferences integer because the number of calls required may potentially result in
     * an increased level of code inefficiency.
     */
    public enum RepeatState {
        NOREPEAT,
        REPEATONE,
        REPEATALL
    }

    public void initializeMediaDB(Context context) {
        mediaDB = new MediaDB(context);
    }

    public MediaManager() {} // Don't use this at all please, it's pointless, like your life lol.

    public MediaManager(final MainActivity mainActivity) {
        //Log.e("onCreate: MediaManager", "Working");
        audioManager = (AudioManager) mainActivity.getSystemService(Context.AUDIO_SERVICE);

        //mediaDB = new MediaDB(mainActivity.getApplicationContext()); // Instantiate the SQLite Object

        // Instantiate the MediaPlayer Object
        mMediaPlayer = new MediaPlayer();

        // Retrieve the Last Played Object
        setCurrent(preferenceHelper.getLastPlayedSong());

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
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        mediaPlayerIsPaused = false;
        shufflerRandomizer = new Random();
        mPlaybackState = new PlaybackState.Builder()
                .setState(PlaybackState.STATE_NONE, 0, 1.0f)
                .build();

        /**
         * Some management methods for the MediaPlayer Object
         *
         * @param mediaPlayer http://stackoverflow.com/questions/10529226/notify-once-the-audio-is-finished-playing
         */
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mediaPlayer) {
                    // Check to make sure it's not hidden
                    if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }

                    // Get a handler that can be used to post to the main thread
                    // http://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread
                    final Handler mainHandler = new Handler(getInstance().getMainLooper());

                    //Log.e("OnPrepared", "Working");
                    long songDuration = getCurrent().getDuration();

                    slided_SongTitle.setText(getCurrent().getTitle());
                    slided_SongArtist.setText(getCurrent().getArtistName());
                    preferenceHelper.setCurrentSongId(getCurrent().getId());

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
                            Log.d("mainHandler", "mediaPlayerIsPaused: " + mediaPlayerIsPaused);

                                if (!mediaPlayerIsPaused) {

                                    if (mMediaPlayer != null) {
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

                                } else {
                                    // Don't update if it's not playing..
                                    //repeat yourself that again in 100 miliseconds
                                    mainHandler.postDelayed(this, 100);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mainHandler.post(progressRunnable);

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

                persistentNotif.updateNotification();

                mediaPlayer.start();
                setmPlaybackState(new PlaybackState.Builder()
                        .setState(PlaybackState.STATE_PLAYING,
                                mMediaPlayer.getCurrentPosition(), 1.0f)
                        .build());
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //Log.e("Completed", "Yep");

                try {
                    if (repeatState == RepeatState.REPEATALL) {
                        // Since it's repeat all, naturally it mimics an onClick Next
                        // so that we can reuse code instead

                        // Add the completed song to the history first
                        historyStack.add(getCurrent());

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
                    } else {
                        //Since it's repeat one, let's replay it again.

                        // Make sure it's stopped
                        mMediaPlayer.stop();

                        // Then play it again
                        mediaPlayer.prepare();
                    }

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

    public LinkedList<Song> getManagerQueue() {
        return managerQueue;
    }

    public ArrayList<Song> getSongFiles() {
        return songFiles;
    }

    public ArrayList<Album> getAlbumFiles() {
        return albumFiles;
    }

    public ArrayList<Playlist> getPlayLists() {
        return playLists;
    }

    public ArrayList<Song> getTopPlayed() {
        return topPlayed;
    }

    public Stack<Song> getHistoryStack() {
        return historyStack;
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

    public void setmPlaybackState(PlaybackState state) { mPlaybackState = state; }

    public Song getCurrent() {
        try {
            // If the MediaManager has already been playing
            if (!managerQueue.isEmpty() && currentlyPlayingIndex >= 0
                    && currentlyPlayingIndex < managerQueue.size()) {

                return managerQueue.get(currentlyPlayingIndex);
            } else { // If we fail to find it, let's fix it
                // This happens most of the time when the player just started
                mMediaPlayer.reset();

                // Retrieve the current song from shared preferences
                Song newCurrentSong = preferenceHelper.getLastPlayedSong();

                mMediaPlayer.setDataSource(newCurrentSong.getDataPath());
                mMediaPlayer.prepare();
                mMediaPlayer.pause();
                mediaPlayerIsPaused = true;

                // Add it to the queue
                managerQueue.add(newCurrentSong);

                // Set the currentlyPlayingIndex to the newCurrentSong's Index
                currentlyPlayingIndex = managerQueue.indexOf(newCurrentSong);

                // Make sure we perform repeat and shuffle checks


                // Return in
                return newCurrentSong;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void setCurrent(Song song) {
        currentlyPlayingIndex = managerQueue.indexOf(song);
        preferenceHelper.setLastPlayedSong(song);
    }

    public Song getNext() {
        Log.d("getNext()", "Running getNext()");

        // Make sure to add the current song to the history stack first
        historyStack.push(getCurrent());

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
        // Let's use the history stack now.
        Log.d("historyStack", "isEmpty -> " + historyStack.isEmpty());

        if (!historyStack.isEmpty()) {
            return historyStack.pop();
        } else { // Return the current song since there isn't any history.
            return getCurrent();
        }
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

    // Updates the songFiles and the topPlayed
    public void updateTopPlayed() {
        if (!topPlayed.isEmpty()) { // Clear the list first before we repopulate.
            topPlayed.clear();
        }

        HashMap<Long, Long> count = mediaDB.retrieveCountFromDB();
        // Then we update the the database with the songfiles
        for (Song s : songFiles) {
            if (count.containsKey(s.getId())) {
                s.setCount(count.get(s.getId()));
            }
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

    // Updates the songFiles
    public void updateAllPlayCount() {
        HashMap<Long, Long> count = mediaDB.retrieveCountFromDB();

        if (!count.isEmpty()) {
            for (Song s : songFiles) {
                if (count.containsKey(s.getId())) {
                    s.setCount(count.get(s.getId()));
                }
            }
        } else {
            Toast.makeText(MainActivity.getInstance(), "No Data Found for Play Count.", Toast.LENGTH_SHORT).show();
        }
    }

    public void purgeMediaplayer() {
        if (mMediaPlayer != null) { // Check first
            if (mMediaPlayer.isPlaying()) {
                //mMediaPlayer.pause();
                //mMediaPlayer.stop();
                mMediaPlayer.reset();
            } else {
                mMediaPlayer.reset();
            }
        }
    }
}
