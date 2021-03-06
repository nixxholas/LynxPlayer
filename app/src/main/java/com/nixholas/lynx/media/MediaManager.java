package com.nixholas.lynx.media;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.nixholas.lynx.adapters.DataAdapter;
import com.nixholas.lynx.media.entities.Album;
import com.nixholas.lynx.media.entities.Playlist;
import com.nixholas.lynx.media.entities.Song;
import com.nixholas.lynx.ui.activities.MainActivity;
import com.nixholas.lynx.utils.AlbumService;
import com.nixholas.lynx.utils.RemoteControlReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static com.nixholas.lynx.ui.activities.MainActivity.getInstance;
import static com.nixholas.lynx.ui.activities.MainActivity.preferenceHelper;

/**
 * Created by nixho on 02-Nov-16.
 * <p>
 * 20 November 2016
 * Found the API Level 21 and beyond method for media playback
 * https://docs.google.com/presentation/d/1jlI8ty6jq8NdstefSeZeGrBq8yE2avBOXTBo175Kgyg/edit?usp=sharing
 */

public class MediaManager extends Service {
    private static String TAG = "MediaManager";
    // Action Strings

    // Standard actions
    public static final String LYNX_ACTION_PLAY = "action_play";
    public static final String LYNX_ACTION_PAUSE = "action_pause";
    public static final String LYNX_ACTION_REWIND = "action_rewind";
    public static final String LYNX_ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String LYNX_ACTION_NEXT = "action_next";
    public static final String LYNX_ACTION_PREVIOUS = "action_previous";
    public static final String LYNX_ACTION_STOP = "action_stop";

    // Command sources
    public static final String LYNX_SOURCE_PHYSICAL_BUTTON = "lynx_physical_button";

    // Static Variables for Data Sets
    private static int HISTORY_LIMIT = 1000;

    // Main Objects for MediaManager
    public AudioManager audioManager;

    /**
     * Lock screen controls
     */
    private MediaSession mSession;

    /**
     * Handles music playback
     */
    public LynxMediaPlayer mLynxMediaPlayer;

    /**
     * Alarm intent for removing the notification when nothing is playing
     * for some time
     */
    private AlarmManager mAlarmManager;
    private PendingIntent mShutdownIntent;
    private boolean mShutdownScheduled;

    public RemoteControlReceiver remoteControlReceiver;
    private MediaDB mediaDB;

    private PowerManager.WakeLock mHeadsetHookWakeLock;

    /**
     * mHandler
     *
     * This handler allows an extra thread to reconnect with the main thread so as to offload tasks
     * that are being executed/waiting to be executed in the main thread.
     */
    Handler mainHandler;

    /**
     * Runnable, progressRunable
     *
     * This runnable allows us to carefully manage the seekbar for the current song that is being
     * played without reducing the usage of the main thread which is heavily used during general
     * usage.
     */
    Runnable progressRunnable;

    // MediaManager Sub Objects
    public boolean mediaPlayerIsPaused = true;
    private RepeatState repeatState = RepeatState.NOREPEAT; // 0 for none, 1 for repeat one, 2 for repeat all
    private Random shufflerRandomizer;
    public int currentlyPlayingIndex; // This is binded with the managerQueue, so that we'll know what is up.
    private boolean isThisLastPlayed = false;

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
    private static LinkedList<Integer> mUpcoming = new LinkedList<>();
    private static Stack<Integer> mHistory = new Stack<>();
    public DataAdapter mDataAdapter;
    protected ArrayList<Song> topPlayed = new ArrayList<>(20);

    // Playlist Helper
    //public PlaylistUtil playlistUtil = new PlaylistUtil();

    public class ServiceBinder extends Binder {
        public MediaManager getService() {
            return MediaManager.this;
        }
    }

    private Binder mBinder = new MediaManager.ServiceBinder();

    public MediaSession.Token getMediaSessionToken() {
        return mSession.getSessionToken();
    }

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

