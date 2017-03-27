package com.nixholas.lynx.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by nixholas on 12/3/17.
 */

public class LynxMediaPlayer implements MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {
    private static final String TAG = "LynxMediaPlayer";
    private final WeakReference<MediaManager> mService;

    private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

    private MediaPlayer mNextMediaPlayer;

    private Handler mHandler;

    private boolean mIsInitialized = false;

    private String mNextMediaPath;

    /**
     * Constructor of <code>LynxMediaPlayer</code>
     */
    LynxMediaPlayer(final MediaManager service) {
        mService = new WeakReference<MediaManager>(service);
    }

    /**
     * @param path The path of the file, or the http/rtsp URL of the stream
     *            you want to play
     */
    public void setDataSource(final String path) {
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
        if (mIsInitialized) {
            setNextDataSource(null);
        }
    }

    /**
     * @param player The {@link MediaPlayer} to use
     * @param path The path of the file, or the http/rtsp URL of the stream
     *            you want to play
     * @return True if the <code>player</code> has been prepared and is
     *         ready to play, false otherwise
     */
    private boolean setDataSourceImpl(final MediaPlayer player, final String path) {
        try {
            player.reset();
            player.setOnPreparedListener(null);
            if (path.startsWith("content://")) {
                player.setDataSource(mService.get(), Uri.parse(path));
            } else {
                player.setDataSource(path);
            }
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);

            player.prepare();
        } catch (final IOException todo) {
            // TODO: notify the user why the file couldn't be opened
            return false;
        } catch (final IllegalArgumentException todo) {
            // TODO: notify the user why the file couldn't be opened
            return false;
        }
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        return true;
    }

    /**
     * Set the MediaPlayer to start when this MediaPlayer finishes playback.
     *
     * @param path The path of the file, or the http/rtsp URL of the stream
     *            you want to play
     */
    public void setNextDataSource(final String path) {
        mNextMediaPath = null;
        try {
            mCurrentMediaPlayer.setNextMediaPlayer(null);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Next media player is current one, continuing");
        } catch (IllegalStateException e) {
            Log.e(TAG, "Media player not initialized!");
            return;
        }
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
        if (path == null) {
            return;
        }
        mNextMediaPlayer = new MediaPlayer();
        mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
        if (setDataSourceImpl(mNextMediaPlayer, path)) {
            mNextMediaPath = path;
            mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
        } else {
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
        }
    }

    /**
     * Sets the handler
     *
     * @param handler The handler to use
     */
    public void setHandler(final Handler handler) {
        mHandler = handler;
    }

    /**
     * @return True if the player is ready to go, false otherwise
     */
    public boolean isInitialized() {
        return mIsInitialized;
    }

    /**
     * Starts or resumes playback.
     */
    public void start() {
        mCurrentMediaPlayer.start();
    }

    /**
     * Resets the MediaPlayer to its uninitialized state.
     */
    public void stop() {
        mCurrentMediaPlayer.reset();
        mIsInitialized = false;
    }

    /**
     * Releases resources associated with this MediaPlayer object.
     */
    public void release() {
        mCurrentMediaPlayer.release();
    }

    /**
     * Pauses playback. Call start() to resume.
     */
    public void pause() {
        mCurrentMediaPlayer.pause();
    }

    /**
     * Gets the duration of the file.
     *
     * @return The duration in milliseconds
     */
    public long duration() {
        return mCurrentMediaPlayer.getDuration();
    }

    /**
     * Gets the current playback position.
     *
     * @return The current position in milliseconds
     */
    public long position() {
        return mCurrentMediaPlayer.getCurrentPosition();
    }

    /**
     * Gets the current playback position.
     *
     * @param whereto The offset in milliseconds from the start to seek to
     * @return The offset in milliseconds from the start to seek to
     */
    public long seek(final long whereto) {
        mCurrentMediaPlayer.seekTo((int) whereto);
        return whereto;
    }

    /**
     * Sets the volume on this player.
     *
     * @param vol Left and right volume scalar
     */
    public void setVolume(final float vol) {
        mCurrentMediaPlayer.setVolume(vol, vol);
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId The audio session ID
     */
    public void setAudioSessionId(final int sessionId) {
        mCurrentMediaPlayer.setAudioSessionId(sessionId);
    }

    /**
     * Returns the audio session ID.
     *
     * @return The current audio session ID.
     */
    public int getAudioSessionId() {
        return mCurrentMediaPlayer.getAudioSessionId();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }
}
