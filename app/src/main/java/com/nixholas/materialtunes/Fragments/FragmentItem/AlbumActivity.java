package com.nixholas.materialtunes.Fragments.FragmentItem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.nixholas.materialtunes.Media.Adapter.AlbumAdapter;
import com.nixholas.materialtunes.R;

/**
 * Created by nixholas on 21/12/16.
 */

public class AlbumActivity extends AppCompatActivity {
    // Recycler View and it's components
    RecyclerView recyclerView;
    RecyclerView.Adapter rVAdapter;
    RecyclerView.LayoutManager rVLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recyclerView = (RecyclerView) findViewById(R.id.albumexpanded_recycler);

        rVLayoutManager = new LinearLayoutManager(getApplicationContext());
        rVAdapter = new AlbumAdapter();

        recyclerView.setAdapter(rVAdapter);

        setContentView(R.layout.albumsfrag_expandedview); // Set the content view of course.
    }
}
