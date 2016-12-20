package com.nixholas.materialtunes.Media.Adapter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.Media.Entities.Playlist;
import com.nixholas.materialtunes.Media.Entities.Song;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixho on 13-Nov-16.
 */

public class DataAdapter implements Runnable {
    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    //private static int NUMBER_OF_CORES =
    //        Runtime.getRuntime().availableProcessors();
    // Sets the amount of time an idle thread waits before terminating
    //private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    //private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    // A queue of Runnables
    //private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<>();
    // Creates a thread pool manager
//    ThreadPoolExecutor mDecodeThreadPool = new ThreadPoolExecutor(
//            NUMBER_OF_CORES,       // Initial pool size
//            NUMBER_OF_CORES,       // Max pool size
//            KEEP_ALIVE_TIME,
//            KEEP_ALIVE_TIME_UNIT,
//            mDecodeWorkQueue);


    // Resources-based Variables
//    Uri sArtworkUri = Uri
//            .parse("content://media/external/audio/albumart"); // For us to parse the albumArtUri

    ContentResolver cr;

    public DataAdapter(ContentResolver cr_) {
        this.cr = cr_;
    }

    private void loadSongData(ContentResolver cr) {
        mediaManager.songFiles.clear(); // Make sure we reset it first before we re-initialize to look for new audio files

        /**
         * Media Data Initialization Phase
         */
        // Get Content Dynamically
        Uri songsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String songsSelection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String songsSortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor songCur = cr.query(songsUri, null, songsSelection, null, songsSortOrder);
        int songCount = 0;

        if(songCur != null)
        {
            songCount = songCur.getCount();

            if(songCount > 0)
            {
                while(songCur.moveToNext())
                {
                    //String data = songCur.getString(songCur.getColumnIndex(MediaStore.Audio.Media.DATA));

                    // Debug
                    //Log.e("Song Path", data);

                    // (long _id, long _albumId, long _artistId, String _title,
                    // String _artistName, String _albumName, int _duration)
                    //Log.e("Music ID", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID)));
                    //Log.e("Music Album ID", songCur.getString(songCur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                    //Log.e("Music Artist ID", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
                    //Log.e("Music Title", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    //Log.e("Music Artist Name", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                    //Log.e("Music Album Name", songCur.getString(songCur.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                    //Log.e("Music Duration", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                    mediaManager.songFiles.add(new Song(
                            songCur.getString(songCur.getColumnIndex(MediaStore.Audio.Media.DATA)),
                            songCur.getLong(songCur.getColumnIndex(MediaStore.Audio.Media._ID)),
                            songCur.getLong(songCur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                            songCur.getLong(songCur.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)),
                            songCur.getString(songCur.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                            songCur.getString(songCur.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                            songCur.getString(songCur.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                            songCur.getInt(songCur.getColumnIndex(MediaStore.Audio.Media.DURATION))));
                }

            }

            songCur.close();
        }

    }

    private void loadAlbumData(ContentResolver cr) {
        mediaManager.albumFiles.clear(); // Make sure we reset it first before we re-initialize to look for new albums

        /**
         * Media Data Initialization Phase
         */
        // Get Content Dynamically
        Uri albumsUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String albumsSortOrder = MediaStore.Audio.Albums.ALBUM + " ASC";
        Cursor albumCur = cr.query(albumsUri,
                new String[]{"_id", "album", "artist", "artist_id", "numsongs", "minyear", "album_art"},
                null,
                null,
                albumsSortOrder);

        int albumCount = 0;

        if (albumCur != null) {
            albumCount = albumCur.getCount();

            if (albumCount > 0) {
                while (albumCur.moveToNext()) {
                    // Debug
                    //Log.e("Album ID", albumCur.getString(albumCur.getColumnIndex(MediaStore.Audio.Albums._ID)));
                    //Log.e("Album Name", albumCur.getString(albumCur.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
                    //Log.e("Album Artist", albumCur.getString(albumCur.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));
                    //Log.e("Album Artist ID", albumCur.getString(albumCur.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
                    //Log.e("Album Song Count", albumCur.getString(albumCur.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS)));
                    //Log.e("Album Year", albumCur.getString(albumCur.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR)));
                    /*Log.e("Column 0", String.valueOf(albumCur.getLong(0)));
                    Log.e("Column 1", String.valueOf(albumCur.getString(1)));
                    Log.e("Column 2", String.valueOf(albumCur.getString(2)));
                    Log.e("Column 3", String.valueOf(albumCur.getLong(3)));
                    Log.e("Column 4", String.valueOf(albumCur.getInt(4)));
                    Log.e("Column 5", String.valueOf(albumCur.getInt(5)));*/
                    Album newAlbum = new Album(
                            albumCur.getLong(0),
                            albumCur.getString(1),
                            albumCur.getString(2),
                            albumCur.getLong(3),
                            albumCur.getInt(4),
                            albumCur.getInt(5),
                            albumCur.getString(6));

                    mediaManager.albumFiles.add(newAlbum);
                }
            }

            albumCur.close();
        }

    }

    private void loadPlaylistData(ContentResolver cr) {
        mediaManager.playLists.clear(); // Make sure we reset it first before we re-initialize to look for new playlists

        /**
         * Media Data Initialization Phase
         */
        // Get Content Dynamically
        Uri playlistUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String playlistsSortOrder = MediaStore.Audio.Playlists.NAME + " ASC";
        Cursor playlistCur = cr.query(playlistUri,
                new String[]{"_id", "name"},
                null,
                null,
                playlistsSortOrder);

        int playlistCount = 0;

        if (playlistCur != null) {
            playlistCount = playlistCur.getCount();

            if (playlistCount > 0) {
                while (playlistCur.moveToNext()) {
                    // Debug
                    //Log.d("Column 0", String.valueOf(playlistCur.getLong(0)));
                    //Log.d("Column 1", String.valueOf(playlistCur.getString(1)));

                    Playlist newPlaylist = new Playlist(
                            playlistCur.getLong(0),
                            playlistCur.getString(1));

                    mediaManager.playLists.add(newPlaylist);
                }
            }

            playlistCur.close();
        }

    }

    @Override
    public void run() {
        loadAlbumData(cr);
        loadSongData(cr);
        loadPlaylistData(cr);
    }
}
