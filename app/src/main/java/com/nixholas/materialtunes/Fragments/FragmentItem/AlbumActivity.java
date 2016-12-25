package com.nixholas.materialtunes.Fragments.FragmentItem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.nixholas.materialtunes.Media.Adapter.AlbumAdapter;
import com.nixholas.materialtunes.R;

import static com.nixholas.materialtunes.MainActivity.getInstance;
import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixholas on 21/12/16.
 */

public class AlbumActivity extends AppCompatActivity {
    // Recycler View and it's components
    ObservableRecyclerView recyclerView;
    ObservableRecyclerView.Adapter rVAdapter;
    ObservableRecyclerView.LayoutManager rVLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // http://stackoverflow.com/questions/10602335/passing-extra-data-from-an-activity-to-an-intent
        long albumId = getIntent().getExtras().getLong("albumId");

        recyclerView = (ObservableRecyclerView) findViewById(R.id.albumexpanded_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rVAdapter = new AlbumAdapter(mediaManager.getAlbumSongs(albumId));
        recyclerView.setAdapter(rVAdapter);

        setContentView(R.layout.albumsfrag_expandedview); // Set the content view of course.
    }
}
