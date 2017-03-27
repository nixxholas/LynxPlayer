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
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.nixholas.lynx.R;
import com.nixholas.lynx.adapters.DataAdapter;
import com.nixholas.lynx.media.entities.Album;
import com.nixholas.lynx.media.entities.Playlist;
import com.nixholas.lynx.media.entities.Song;
import com.nixholas.lynx.ui.activities.MainActivity;
import com.nixholas.lynx.utils.AlbumService;
import com.nixholas.lynx.utils.RemoteControlReceiver;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static com.nixholas.lynx.ui.MediaControlUpdater.mediaControlsOnClickNext;
import static com.nixholas.lynx.ui.SlidingBarUpdater.updateSlideBar;
import static com.nixholas.lynx.ui.activities.MainActivity.getInstance;
import static com.nixholas.lynx.ui.activities.MainActivity.mediaControls_PlayPause;
import static com.nixholas.lynx.ui.activities.MainActivity.mediaSeekText_Maximum;
import static com.nixholas.lynx.ui.activities.MainActivity.mediaSeekText_Progress;
import static com.nixholas.lynx.ui.activities.MainActivity.persistentNotif;
import static com.nixholas.lynx.ui.activities.MainActivity.preferenceHelper;
import static com.nixholas.lynx.ui.activities.MainActivity.slideButton;
import static com.nixholas.lynx.ui.activities.MainActivity.slideSongArtist;
import static com.nixholas.lynx.ui.activities.MainActivity.slideSongTitle;
import static com.nixholas.lynx.ui.activities.MainActivity.slidedSeekBar;
import static com.nixholas.lynx.ui.activities.MainActivity.slided_SongArtist;
import static com.nixholas.lynx.ui.activities.MainActivity.slided_SongTitle;
import static com.nixholas.lynx.ui.activities.MainActivity.slidingSeekBar;
import static com.nixholas.lynx.ui.activities.MainActivity.slidingUpPanelLayout;

/**
 * Created by nixho on 02-Nov-16.
 * <p>
 * 20 November 2016
 * Found the API Level 21 and beyond method for media playback
 * https://docs.google.com/presentation/d/1jlI8ty6jq8NdstefSeZeGrBq8yE2avBOXTBo175Kgyg/edit?usp=sharing
 */

