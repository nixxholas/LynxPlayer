package com.nixholas.materialtunes.Fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nixholas.materialtunes.Media.Adapter.SongsAdapter;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.Preferences;

import java.io.File;
import java.io.FileFilter;

import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixho on 03-Nov-16.
 */

public class SongsFragment extends Fragment {
    public File mainDirectory;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter rVAdapter;
    private RecyclerView.LayoutManager rVLayoutManager;
    private Preferences mPreference;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String FRAGMENT_NAME = "Songs";
    private static final int PERM_REQUEST_APP_CORE_PERMISSIONS = 133;

    public SongsFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SongsFragment newInstance(int sectionNumber) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_NAME, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Ensure we have READ_EXTERNAL_STORAGE for Music database in LocalProvider
        // Ensure we have WRITE_EXTERNAL_STORAGE for Album arts storage
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERM_REQUEST_APP_CORE_PERMISSIONS);
        }


        /**
         * Media Data Initialization Phase
         */

        // Get Content Dynamically
        ContentResolver cr = getActivity().getContentResolver();

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
                    //String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));

                    // Debug
                    //Log.e("Song Count", data);

                    // (long _id, long _albumId, long _artistId, String _title,
                    // String _artistName, String _albumName, int _duration)
                    /*Log.e("Music ID", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID)));
                    Log.e("Music Album ID", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                    Log.e("Music Artist ID", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
                    Log.e("Music Title", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    Log.e("Music Artist Name", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                    Log.e("Music Album Name", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                    Log.e("Music Duration", songCur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));*/

                    mediaManager.songFiles.add(new Song(
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

        /**
         * User Interface Initialization
         */

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.main_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // use a linear layout manager
        rVLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(rVLayoutManager);

        rVAdapter = new SongsAdapter(mediaManager.songFiles);
        recyclerView.setAdapter(rVAdapter);

        return rootView;
    }

}
