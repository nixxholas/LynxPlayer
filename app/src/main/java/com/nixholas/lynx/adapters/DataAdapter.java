package com.nixholas.lynx.adapters;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.nixholas.lynx.media.entities.Album;
import com.nixholas.lynx.media.entities.Playlist;
import com.nixholas.lynx.media.entities.Song;

import java.util.ArrayList;

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
    private ContentResolver cr;

    public DataAdapter(ContentResolver cr_) {
        this.cr = cr_;
    }

    public void updateSongDataset(ArrayList<Song> mDataSet) {
        if (mDataSet != null)
            mDataSet.clear(); // Make sure we reset it first before we re-initialize to look for new audio files

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
                    //Log.d("Song Path", data);

                    // (long _id, long _albumId, long _artistId, String _title,
                    // String _artistName, String _albumName, int _duration)
                    //Log.d("Music ID", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID)));
                    //Log.d("Music Album ID", songCur.getString(songCur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                    //Log.d("Music Artist ID", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
                    //Log.d("Music Title", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    //Log.d("Music Artist Name", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                    //Log.d("Music Album Name", songCur.getString(songCur.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                    //Log.d("Music Duration", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                    mDataSet.add(new Song(
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

    public void updateAlbumDataset(ArrayList<Album> mDataset) {
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
                    // Log.d("Column 0", String.valueOf(albumCur.getLong(0)));
                    // Log.d("Column 1", String.valueOf(albumCur.getString(1)));
                    // Log.d("Column 2", String.valueOf(albumCur.getString(2)));
                    // Log.d("Column 3", String.valueOf(albumCur.getLong(3)));
                    // Log.d("Column 4", String.valueOf(albumCur.getInt(4)));
                    // Log.d("Column 5", String.valueOf(albumCur.getInt(5)));
                    Album newAlbum = new Album(
                            albumCur.getLong(0),
                            albumCur.getString(1),
                            albumCur.getString(2),
                            albumCur.getLong(3),
                            albumCur.getInt(4),
                            albumCur.getInt(5),
                            albumCur.getString(6));

                    // Perform thorough null checks for strings
                    if (newAlbum.getTitle() == null) {
                        newAlbum.setTitle(""); // Don't make it null, make it blank
                        // Here's the reinforced reason
                        // http://stackoverflow.com/questions/4802015/difference-between-null-and-empty-java-string
                    }

                    if (newAlbum.getArtistName() == null) {
                        newAlbum.setTitle("");
                    }

                    if (!mDataset.contains(newAlbum))
                        mDataset.add(newAlbum);
                }
            }

            albumCur.close();
        }
    }

    public void updatePlaylistDataset(ArrayList<Playlist> _mDataset) {
        _mDataset.clear(); // Make sure we reset it first before we re-initialize to look for new playlists

        /**
         * Media Data Initialization Phase
         */
        // Get Content Dynamically
        Uri playlistUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String playlistSortOrder = MediaStore.Audio.Playlists.NAME + " ASC";
        Cursor playlistCur = cr.query(playlistUri,
                new String[]{"_id", "name", "date_added"},
                null,
                null,
                playlistSortOrder);

        int playlistCount = 0;

        if (playlistCur != null) {
            playlistCount = playlistCur.getCount();

            if (playlistCount > 0) {
                while (playlistCur.moveToNext()) {
                    // Debug
                    //Log.d("Column 0", String.valueOf(playlistCur.getLong(0)));
                    //Log.d("Column 1", String.valueOf(playlistCur.getString(1)));

                    _mDataset.add(new Playlist(
                            playlistCur.getLong(0),
                            playlistCur.getString(1),
                            playlistCur.getString(2)));
                }
            }

            playlistCur.close();
        }
    }

    private boolean checkDupeAndAdd(Album album, ArrayList<Album> mDataset) {
        Log.d("findDuplicateAlbum", "Running");

        try {
            if (mDataset != null && !mDataset.isEmpty()) {

                for (Album a : mDataset) {
                    if (a.getArtistName().equals(album.getArtistName()) &&
                            a.getTitle().equals(album.getTitle())) { // If we really find a dupe
//                        for (Song s : getSongDataset()) { // Set all the existing songs
//                            if (s.getAlbumId() == album.getId()) { // To the existing album
//                                s.setAlbumId(a.getId());
//                                s.setAlbumName(a.getTitle());
//                            }
//                        }

                        // As of now, we won't be doing anything to the album

                        return true; // Then return true
                    } else {
                        // Since no dupes are found
                        mDataset.add(album);

                        //Log.d("findDuplicateAlbum", "albumFiles is either null or is empty");
                        return false;
                    }
                }
            }
        } catch (Exception ex) {
            Log.d("findDuplicateAlbum", "An error occured");
            ex.printStackTrace();
            return false;
        }

        return false;
    }

    @Override
    public void run() {

    }

}
