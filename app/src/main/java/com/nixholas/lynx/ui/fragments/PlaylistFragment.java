package com.nixholas.lynx.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nixholas.lynx.adapters.PlaylistAdapter;
import com.nixholas.lynx.R;

import static com.nixholas.lynx.ui.activities.MainActivity.mediaManager;

/**
 * Created by nixho on 03-Nov-16.
 */

public class PlaylistFragment extends Fragment {
    CardView mostPlayedCardView;
    RecyclerView recyclerView;
    RecyclerView.Adapter rVAdapter;
    RecyclerView.LayoutManager rVLayoutManager;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String FRAGMENT_NAME = "Lists";

    public PlaylistFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaylistFragment newInstance(int sectionNumber) {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_NAME, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.listbase_recyclerView);
        mostPlayedCardView = (CardView) rootView.findViewById(R.id.mostplayed_cardview);

        /**
         * User Interface Initialization
         */
        rVLayoutManager = new LinearLayoutManager(getActivity());

        // use a linear layout manager
        recyclerView.setLayoutManager(rVLayoutManager);

        rVAdapter = new PlaylistAdapter(mediaManager);
        recyclerView.setAdapter(rVAdapter);

        mostPlayedCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We'll have to launch a fragment for the most played

                // Update the counts first
                mediaManager.updateAllPlayCount();

                // Launch the activity
                Intent intent = new Intent(v.getContext(), MostPlayedActivity.class);

                startActivity(intent);
            }
        });

        return rootView;
    }
}
