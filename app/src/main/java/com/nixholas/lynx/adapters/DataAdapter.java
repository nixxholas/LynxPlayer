package com.nixholas.lynx.adapters;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.nixholas.lynx.media.entities.Album;
import com.nixholas.lynx.media.entities.Playlist;
import com.nixholas.lynx.media.entities.Song;

import java.util.ArrayList;

import static com.nixholas.lynx.ui.activities.MainActivity.mediaManager;

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

    private ArrayList<Song> songs = new ArrayList<>();
    private ArrayList<Album> albums = new ArrayList<>();
    private ArrayList<Playlist> playlists = new ArrayList<>();
    private ContentResolver cr;

    public DataAdapter(ContentResolver cr_) {
        this.cr = cr_;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    private void loadSongData(ContentResolver cr) {
        songs.clear(); // Make sure we reset it first before we re-initialize to look for new audio files

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

                    mediaManager.getSongFiles().add(new Song(
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

    private void loadSongDataset(ContentResolver cr, ArrayList<Song> mDataSet) {
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

    private void loadAlbumData(ContentResolver cr) {
        albums.clear(); // Make sure we reset it first before we re-initialize to look for new albums

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

                    // Call the duplicate checker
                    mediaManager.findDuplicateAlbum(newAlbum);
                }
            }

            albumCur.close();
        }

    }

    private void loadPlaylistData(ContentResolver cr) {
        playlists.clear(); // Make sure we reset it first before we re-initialize to look for new playlists

        /**
         * Media Data Initialization Phase
         */
        // Get Content Dynamically
        Uri playlistUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String playlistsSortOrder = MediaStore.Audio.Playlists.NAME + " ASC";
        Cursor playlistCur = cr.query(playlistUri,
                new String[]{"_id", "name", "date_added"},
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

                    playlists.add(new Playlist(
                            playlistCur.getLong(0),
                            playlistCur.getString(1),
                            playlistCur.getString(2)));
                }
            }

            playlistCur.close();
        }
    }

    public void updateSongData() {
        loadSongData(cr);
    }

    public void updateAlbumData() {
        loadAlbumData(cr);
    }

    public void updatePlaylistData() {
        //Log.d("updatePlaylistData", "Running");
        loadPlaylistData(cr);
    }

    @Override
    public void run() {
        loadAlbumData(cr);
        //loadSongData(cr);
        loadPlaylistData(cr);

        // If the index is not proper,
        if (mediaManager.currentlyPlayingIndex < 0 ||
                !(mediaManager.currentlyPlayingIndex > mediaManager.getSongFiles().size())) {
            // Set it up because it means that there is a discrepancy
            mediaManager.setupLastPlayed();
        }
    }
}