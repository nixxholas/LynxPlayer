package com.nixholas.lynx.media;

import android.media.MediaPlayer;

/**
 * Created by nixholas on 12/3/17.
 */

public class LynxMediaPlayer extends MediaPlayer implements MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {
    private String currentDataPath;

    public String getCurrentDataPath() {
        return currentDataPath;
    }

    public void setCurrentDataPath(String currentDataPath) {
        this.currentDataPath = currentDataPath;
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

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }
}
