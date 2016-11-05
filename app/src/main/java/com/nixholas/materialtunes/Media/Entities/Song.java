package com.nixholas.materialtunes.Media.Entities;

/**
 * Created by nixho on 03-Nov-16.
 *
 * id, albumId, artistId, title, artist, album, duration, trackNumber
 */

public class Song {
    private final String dataPath;
    private final long albumId;
    private final String albumName;
    private final long artistId;
    private final String artistName;
    private final int duration;
    private final long id;
    private final String title;

    /**
     * Default Constructor
     */
    public Song() {
        this.dataPath = "";
        this.id = -1;
        this.albumId = -1;
        this.artistId = -1;
        this.title = "";

        this.artistName = "";
        this.albumName = "";
        this.duration = -1;
    }

    public String getTitle() {
        return title;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getDataPath() {
        return dataPath;
    }

    public long getArtistId() {
        return artistId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public int getDuration() {
        return duration;
    }

    public long getId() {
        return id;
    }

    public Song(String _dataPath, long _id, long _albumId, long _artistId, String _title, String _artistName, String _albumName, int _duration) {
        this.dataPath = _dataPath;
        this.id = _id;
        this.albumId = _albumId;
        this.artistId = _artistId;
        this.title = _title;
        this.artistName = _artistName;
        this.albumName = _albumName;
        this.duration = _duration;
    }
}
