package com.nixholas.lynx.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nixholas.lynx.adapters.AlbumsAdapter;
import com.nixholas.lynx.R;

import static com.nixholas.lynx.ui.activities.MainActivity.mediaManager;

/**
 * Created by nixho on 03-Nov-16.
 */

public class AlbumFragment extends Fragment {
    public AlbumsAdapter rVAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager rVLayoutManager;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String FRAGMENT_NAME = "Albums";

    public AlbumFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AlbumFragment newInstance(int sectionNumber) {
        AlbumFragment fragment = new AlbumFragment();
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
        //Log.d("AlbumFragment ", "Called");
        rVLayoutManager = new GridLayoutManager(getActivity(), 2);

        // Use a linear layout manager
        recyclerView.setLayoutManager(rVLayoutManager);

        rVAdapter = new AlbumsAdapter(mediaManager);
        recyclerView.setAdapter(rVAdapter);

        return rootView;
    }
}
