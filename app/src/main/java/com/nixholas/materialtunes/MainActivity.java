package com.nixholas.materialtunes;

import android.content.ContentUris;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nixholas.materialtunes.Fragments.AlbumsFragment;
import com.nixholas.materialtunes.Fragments.ListsFragment;
import com.nixholas.materialtunes.Fragments.SongsFragment;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.Media.MediaManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    // Protected Entities
    // Sliding Up Bar
    @BindView(R.id.slide_albumart) ImageView slideAlbumArt;
    @BindView(R.id.slide_songartist) TextView slideSongArtist;
    @BindView(R.id.slide_songtitle) TextView slideSongTitle;
    @BindView(R.id.slide_button) ImageButton slideButton;
    @BindView(R.id.slide_layout) RelativeLayout slideRelativeLayout;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingUpPanelLayout;

    // Expanded View of Sliding Up Bar
    @BindView(R.id.slided_image) ImageView slidedAlbumArt;
    @BindView(R.id.slided_layout) LinearLayout slidedLinearLayout;
    @BindView(R.id.media_controls_playpause) ImageButton mediaControls_PlayPause;
    @BindView(R.id.media_controls_previous) ImageButton mediaControls_Previous;
    @BindView(R.id.media_controls_next) ImageButton mediaControls_Next;
    @BindView(R.id.media_controls_shuffle) ImageButton mediaControls_Shuffle;
    @BindView(R.id.media_controls_repeat) ImageButton mediaControls_Repeat;

    // Publicly Accessible Entities
    public static MediaManager mediaManager = new MediaManager();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        slidedLinearLayout.setAlpha(0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // http://stackoverflow.com/questions/10529226/notify-once-the-audio-is-finished-playing
        mediaManager.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                //Log.e("Completed", "Yep");

                try {
                    if (mediaManager.PlayState == MediaManager.MPPlayState.REPEATALL) {
                        Song nextSong = mediaManager.getNext();

                        mediaManager.mediaPlayer.reset();
                        mediaManager.mediaPlayer.setDataSource("file://" + nextSong.getDataPath());
                        mediaManager.mediaPlayer.prepareAsync();
                    } else if (mediaManager.PlayState != MediaManager.MPPlayState.NOREPEAT) {
                        // Update the UI
                        slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
                    } // No need to perform an else for REPEATONE

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i("PanelSlideListener", "onPanelSlide, offset " + slideOffset);

                /**
                 * http://stackoverflow.com/questions/5078041/how-can-i-make-an-image-transparent-in-android
                 *
                 * since slideOffset will always be 0 to 1,
                 *
                 * 255 is the new limit for the alpha.
                 *
                 * So slideOffset * 255 will be the inverse of what we need.
                 *
                 * So we'll do 255 - (slideOffset * 255) for the Unexpanded Views and the previous for the expanded views
                 */
                int unexpandedOffset = (int) (255 - (slideOffset * 255)); // For the Un expanded views
                int expandedOffset = (int) (slideOffset * 255); // For the expanded views

                // Set the Unexpanded Views first
                slideButton.setImageAlpha(unexpandedOffset);
                slideAlbumArt.setImageAlpha(unexpandedOffset);
                slideSongTitle.setAlpha(1 - slideOffset);
                slideSongArtist.setAlpha(1 - slideOffset);
                //slideRelativeLayout.setAlpha(1 - slideOffset); // This Results in synthetic disappearance.

                // Then the expanded views
                slidedLinearLayout.setAlpha(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                //Log.i("PanelSlideListener", "onPanelStateChanged " + newState);

                /*if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    // Set the Unexpanded Views first
                    // http://stackoverflow.com/questions/13397709/android-hide-imageview
                    *//*slideButton.setVisibility(View.GONE);
                    slideAlbumArt.setVisibility(View.GONE);
                    slideSongTitle.setVisibility(View.GONE);
                    slideSongArtist.setVisibility(View.GONE);
                    slideRelativeLayout.setVisibility(View.GONE);*//*

                    // Set the Expanded Layout
                    //slidedLinearLayout.setVisibility(View.VISIBLE);

                    // Then the expanded views
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    // Set the Unexpanded Views first
                    *//*slideButton.setVisibility(View.VISIBLE);
                    slideAlbumArt.setVisibility(View.VISIBLE);
                    slideSongTitle.setVisibility(View.VISIBLE);
                    slideSongArtist.setVisibility(View.VISIBLE);
                    slideRelativeLayout.setVisibility(View.VISIBLE);*//*

                    // Then the expanded views
                    //slidedLinearLayout.setVisibility(View.GONE);

                } else {
                    // Do Nothing
                }*/
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    return SongsFragment.newInstance(position);
                case 1:
                    return AlbumsFragment.newInstance(position);
                case 2:
                    return ListsFragment.newInstance(position);
                default:
                    return ListsFragment.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Songs";
                case 1:
                    return "Albums";
                case 2:
                    return "Lists";
            }
            return null;
        }
    }

    /**
     * The two buttons below must have the same binded views at the same time.
     * @param v
     */
    @OnClick(R.id.slide_button)
    public void slideButtonOnClick(View v) {
        //Log.e("Slide Button", "Clicked");

        if (mediaManager.mediaPlayer.isPlaying() || mediaManager.mediaPlayerIsPaused) {
            // http://stackoverflow.com/questions/25381624/possible-to-detect-paused-state-of-mediaplayer
            if (mediaManager.mediaPlayerIsPaused) { // If the current song is paused,
                mediaManager.mediaPlayer.start();
                mediaManager.mediaPlayerIsPaused = false;
                //http://stackoverflow.com/questions/7024881/replace-one-image-with-another-after-clicking-a-button
                slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);

            } else { // Else we pause it
                mediaManager.mediaPlayer.pause();
                mediaManager.mediaPlayerIsPaused = true;
                slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            }
        }
    }

    @OnClick(R.id.media_controls_playpause)
    public void mediaControlsOnClickPlayPause(View v) {
        if (mediaManager.mediaPlayer.isPlaying() || mediaManager.mediaPlayerIsPaused) {
            // http://stackoverflow.com/questions/25381624/possible-to-detect-paused-state-of-mediaplayer
            if (mediaManager.mediaPlayerIsPaused) { // If the current song is paused,
                mediaManager.mediaPlayer.start();
                mediaManager.mediaPlayerIsPaused = false;
                //http://stackoverflow.com/questions/7024881/replace-one-image-with-another-after-clicking-a-button
                slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);

            } else { // Else we pause it
                mediaManager.mediaPlayer.pause();
                mediaManager.mediaPlayerIsPaused = true;
                slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            }
        }
    }

    @OnClick(R.id.media_controls_previous)
    public void mediaControlsOnClickPrevious(View v) {
        try {
            // This is a temporary fix, making it circular soon
            final Song prevSong = mediaManager.songFiles.get(mediaManager.currentlyPlayingIndex - 1);
            mediaManager.currentlyPlayingIndex -= 1;

            Uri audioUri = Uri.parse("file://" + prevSong.getDataPath());

            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, prevSong.getAlbumId());

            if (mediaManager.mediaPlayer.isPlaying() || mediaManager.mediaPlayerIsPaused) {
                mediaManager.mediaPlayer.stop();
                mediaManager.mediaPlayer.reset();
                mediaManager.mediaPlayer.setDataSource(v.getContext(), audioUri);
                mediaManager.mediaPlayer.prepareAsync();
                mediaManager.mediaPlayerIsPaused = false;

                /**
                 * User Interface Changes
                 */
                slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                slideSongTitle.setText(prevSong.getTitle());
                slideSongArtist.setText(prevSong.getArtistName());
                Glide.with(v.getContext()).load(albumArtUri).into(slideAlbumArt);
                Glide.with(v.getContext()).load(albumArtUri).into(slidedAlbumArt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.media_controls_next)
    public void mediaControlsOnClickNext(View v) {
        try {
            // This is a temporary fix, making it circular soon
            final Song nextSong = mediaManager.songFiles.get(mediaManager.currentlyPlayingIndex + 1);
            mediaManager.currentlyPlayingIndex += 1;

            Uri audioUri = Uri.parse("file://" + nextSong.getDataPath());

            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, nextSong.getAlbumId());

            if (mediaManager.mediaPlayer.isPlaying() || mediaManager.mediaPlayerIsPaused) {
                mediaManager.mediaPlayer.stop();
                mediaManager.mediaPlayer.reset();
                mediaManager.mediaPlayer.setDataSource(v.getContext(), audioUri);
                mediaManager.mediaPlayer.prepareAsync();
                mediaManager.mediaPlayerIsPaused = false;

                /**
                 * User Interface Changes
                 */
                slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
                slideSongTitle.setText(nextSong.getTitle());
                slideSongArtist.setText(nextSong.getArtistName());
                Glide.with(v.getContext()).load(albumArtUri).into(slideAlbumArt);
                Glide.with(v.getContext()).load(albumArtUri).into(slidedAlbumArt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.media_controls_repeat)
    public void mediaControlsOnClickRepeat (View v) {
        if (mediaManager.PlayState == MediaManager.MPPlayState.NOREPEAT) {
            // Next is repeat all..
            mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_white_24dp);
            mediaManager.PlayState = MediaManager.MPPlayState.REPEATALL;
            Log.e("PlayState", "Repeat All");
        } else if (mediaManager.PlayState == MediaManager.MPPlayState.REPEATALL) {
            // Next is repeat one only..
            //http://stackoverflow.com/questions/9461270/media-player-looping-android
            mediaManager.mediaPlayer.setLooping(true);
            mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_one_white_24dp);
            mediaManager.PlayState = MediaManager.MPPlayState.REPEATONE;
        } else {
            // Next is repeat nothing..
            mediaManager.mediaPlayer.setLooping(false);
            mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_white_24dp);
            mediaManager.PlayState = MediaManager.MPPlayState.NOREPEAT;
        }
    }
}
