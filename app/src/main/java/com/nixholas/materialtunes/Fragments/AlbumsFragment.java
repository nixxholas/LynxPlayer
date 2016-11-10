package com.nixholas.materialtunes.Fragments;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nixholas.materialtunes.Media.Adapter.AlbumsAdapter;
import com.nixholas.materialtunes.Media.Entities.Album;
import com.nixholas.materialtunes.R;

import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixho on 03-Nov-16.
 */

public class AlbumsFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerView.Adapter rVAdapter;
    RecyclerView.LayoutManager rVLayoutManager;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String FRAGMENT_NAME = "Albums";

    public AlbumsFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AlbumsFragment newInstance(int sectionNumber) {
        AlbumsFragment fragment = new AlbumsFragment();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_NAME, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.main_RecyclerView);

        mediaManager.albumFiles.clear(); // Make sure we reset it first before we re-initialize to look for new albums


        /**
         * Media Data Initialization Phase
         */
        // Get Content Dynamically
        ContentResolver cr = getActivity().getContentResolver();

        Uri albumsUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String albumsSortOrder = MediaStore.Audio.Albums.ALBUM + " ASC";
        Cursor albumCur = cr.query(albumsUri,
                new String[]{"_id", "album", "artist", "artist_id", "numsongs", "minyear"},
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

                    mediaManager.albumFiles.add(new Album(
                            albumCur.getLong(0),
                            albumCur.getString(1),
                            albumCur.getString(2),
                            albumCur.getLong(3),
                            albumCur.getInt(4),
                            albumCur.getInt(5)
                    ));
                }
            }
        }

        /**
         * User Interface Initialization
         */
        //Log.e("AlbumsFragment ", "Called");
        rVLayoutManager = new GridLayoutManager(getActivity(), 2);

        // Use a linear layout manager
        recyclerView.setLayoutManager(rVLayoutManager);

        rVAdapter = new AlbumsAdapter(mediaManager.albumFiles);
        recyclerView.setAdapter(rVAdapter);

        return rootView;
    }
}
