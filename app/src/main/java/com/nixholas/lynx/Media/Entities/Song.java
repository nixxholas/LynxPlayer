package com.nixholas.lynx.Media.Entities;

/**
 * Created by nixho on 03-Nov-16.
 *
 * id, albumId, artistId, title, artist, album, duration, trackNumber
 */

public class Song {
    private String dataPath;
    private long albumId;
    private String albumName;
    private long artistId;
    private String artistName;
    private int duration;
    private long id;
    private String title;
    private long count;

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
        this.count = 0;
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
        this.count = 0;
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

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
