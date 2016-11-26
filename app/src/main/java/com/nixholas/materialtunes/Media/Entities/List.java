package com.nixholas.materialtunes.Media.Entities;

import java.util.ArrayList;

/**
 * Created by nixho on 26-Nov-16.
 */

public class List {
    private String ListName;
    private ArrayList<Song> songs;

    public List(String listName, ArrayList<Song> songs) {
        ListName = listName;
        this.songs = songs;
    }

    public String getListName() {
        return ListName;
    }

    public void setListName(String listName) {
        ListName = listName;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }
}
