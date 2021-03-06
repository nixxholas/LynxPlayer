package com.nixholas.lynx.media.entities.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;

import static com.nixholas.lynx.ui.activities.MainActivity.getInstance;

/**
 * Created by nixholas on 20/12/16.
 */

public class SongUtil {
    static Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public static void removeSong(long selectSong) {
        // Log.i(TAG, "deletePlaylist");
        ContentResolver resolver = getInstance().getContentResolver();
        String where = MediaStore.Audio.Media._ID + "=?";
        String[] whereVal = {selectSong + ""};
        resolver.delete(uri, where, whereVal);
        //mDataAdapter.updateSongData();
        //Log.d("PlaylistUtil", "removePlaylist");
    }

}
