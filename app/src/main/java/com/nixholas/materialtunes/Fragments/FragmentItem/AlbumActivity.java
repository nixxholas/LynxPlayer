package com.nixholas.materialtunes.Fragments.FragmentItem;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.nixholas.materialtunes.Fragments.Adapters.AlbumAdapter;
import com.nixholas.materialtunes.R;
import com.nixholas.materialtunes.Utils.Color.TextColorHelper;
import com.nixholas.materialtunes.Utils.PaletteBitmap.PaletteBitmap;
import com.nixholas.materialtunes.Utils.PaletteBitmap.PaletteBitmapTranscoder;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;

import static com.nixholas.materialtunes.MainActivity.mediaManager;

public class AlbumActivity extends AppCompatActivity {
    // Recycler View and it's components
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton floatingActionButton;
    FastScrollRecyclerView recyclerView;
    FastScrollRecyclerView.Adapter rVAdapter;
    NestedScrollView nestedScrollView;
    RelativeLayout relativeLayout;
    ImageView albumArt;
    TextView title, artist;
    int color, textColor, darkColor, darkTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumsfrag_expandedview); // Set the content view of course.

        postponeEnterTransition();

        // http://stackoverflow.com/questions/10602335/passing-extra-data-from-an-activity-to-an-intent
        long albumId = getIntent().getExtras().getLong("albumId");
        String albumArtUri = getIntent().getExtras().getString("albumArtUri");
        String albumName = getIntent().getExtras().getString("albumName");
        String albumArtist = getIntent().getExtras().getString("albumArtist");

        nestedScrollView = (NestedScrollView) findViewById(R.id.albumexpanded_nestedscroll);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.albumexpanded_fab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.albumexpanded_toolbar);
        setSupportActionBar(toolbar);

        // Enable the Back button on the Toolbar
        // http://stackoverflow.com/questions/26651602/display-back-arrow-on-toolbar-android
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set Some Details?
        toolbar.setTitle(albumName);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // What to do when the back button is clicked
                supportFinishAfterTransition(); // http://stackoverflow.com/questions/26600239/animate-imageview-between-two-activities-using-shared-element-transitions-with-c
            }
        });

        // Setup the static elements
        albumArt = (ImageView) findViewById(R.id.albumexpanded_image);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.albumexpanded_collapsetoolbar);
        relativeLayout = (RelativeLayout) findViewById(R.id.albumexpanded_staticinfo);
        title = (TextView) findViewById(R.id.albumexpanded_title);
        artist = (TextView) findViewById(R.id.albumexpanded_artist);

        title.setText(albumName);
        artist.setText(albumArtist);

        if (albumArtUri != null) {
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
                            Palette.Swatch darkSwatch = resource.palette.getDarkVibrantSwatch();
                            if (swatch != null && darkSwatch != null) {
                                color = swatch.getRgb();
                                darkColor = darkSwatch.getRgb();
                                collapsingToolbarLayout.setBackgroundColor(color);
                                nestedScrollView.setBackgroundColor(color);
                                relativeLayout.setBackgroundColor(darkSwatch.getRgb());
                                textColor = TextColorHelper.getBlackWhiteColor(swatch.getTitleTextColor());
                                collapsingToolbarLayout.setExpandedTitleColor(textColor);
                                collapsingToolbarLayout.setCollapsedTitleTextColor(textColor);
                                title.setTextColor(swatch.getTitleTextColor());
                                artist.setTextColor(swatch.getBodyTextColor());
                            } else {
                                Palette.Swatch mutedSwatch = resource.palette.getMutedSwatch();
                                Palette.Swatch darkMutedSwatch = resource.palette.getDarkMutedSwatch();
                                if (mutedSwatch != null && darkMutedSwatch != null) {
                                    color = mutedSwatch.getRgb();
                                    collapsingToolbarLayout.setBackgroundColor(color);
                                    nestedScrollView.setBackgroundColor(color);
                                    relativeLayout.setBackgroundColor(darkMutedSwatch.getRgb());
                                    textColor = TextColorHelper.getBlackWhiteColor(mutedSwatch.getTitleTextColor());
                                    collapsingToolbarLayout.setExpandedTitleColor(textColor);
                                    collapsingToolbarLayout.setCollapsedTitleTextColor(textColor);
                                    title.setTextColor(mutedSwatch.getTitleTextColor());
                                    artist.setTextColor(mutedSwatch.getBodyTextColor());
                                }
                            }
                        }
                    });
        } else {
            // https://github.com/bumptech/glide/issues/588
            Glide.with(this)
                    .fromResource()
                    .asBitmap()
                    .transcode(new PaletteBitmapTranscoder(this), PaletteBitmap.class)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .fitCenter()
                    .load(R.drawable.untitled_album)
                    .into(new ImageViewTarget<PaletteBitmap>(albumArt) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            albumArt.setImageBitmap(resource.getBitmap());
                            Palette.Swatch swatch = resource.palette.getVibrantSwatch();
                            Palette.Swatch darkSwatch = resource.palette.getDarkVibrantSwatch();
                            if (swatch != null && darkSwatch != null) {
                                color = swatch.getRgb();
                                collapsingToolbarLayout.setBackgroundColor(color);
                                nestedScrollView.setBackgroundColor(color);
                                relativeLayout.setBackgroundColor(darkSwatch.getRgb());
                                textColor = TextColorHelper.getBlackWhiteColor(swatch.getTitleTextColor());
                                collapsingToolbarLayout.setExpandedTitleColor(textColor);
                                collapsingToolbarLayout.setCollapsedTitleTextColor(textColor);
                                title.setTextColor(textColor);
                                artist.setTextColor(textColor);
                            } else {
                                Palette.Swatch mutedSwatch = resource.palette.getMutedSwatch();
                                Palette.Swatch darkMutedSwatch = resource.palette.getDarkMutedSwatch();
                                if (mutedSwatch != null && darkMutedSwatch != null) {
                                    color = mutedSwatch.getRgb();
                                    collapsingToolbarLayout.setBackgroundColor(color);
                                    nestedScrollView.setBackgroundColor(color);
                                    relativeLayout.setBackgroundColor(darkMutedSwatch.getRgb());
                                    textColor = TextColorHelper.getBlackWhiteColor(mutedSwatch.getTitleTextColor());
                                    collapsingToolbarLayout.setExpandedTitleColor(textColor);
                                    collapsingToolbarLayout.setCollapsedTitleTextColor(textColor);
                                    title.setTextColor(textColor);
                                    artist.setTextColor(textColor);
                                }
                            }
                        }
                    });
        }

        startPostponedEnterTransition();

        recyclerView = (FastScrollRecyclerView) findViewById(R.id.albumexpanded_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rVAdapter = new AlbumAdapter(mediaManager.getAlbumSongs(albumId), color, textColor);
        recyclerView.setAdapter(rVAdapter);
    }


    /**
     * Schedules the shared element transition to be started immediately
     * after the shared element has been measured and laid out within the
     * activity's view hierarchy. Some common places where it might make
     * sense to call this method are:
     * <p>
     * (1) Inside a Fragment's onCreateView() method (if the shared element
     * lives inside a Fragment hosted by the called Activity).
     * <p>
     * (2) Inside a Picasso Callback object (if you need to wait for Picasso to
     * asynchronously load/scale a bitmap before the transition can begin).
     * <p>
     * (3) Inside a LoaderCallback's onLoadFinished() method (if the shared
     * element depends on data queried by a Loader).
     */
    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
    }
}
