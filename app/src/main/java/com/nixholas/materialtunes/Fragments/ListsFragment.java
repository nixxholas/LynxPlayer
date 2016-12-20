package com.nixholas.materialtunes.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nixholas.materialtunes.Media.Adapter.ListsAdapter;
import com.nixholas.materialtunes.Media.Adapter.SongsAdapter;
import com.nixholas.materialtunes.R;

import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixho on 03-Nov-16.
 */

public class ListsFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerView.Adapter rVAdapter;
    RecyclerView.LayoutManager rVLayoutManager;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String FRAGMENT_NAME = "Lists";

    public ListsFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ListsFragment newInstance(int sectionNumber) {
        ListsFragment fragment = new ListsFragment();
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
        rVLayoutManager = new LinearLayoutManager(getActivity());

        // use a linear layout manager
        recyclerView.setLayoutManager(rVLayoutManager);

        rVAdapter = new ListsAdapter(mediaManager.playLists);
        recyclerView.setAdapter(rVAdapter);

        return rootView;
    }
}
