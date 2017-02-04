package com.nixholas.materialtunes.Fragments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.nixholas.materialtunes.Media.Adapters.TopPlayedAdapter;
import com.nixholas.materialtunes.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixholas on 29/1/17.
 */

public class MostPlayedActivity extends AppCompatActivity {
    FastScrollRecyclerView recyclerView;
    TopPlayedAdapter rVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostplayed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mostplayed_toolbar);
        setSupportActionBar(toolbar);

        // Set Some Details?
        toolbar.setTitle("Most Played");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // What to do when the back button is clicked
                supportFinishAfterTransition(); // http://stackoverflow.com/questions/26600239/animate-imageview-between-two-activities-using-shared-element-transitions-with-c
            }
        });

        // Enable the Back button on the Toolbar
        // http://stackoverflow.com/questions/26651602/display-back-arrow-on-toolbar-android
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (FastScrollRecyclerView) findViewById(R.id.mostplayed_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mediaManager.updateTopPlayed();
        rVAdapter = new TopPlayedAdapter(mediaManager.topPlayed);
        recyclerView.setAdapter(rVAdapter);
    }
}
