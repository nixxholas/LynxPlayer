package com.nixholas.materialtunes.Fragments.FragmentItem;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.nixholas.materialtunes.Media.Adapter.AlbumAdapter;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.PaletteBitmap.PaletteBitmap;
import com.nixholas.materialtunes.Utils.PaletteBitmap.PaletteBitmapTranscoder;
import com.nixholas.materialtunes.Utils.PreferencesExample;

import java.io.File;

import static com.nixholas.materialtunes.MainActivity.getInstance;
import static com.nixholas.materialtunes.MainActivity.mediaManager;

/**
 * Created by nixholas on 21/12/16.
 */

public class AlbumActivity extends AppCompatActivity {
    // Recycler View and it's components
    RecyclerView recyclerView;
    RecyclerView.Adapter rVAdapter;
    ImageView albumArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumsfrag_expandedview); // Set the content view of course.

        // http://stackoverflow.com/questions/10602335/passing-extra-data-from-an-activity-to-an-intent
        long albumId = getIntent().getExtras().getLong("albumId");
        String albumArtUri = getIntent().getExtras().getString("albumArtUri");

        // Setup the static elements
        albumArt = (ImageView) findViewById(R.id.albumexpanded_image);

        // http://stackoverflow.com/questions/35997439/palette-using-with-glide-sometimes-fail-to-load-dark-vibrant-color
        Glide.with(this)
                .fromUri()
                .asBitmap()
                .transcode(new PaletteBitmapTranscoder(this), PaletteBitmap.class)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .fitCenter().load(Uri.fromFile(new File(albumArtUri)))
                .into(new ImageViewTarget<PaletteBitmap>(albumArt) {
                    @Override
                    protected void setResource(PaletteBitmap resource) {
                        albumArt.setImageBitmap(resource.getBitmap());
                        Palette.Swatch swatch = resource.palette.getVibrantSwatch();
                        if (swatch != null) {
                            int color = swatch.getRgb();
                            holder.currentAlbumCard.setBackgroundColor(color);
                            int textColor = PreferencesExample.getBlackWhiteColor(swatch.getTitleTextColor());
                            holder.title.setTextColor(textColor);
                            holder.artistName.setTextColor(textColor);
                        } else {
                            Palette.Swatch mutedSwatch = resource.palette.getMutedSwatch();
                            if (mutedSwatch != null) {
                                int color = mutedSwatch.getRgb();
                                holder.currentAlbumCard.setBackgroundColor(color);
                                int textColor = PreferencesExample.getBlackWhiteColor(mutedSwatch.getTitleTextColor());
                                holder.title.setTextColor(textColor);
                                holder.artistName.setTextColor(textColor);
                            }
                        }
                    }
                });

        recyclerView = (RecyclerView) findViewById(R.id.albumexpanded_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rVAdapter = new AlbumAdapter(mediaManager.getAlbumSongs(albumId));
        recyclerView.setAdapter(rVAdapter);
    }
}
