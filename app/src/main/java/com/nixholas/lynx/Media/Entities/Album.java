package com.nixholas.lynx.Media.Entities;

/**
 * Created by nixho on 03-Nov-16.
 */

public class Album {
    private long id;
    private String title;
    private String artistName;
    private long artistId;
    private int songCount;
    private int year;
    private String albumArtPath;
    private boolean hasAlbumArt;

    public Album() {
        this.id = -1;
        this.title = "";
        this.artistName = "";
        this.artistId = -1;
        this.songCount = -1;
        this.year = -1;
        this.albumArtPath = "";
    }

    public Album(long _id, String _title, String _artistName, long _artistId, int _songCount, int _year, String _albumArtPath) {
        this.id = _id;
        this.title = _title;
        this.artistName = _artistName;
        this.artistId = _artistId;
        this.songCount = _songCount;
        this.year = _year;
        this.albumArtPath = _albumArtPath;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setHasAlbumArt(boolean hasAlbumArt) {
        this.hasAlbumArt = hasAlbumArt;
    }

    public long getId() {
        return id;
    }

    public boolean hasAlbumArt() {
        return hasAlbumArt;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumArtPath() {
        return albumArtPath;
    }

    public void setAlbumArtPath(String albumArtPath) {
        this.albumArtPath = albumArtPath;
    }

    public long getArtistId() {
        return artistId;
    }

    public int getSongCount() {
        return songCount;
    }

    public int getYear() {
        return year;
    }

}
