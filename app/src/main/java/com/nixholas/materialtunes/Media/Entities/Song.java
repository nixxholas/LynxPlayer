package com.nixholas.materialtunes.Media.Entities;

/**
 * Created by nixho on 03-Nov-16.
 *
 * id, albumId, artistId, title, artist, album, duration, trackNumber
 */

public class Song {
    private final long albumId;
    private final String albumName;
    private final long artistId;
    private final String artistName;
    private final int duration;
    private final long id;
    private final String title;
    private final int trackNumber;

    /**
     * Default Constructor
     */
    public Song() {
        this.id = -1;
        this.albumId = -1;
        this.artistId = -1;
        this.title = "";
        this.artistName = "";
        this.albumName = "";
        this.duration = -1;
        this.trackNumber = -1;
    }

    public Song(long _id, long _albumId, long _artistId, String _title, String _artistName, String _albumName, int _duration, int _trackNumber) {
        this.id = _id;
        this.albumId = _albumId;
        this.artistId = _artistId;
        this.title = _title;
        this.artistName = _artistName;
        this.albumName = _albumName;
        this.duration = _duration;
        this.trackNumber = _trackNumber;
    }
}
