package com.nixholas.lynx.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import static com.nixholas.lynx.ui.activities.MainActivity.mediaManager;

/**
 * Created by nixho on 20-Nov-16.
 *
 * We'll have to handle hardware changes soon.
 *
 * https://developer.android.com/training/managing-audio/volume-playback.html#PlaybackControls
 */

public class RemoteControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive()", "Running method");

        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                if (mediaManager.mLynxMediaPlayer.isPlaying()) {
                    mediaManager.mLynxMediaPlayer.start();
                }
            } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) {
                if (mediaManager.mLynxMediaPlayer.isPlaying()) {
                    mediaManager.mLynxMediaPlayer.pause();
                }
            }
        }
    }
}