public class MediaManager extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
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
    public MediaPlayer mMediaPlayer;
    private LynxMediaPlayer lynxMediaPlayer;

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
     * mainHandler
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
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        lynxMediaPlayer = new LynxMediaPlayer(this);

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
                    //mMediaPlayer.start();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager mgr = (TelephonyManager) getInstance().getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

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
            if (!getSongDataset().isEmpty() && currentlyPlayingIndex >= 0
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


                    // Return in

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

    public void setupLastPlayed() {
        Log.d("setupLastPlayed()", "Setting up");

        // Retrieve the Last Played Object
        setCurrent(preferenceHelper.getLastPlayedSong());

        try {
            // Set it up
            Uri audioUri = Uri.parse("file://" + getSongDataset().get(currentlyPlayingIndex).getDataPath());

            isThisLastPlayed = true;

            Log.d("setupLastPlayed()", "Resetting mMediaPlayer");
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(getInstance().getApplicationContext(), audioUri);
            mMediaPlayer.prepare();

            //Log.d("setupLastPlayed()", "Pausing mMediaPlayer");
            //            mediaPlayerIsPaused = true;
            //            mMediaPlayer.pause();

            Log.d("setupLastPlayed()", "Calling updateSliderBar()");
            // Update the UI
            updateSlideBar(getInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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

    public boolean findDuplicateAlbum(Album album) {
        Log.d("findDuplicateAlbum", "Running");

        try {
            if (getAlbumDataset() != null && !getAlbumDataset().isEmpty()) {

                for (Album a : getAlbumDataset()) {
                    if (a.getArtistName().equals(album.getArtistName()) &&
                            a.getTitle().equals(album.getTitle())) { // If we really find a dupe
                        for (Song s : getSongDataset()) { // Set all the existing songs
                            if (s.getAlbumId() == album.getId()) { // To the existing album
                                s.setAlbumId(a.getId());
                                s.setAlbumName(a.getTitle());
                            }
                        }
                        return true; // Then return true
                    }
                }
            }

            // Since no dupes are found
            getAlbumDataset().add(album);

            //Log.d("findDuplicateAlbum", "albumFiles is either null or is empty");
            return false;
        } catch (Exception ex) {
            Log.d("findDuplicateAlbum", "An error occured");
            ex.printStackTrace();
            return false;
        }
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
        if (mMediaPlayer != null) { // Check first
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.reset();
            } else {
                mMediaPlayer.reset();
            }
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


    /**
     * Some management methods for the MediaPlayer Object
     *
     * @param mediaPlayer http://stackoverflow.com/questions/10529226/notify-once-the-audio-is-finished-playing
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d("onCompletion", "Running");

        try {
            if (repeatState == RepeatState.REPEATALL) {
                // Since it's repeat all, naturally it mimics an onClick Next
                // so that we can reuse code instead

                // Add the completed song to the history first
                mHistory.add(currentlyPlayingIndex);

                mediaControlsOnClickNext(MainActivity.getInstance().getCurrentFocus());
            } else if (repeatState == RepeatState.NOREPEAT) {
                Song currentSong = getSongDataset().get(currentlyPlayingIndex); // Get the current song that just ended
                mMediaPlayer.reset(); // Reset the player first
                Uri audioUri = Uri.parse("file://" + currentSong.getDataPath()); // Get the path of the song
                mMediaPlayer.setDataSource(MainActivity.getInstance().getApplicationContext(), audioUri); // Set it again

                // Update the UI
                slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);

                // Then prepare the mediaplayer
                mMediaPlayer.prepare();
            } else if (repeatState == RepeatState.REPEATONE){
                //Since it's repeat one, let's replay it again.

                // Make sure it's stopped
                mMediaPlayer.stop();
                Song currentSong = getSongDataset().get(currentlyPlayingIndex); // Get the current song that just ended
                mMediaPlayer.reset(); // Reset the player first
                Uri audioUri = Uri.parse("file://" + currentSong.getDataPath()); // Get the path of the song
                mMediaPlayer.setDataSource(MainActivity.getInstance().getApplicationContext(), audioUri); // Set it again

                // Then play it again
                mMediaPlayer.prepare();
            } else {
                // Something must have gone wrong, just reset the song in that case

                // Make sure it's stopped
                mMediaPlayer.stop();
                Song currentSong = getSongDataset().get(currentlyPlayingIndex); // Get the current song that just ended
                mMediaPlayer.reset(); // Reset the player first
                Uri audioUri = Uri.parse("file://" + currentSong.getDataPath()); // Get the path of the song
                mMediaPlayer.setDataSource(MainActivity.getInstance().getApplicationContext(), audioUri); // Set it again

                // Then play it again
                mMediaPlayer.prepare();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        // Check to make sure it's not hidden
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        // Retrieve the current song
        Song currentlyPlaying = getSongDataset().get(currentlyPlayingIndex);

        //Log.d("OnPrepared", "Working");
        long songDuration = currentlyPlaying.getDuration();

        slideSongTitle.setText(currentlyPlaying.getTitle());
        slideSongArtist.setText(currentlyPlaying.getArtistName());
        slided_SongTitle.setText(currentlyPlaying.getTitle());
        slided_SongArtist.setText(currentlyPlaying.getArtistName());
        preferenceHelper.setCurrentSongId(currentlyPlaying.getId());

        // http://stackoverflow.com/questions/17168215/seekbar-and-media-player-in-android
        //Log.d("MaxDuration", getCurrent().getDuration() + "");
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

        mainHandler.removeCallbacks(progressRunnable);

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("mainHandler", "mediaPlayerIsPaused: " + mediaPlayerIsPaused);

                    if (!mediaPlayerIsPaused && mMediaPlayer != null) {
                        // http://stackoverflow.com/questions/35027321/seek-bar-and-media-player-and-a-time-of-a-track
                        //set seekbar progress
                        slidingSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                        slidedSeekBar.setProgress(mMediaPlayer.getCurrentPosition());

                        long currentPosition = mMediaPlayer.getCurrentPosition();
                        // Set current time
                        //mediaSeekText_Progress.setText(mMediaPlayer.getCurrentPosition() + "");
                        //mediaSeekText_Progress.setText(msecondsToString(mMediaPlayer.getCurrentPosition()));
                        // http://stackoverflow.com/questions/13444546/android-adt-21-0-warning-implicitly-using-the-default-locale
                        mediaSeekText_Progress.setText(String.format(Locale.ENGLISH, "%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                                TimeUnit.MILLISECONDS.toSeconds(currentPosition) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition))
                        ));

                        //repeat yourself that again in 600 miliseconds
                        mainHandler.postDelayed(this, 600);
                    } else {
                        // Don't update if it's not playing..
                        //repeat yourself that again in 600 miliseconds
                        mainHandler.postDelayed(this, 600);
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

        // Make sure to start it
        if (isThisLastPlayed) {
            // Don't need to pause()
            mediaPlayerIsPaused = true;
            isThisLastPlayed = false;
        } else {
            mMediaPlayer.start();
            mediaPlayerIsPaused = false;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

}