    public MediaManager(ContentResolver contentResolver) {
        //Log.d("onCreate: MediaManager", "Working");

        // Setup the Data adapter
        mDataAdapter = new DataAdapter(contentResolver);

        // Initialize the audio manager and register any headset controls for playback
        audioManager = (AudioManager) getInstance().getSystemService(Context.AUDIO_SERVICE);

        // Get a handler that can be used to post to the main thread
        // http://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread
        mainHandler = new Handler(getInstance().getMainLooper());

        initializeMediaDB(getInstance().getApplicationContext()); // Instantiate the SQLite Object

        // Instantiate the MediaPlayer Object
        mLynxMediaPlayer = new LynxMediaPlayer(this);

        // Setup the shuffle randomizer
        shufflerRandomizer = new Random();
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

    public ArrayList<Song> getSongDataset() {
        return getInstance().songFragment.rVAdapter.mDataset;
    }

    public ArrayList<Album> getAlbumDataset() {
        return getInstance().albumFragment.rVAdapter.mDataset;
    }

    public ArrayList<Playlist> getPlaylistDataset() {
        return getInstance().playlistFragment.rVAdapter.mDataset;
    }

    public ArrayList<Song> getTopPlayed() {
        return topPlayed;
    }

    private String placeZeroIfNeeded(long number) {
        return (number >=10)? Long.toString(number):String.format("0%s",Long.toString(number));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Song getCurrent() {
        try {
            // If the MediaManager has already been playing
            if (getSongDataset() != null && !getSongDataset().isEmpty()
                    && currentlyPlayingIndex >= 0
                    && currentlyPlayingIndex <= getSongDataset().size()) {
                return getSongDataset().get(currentlyPlayingIndex);
            } else { // If we fail to find it, let's fix it
                // Retrieve the current song index from shared preferences
                int lastPlayedSongIndex = preferenceHelper.getLastPlayedSong();

                if (lastPlayedSongIndex <= getSongDataset().size() && lastPlayedSongIndex >= 0) {
                    // Retrieve the current song
                    Song newCurrentSong = getSongDataset().get(lastPlayedSongIndex);

                    // Set the currentlyPlayingIndex to the newCurrentSong's Index
                    currentlyPlayingIndex = lastPlayedSongIndex;

                    // Make sure we perform repeat and shuffle checks


                    // Return it
                    return newCurrentSong;
                } else {
                    // Since the user does not have a last played song yet, we'll give him the first
                    // song he has.
                    if (!getSongDataset().isEmpty()) {
                        return getSongDataset().get(0);
                    }

                    return null; // Return null if there still isn't anything.
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

//    public void setupLastPlayed() {
//        Log.d("setupLastPlayed()", "Setting up");
//
//        // Retrieve the Last Played Object
//        setCurrent(preferenceHelper.getLastPlayedSong());
//
//        try {
//            // Set it up
//            Uri audioUri = Uri.parse("file://" + getSongDataset().get(currentlyPlayingIndex).getDataPath());
//
//            isThisLastPlayed = true;
//
//            Log.d("setupLastPlayed()", "Resetting mMediaPlayer");
//            mMediaPlayer.reset();
//            mMediaPlayer.setDataSource(getInstance().getApplicationContext(), audioUri);
//            mMediaPlayer.prepare();
//            mMediaPlayer.stop();
//
//            //Log.d("setupLastPlayed()", "Pausing mMediaPlayer");
//            //            mediaPlayerIsPaused = true;
//            //            mMediaPlayer.pause();
//
//            Log.d("setupLastPlayed()", "Calling updateSliderBar()");
//            // Update the UI
//            updateSlideBar(getInstance());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    public void setCurrent(int index) {
        // Update the currentlyPlayingIndex
        currentlyPlayingIndex = index;

        // Update the sharedpreferences to be in sync with the current state.
        preferenceHelper.setLastPlayedSong(currentlyPlayingIndex);
    }

    public Song getNext() {
        Log.d("getNext()", "Running getNext()");

        // Make sure to add the current song to the history stack first
        mHistory.add(currentlyPlayingIndex);

        // Let's check if the user has added anything to upcoming or not
        if (!mUpcoming.isEmpty()) {
            // Since he/she did add something, we'll play it first
            Log.d("getNext()", "Returning upcoming song");
            return getSongDataset().get(mUpcoming.removeFirst());
        }

        if (preferenceHelper.getShuffle()) { // If shuffle mode is on
            Log.d("getNext()", "Shuffle mode is on, running shuffle mode code");

            // Shuffle and retrieve the song
            int newSong = currentlyPlayingIndex;
            if (getSongDataset().size() > 0) { // Don't let a Deadlock happen
                while (newSong == currentlyPlayingIndex) {
                    newSong = shufflerRandomizer.nextInt(getSongDataset().size());
                }
            }
            currentlyPlayingIndex = newSong;
        } else {
            // else retrieve the next song with an increment on currentlyPlayingIndex
            currentlyPlayingIndex++;
            if (currentlyPlayingIndex == getSongDataset().size()) { // Make it circular
                currentlyPlayingIndex = 0;
            }
        }

        // Debugging Purporses only.
        Log.d("getNext()", "Returning Song object with currentlyPlayingIndex of "
                + currentlyPlayingIndex);

        return getSongDataset().get(currentlyPlayingIndex);
    }

    public Song getPrevious() {
        // Let's use the history stack now.
        Log.d("historyStack", "isEmpty -> " + mHistory.isEmpty());

        if (!mHistory.isEmpty()) {
            return getSongDataset().get(mHistory.pop());
        } else { // Return the current song since there isn't any history.
            return getSongDataset().get(currentlyPlayingIndex);
        }
    }

    /**
     * This method is made such that all the songs are queued since the user is playing a song
     * directly from the songs tab
     * @param currentSong
     */
    public void putAllOnQueue(Song currentSong) {
        Log.d("putAllOnQueue()", "Current Song: " + currentSong.getTitle());

        for (Song s : getSongDataset()) {
            if (s.getId() != currentSong.getId()) {
                // Add the song to the queue
                mUpcoming.add(getSongDataset().indexOf(s));
            }
        }
    }

    public void putAlbumOnQueue(ArrayList<Song> songs, Song dupe) {
        for (Song s : songs) {
            if (s != dupe) {
                mUpcoming.add(getSongDataset().indexOf(s));
            }
        }
    }

    public RepeatState getRepeatState() {
        return repeatState;
    }

    public void setRepeatState(RepeatState repeatState) {
        this.repeatState = repeatState;
    }

    public void purgeList(long playlistId) {
        for (int i = 0; i < getInstance().playlistFragment.rVAdapter.mDataset.size(); i++) {
            if (getInstance().playlistFragment.rVAdapter.mDataset.get(i).getPlaylistId() == playlistId) {
                getInstance().playlistFragment.rVAdapter.mDataset.remove(i);
                break;
            }
        }
    }

    public ArrayList<Song> getAlbumSongs(long albumId) {
        ArrayList<Song> result = new ArrayList<>();

        for (Song s : getSongDataset()) {
            if (s.getAlbumId() == albumId) {
                result.add(s);
            }
        }

        return result;
    }

    // Updates the songFiles and the topPlayed
    public void updateTopPlayed() {
        if (!topPlayed.isEmpty()) { // Clear the list first before we repopulate.
            topPlayed.clear();
        }

        HashMap<Long, Long> count = mediaDB.retrieveCountFromDB();
        // Then we update the the database with the songfiles
        for (Song s : getSongDataset()) {
            if (count.containsKey(s.getId())) {
                s.setCount(count.get(s.getId()));
            }
        }

        // Finally, update the playlist via the Database
        for (Long mediaStoreId : mediaDB.retrieveTopPlayed()) {
            for (Song s : getSongDataset()) {
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
            for (Song s : getSongDataset()) {
                if (count.containsKey(s.getId())) {
                    s.setCount(count.get(s.getId()));
                }
            }
        } else {
            Toast.makeText(MainActivity.getInstance(), "No Data Found for Play Count.", Toast.LENGTH_SHORT).show();
        }
    }

    public void purgeMediaplayer() {
        if (mLynxMediaPlayer != null) { // Check first
            mLynxMediaPlayer.reset();
        }
    }

    /**
     * Method updateMetaData()
     *
     * This method allows us to update the metadata relevant with the song object and bind it with
     * mSession so that external controls and services can utilize it to control mMediaPlayer
     * easily.
     */
    private void updateMetaData() {
        Bitmap albumArt = AlbumService.getAlbumArt(getApplicationContext(),
                getCurrent().getAlbumId());
        // Update the current metadata
        mSession.setMetadata(new MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, getCurrent().getArtistName())
                .putString(MediaMetadata.METADATA_KEY_ALBUM, getCurrent().getAlbumName())
                .putString(MediaMetadata.METADATA_KEY_TITLE, getCurrent().getTitle())
                .build());
    }

    public void updateMediaDatabase() {
        Log.d(TAG, "Running updateMediaDatabase()");

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
}
