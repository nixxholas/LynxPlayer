package com.nixholas.materialtunes.Media;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;

import static com.nixholas.materialtunes.MainActivity.getInstance;
import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.MainActivity.persistentNotif;

/**
 * Created by nixho on 24-Nov-16.
 */

public class MediaNotifUpdater {
    private static String LOGSTR = "MediaNU";

    public static void mediaNotifOnClickPrevious() {
        try {
            final Song prevSong = mediaManager.getPrevious();

            Uri audioUri = Uri.parse("file://" + prevSong.getDataPath());

            if (mediaManager.mMediaPlayer.isPlaying() || mediaManager.mediaPlayerIsPaused) {
                mediaManager.mMediaPlayer.stop();
                mediaManager.mMediaPlayer.reset();
                mediaManager.mMediaPlayer.setDataSource(getInstance(), audioUri);
                mediaManager.mMediaPlayer.prepareAsync();
                mediaManager.mediaPlayerIsPaused = false;

                persistentNotif.updateNotification();
            }

        } catch (Exception e) {
            Log.e(LOGSTR + "OnClickPrevious", e.toString());
        }
    }

    public static void mediaNotifOnClickNext() {
        try {
            //Log.e("onClickNext", "Working");
            //Log.e("mMediaPlayer.isPlaying", mediaManager.mMediaPlayer.isPlaying() + "");
            /**
             * If getInstance().getCurrentFocus() is used, there will be a high probability of
             * com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView{18335a0 VFED..... .F.....D 0,0-1440,2070 #7f0c00a9 app:id/main_RecyclerView}
             * which in other words mean that the UI might fail to load on the right view.
             *
             * Glide is still failing to load on the right context because of the wrong view...
             */
            //View v = getInstance().getCurrentFocus().getRootView();

            final Song nextSong = mediaManager.getNext();

            Uri audioUri = Uri.parse("file://" + nextSong.getDataPath());

            if (mediaManager.mMediaPlayer.isPlaying() || !mediaManager.mediaPlayerIsPaused) {
                mediaManager.mMediaPlayer.stop();
                mediaManager.mMediaPlayer.reset();
                mediaManager.mMediaPlayer.setDataSource(getInstance(), audioUri);
                mediaManager.mMediaPlayer.prepareAsync();
                mediaManager.mediaPlayerIsPaused = false;

                persistentNotif.updateNotification();
            }
        } catch (Exception e) {
            Log.e(LOGSTR + "OnClickNext", e.toString());
        }
    }

}
