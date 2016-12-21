package com.nixholas.materialtunes.Media.Entities.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nixholas.materialtunes.Media.Entities.Playlist;

import static android.content.ContentValues.TAG;
import static com.nixholas.materialtunes.MainActivity.getInstance;
import static com.nixholas.materialtunes.MainActivity.mDataAdapter;

/**
 * Created by nixholas on 20/12/16.
 */

public class PlaylistUtil {
    private static ContentValues[] mContentValuesCache = null;
    static Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

    /**
     * Adding, Removing and Renaming a Playlist.
     *
     * http://stackoverflow.com/questions/13154662/how-to-delete-a-playlist-from-mediastore-audio-playlists-in-android
     *
     * @param context
     * @param newplaylist
     */
    public static void addNewPlaylist(Context context, String newplaylist) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, newplaylist);
        resolver.insert(uri, values);
        mDataAdapter.updatePlaylistData();
    }

    public static void removePlaylist(Context context, long selectedPlaylist) {
        Log.i(TAG, "deletePlaylist");
        ContentResolver resolver = context.getContentResolver();
        String where = MediaStore.Audio.Playlists._ID + "=?";
        String[] whereVal = {selectedPlaylist + ""};
        resolver.delete(uri, where, whereVal);
        mDataAdapter.updatePlaylistData();
        //Log.d("PlaylistUtil", "removePlaylist");
    }

    public void renamePlaylist(Context context, String newplaylist, long playlist_id) {
        // Alternative Method
//        ContentResolver resolver = context.getContentResolver();
//        ContentValues values = new ContentValues(1);
//        values.put(MediaStore.Audio.Playlists.NAME, "NewPlaylist");
//
//        resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
//                values, "_id=" + selectedPlaylist, null);

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        String where = MediaStore.Audio.Playlists._ID + " =? ";
        String[] whereVal = { Long.toString(playlist_id) };
        values.put(MediaStore.Audio.Playlists.NAME, newplaylist);
        resolver.update(uri, values, where, whereVal);
        mDataAdapter.updatePlaylistData();
    }

    static String getPlayListId(String playlist ) {
        final String playlistid = MediaStore.Audio.Playlists._ID;
        final String playlistname = MediaStore.Audio.Playlists.NAME;
        String where = MediaStore.Audio.Playlists.NAME + "=?";
        String[] whereVal = {playlist};
        String[] projection = {playlistid, playlistname};
        ContentResolver resolver = getInstance().getContentResolver();
        Cursor record = resolver.query(uri , projection, where, whereVal, null);
        int recordcount = record.getCount();

        String foundplaylistid = "";

        if (recordcount > 0) {
            record.moveToFirst();
            int idColumn = record.getColumnIndex(playlistid);
            foundplaylistid = record.getString(idColumn);
            record.close();
        }

        return foundplaylistid;
    }

    //Adds the specified song to Android's MediaStore.
    public static void addToMediaStorePlaylist(ContentResolver resolver, int audioId, long playlistId) {
        String[] cols = new String[] {"count(*)"};
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base + audioId));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        resolver.insert(uri, values);

    }

    public static void removeFromPlaylist(ContentResolver resolver, int audioId, long playlistId) {
        String[] cols = new String[] {"count(*)"};
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();

        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=" + audioId, null);

    }

    public void addToPlaylist(final Context context, final long[] ids, final long playlistid) {
        final int size = ids.length;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + "play_order" + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cursor = null;
        int base = 0;

        try {
            cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        int numinserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            makeInsertItems(ids, offSet, 1000, base);
            numinserted += resolver.bulkInsert(uri, mContentValuesCache);
        }
    }

    public void makeInsertItems(final long[] ids, final int offset, int len, final int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }

        if (mContentValuesCache == null || mContentValuesCache.length != len) {
            mContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (mContentValuesCache[i] == null) {
                mContentValuesCache[i] = new ContentValues();
            }
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }

}
