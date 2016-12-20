package com.nixholas.materialtunes.Media.Entities;

import java.util.ArrayList;

/**
 * Created by nixho on 26-Nov-16.
 */

public class Playlist {
    private long PlaylistId;
    private String PlaylistName;
    private String PlaylistDateAdded;
    private ArrayList<Song> Songs;

    public Playlist(long playlistId, String playlistName, String playlistDateAdded) {
        PlaylistId = playlistId;
        PlaylistName = playlistName;
        PlaylistDateAdded = playlistDateAdded;
        Songs = new ArrayList<>();
    }

    public long getPlaylistId() {
        return PlaylistId;
    }

    public void setPlaylistId(long playlistId) {
        PlaylistId = playlistId;
    }

    public String getPlaylistName() {
        return PlaylistName;
    }

    public void setPlaylistName(String playlistName) {
        PlaylistName = playlistName;
    }

    public ArrayList<Song> getSongs() {
        return Songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        Songs = songs;
    }

    public String getPlaylistDateAdded() {
        return PlaylistDateAdded;
    }

    public void setPlaylistDateAdded(String playlistDateAdded) {
        PlaylistDateAdded = playlistDateAdded;
    }
}
