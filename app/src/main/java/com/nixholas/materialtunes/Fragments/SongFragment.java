package com.nixholas.materialtunes.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nixholas.materialtunes.Fragments.Adapters.SongAdapter;
import com.nixholas.materialtunes.R;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixho on 03-Nov-16.
 */

public class SongFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerView.Adapter rVAdapter;
    RecyclerView.LayoutManager rVLayoutManager;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String FRAGMENT_NAME = "Songs";

    public SongFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SongFragment newInstance(int sectionNumber) {
        SongFragment fragment = new SongFragment();
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
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());

        /**
         * User Interface Initialization
         */
        rVLayoutManager = new LinearLayoutManager(getActivity());

        // use a linear layout manager
        recyclerView.setLayoutManager(rVLayoutManager);

        rVAdapter = new SongAdapter(mediaManager.getSongFiles());
        recyclerView.setAdapter(rVAdapter);

        return rootView;
    }

}
