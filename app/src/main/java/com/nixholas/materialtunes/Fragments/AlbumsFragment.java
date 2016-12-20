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
        View rootView = inflater.inflate(R.layout.fragment_base, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.base_recyclerView);

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
