package com.nixholas.lynx.media;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.SeekBar;

import com.nixholas.lynx.R;
import com.nixholas.lynx.media.entities.Song;
import com.nixholas.lynx.ui.activities.MainActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.nixholas.lynx.ui.MediaControlUpdater.mediaControlsOnClickNext;
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
 * Created by nixholas on 12/4/17.
 */

public class LynxMediaPlayer extends MediaPlayer implements MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private final String TAG = "LynxMediaPlayer"; // Debugging TAG

    // Service binding with MediaManager
    private final WeakReference<MediaManager> mService;

    // Data storage collections
    private LinkedList<Integer> mUpcoming; // Stores the player's queue
    private Stack<Integer> mHistory; // Stores the player's history

    // Internal Native Variables
    private int mCurrentPlayerIndex; // Current index of the song list the player is playing.
    private int mNextPlayerIndex; // The next index of the song list the player is going to be playing.
    private boolean mPaused;

    /**
     * mHandler
     *
     * This handler allows an extra thread to reconnect with the main thread so as to offload tasks
     * that are being executed/waiting to be executed in the main thread.
     */
    private Handler mHandler;

    /**
     * Runnable, progressRunable
     *
     * This runnable allows us to carefully manage the seekbar for the current song that is being
     * played without reducing the usage of the main thread which is heavily used during general
     * usage.
     */
    private Runnable progressRunnable;

    /**
     * This Listener allows LynxMediaPlayer to handle calls in conjunction with media playback
     * to prevent overlapping audio during phone calls.
     */
    PhoneStateListener mPhoneStateListener;

    public LynxMediaPlayer(MediaManager _mService) {
        // Bind the MediaPlayer with the service
        mService = new WeakReference<>(_mService);

        // Provide a partial wake lock access
        setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);

        // Initialize the queue handlers
        mUpcoming = new LinkedList<>();
        mHistory = new Stack<>();

        // Initialize the internal native variables
        mCurrentPlayerIndex = -1;
        mNextPlayerIndex = -1;
        mPaused = true;

        // Setup the listeners
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    if (mPaused) { // Make sure the player is not paused deliberately first
                        pause();
                    }
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    if (!mPaused) { // Make sure the player is not paused deliberately first
                        start();
                    }
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    // Phone is on idle, so we shall start playing.
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager mgr = (TelephonyManager) getInstance().getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("onCompletion", "Running");

        try {
            Song nextSong = mService.get().getNext();
            Song currentSong = mService.get().getCurrent();
            MediaManager.RepeatState repeatState = mService.get().getRepeatState();

            if (repeatState == MediaManager.RepeatState.REPEATALL) {
                // Since it's repeat all, naturally it mimics an onClick Next
                // so that we can reuse code instead

                // Add the completed song to the history first
                mHistory.add(mCurrentPlayerIndex);

                mediaControlsOnClickNext(MainActivity.getInstance().getCurrentFocus());
            } else if (repeatState == MediaManager.RepeatState.NOREPEAT) {
                reset(); // Reset the player first
                Uri audioUri = Uri.parse("file://" + currentSong.getDataPath()); // Get the path of the song
                setDataSource(MainActivity.getInstance().getApplicationContext(), audioUri); // Set it again

                // Update the UI
                slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);

                // Then prepare the mediaplayer
                prepare();
            } else if (repeatState == MediaManager.RepeatState.REPEATONE){
                //Since it's repeat one, let's replay it again.

                // Make sure it's stopped
                stop();
                reset(); // Reset the player first

                mp.setDataSource(MainActivity.getInstance().getApplicationContext(),
                        Uri.parse("file://" + currentSong.getDataPath())); // Set it again

                // Then play it again
                prepare();
            } else {
                // Something must have gone wrong, just reset the song in that case

                // Make sure it's stopped
                stop();
                reset(); // Reset the player first

                setDataSource(MainActivity.getInstance().getApplicationContext(),
                        Uri.parse("file://" + currentSong.getDataPath())); // Set it again

                // Then play it again
                prepare();
                // Pause it before it can play
                pause();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "An error has occured");
        Log.e(TAG, "What = " + what + "\t Extra = " + extra);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Check to make sure it's not hidden
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        // Retrieve the current song
        Song currentlyPlaying = mService.get().getCurrent();

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
                    seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mHandler.removeCallbacks(progressRunnable);

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("mHandler", "mediaPlayerIsPaused: " + mPaused);

                    if (!mPaused) {
                        // http://stackoverflow.com/questions/35027321/seek-bar-and-media-player-and-a-time-of-a-track
                        //set seekbar progress
                        slidingSeekBar.setProgress(getCurrentPosition());
                        slidedSeekBar.setProgress(getCurrentPosition());

                        long currentPosition = getCurrentPosition();
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
                        mHandler.postDelayed(this, 600);
                    } else {
                        // Don't update if it's not playing..
                        //repeat yourself that again in 600 miliseconds
                        mHandler.postDelayed(this, 600);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mHandler.post(progressRunnable);

        // Finally, bump the counter in the SQLite table
        mService.get().updateMediaDatabase();

        persistentNotif.updateNotification();
    }
}
