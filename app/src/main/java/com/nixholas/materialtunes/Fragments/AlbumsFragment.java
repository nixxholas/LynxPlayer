package com.nixholas.materialtunes.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nixholas.materialtunes.Media.Adapter.AlbumsAdapter;
import com.nixholas.materialtunes.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixho on 03-Nov-16.
 */

public class AlbumsFragment extends Fragment {
    @BindView(R.id.albums_RecyclerView) RecyclerView recyclerView;
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
        View rootView = inflater.inflate(R.layout.fragment_albums, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.albums_RecyclerView);
        ButterKnife.bind(this, rootView);

        /**
         * User Interface Initialization
         */
        rVLayoutManager = new LinearLayoutManager(recyclerView.getContext());

        // Use a linear layout manager
        recyclerView.setLayoutManager(rVLayoutManager);

        rVAdapter = new AlbumsAdapter(mediaManager.songFiles);

        return rootView;
    }
}
