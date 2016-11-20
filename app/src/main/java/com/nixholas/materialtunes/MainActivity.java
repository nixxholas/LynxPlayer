package com.nixholas.materialtunes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nixholas.materialtunes.Fragments.AlbumsFragment;
import com.nixholas.materialtunes.Fragments.ListsFragment;
import com.nixholas.materialtunes.Fragments.SongsFragment;
import com.nixholas.materialtunes.Media.Adapter.DataAdapter;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.Media.MediaManager;
import com.nixholas.materialtunes.Notification.PersistentNotif;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Android Security TTN (To Take Note) Of
 *
 * https://securityintelligence.com/new-vulnerability-android-framework-fragment-injection/
 *
 *
 */
public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener{
    // Protected Entities
    // Sliding Up Bar
    ImageView slideAlbumArt;
    TextView slideSongArtist;
    TextView slideSongTitle;
    ImageButton slideButton;
    RelativeLayout slideRelativeLayout;
    SlidingUpPanelLayout slidingUpPanelLayout;

    // Expanded View of Sliding Up Bar
    ImageView slidedAlbumArt;
    LinearLayout slidedLinearLayout;
    ImageButton mediaControls_PlayPause;
    ImageButton mediaControls_Previous;
    ImageButton mediaControls_Next;
    ImageButton mediaControls_Shuffle;
    ImageButton mediaControls_Repeat;

    // Publicly Accessible Entities
    public static MediaManager mediaManager = new MediaManager();
    DataAdapter mDataAdapter;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    private static final int PERM_REQUEST_APP_CORE_PERMISSIONS = 133;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Request for proper permissions first
        // https://developer.android.com/training/permissions/requesting.html
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1); // Reading
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                2); // Writing

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slidedLinearLayout = (LinearLayout) findViewById(R.id.slided_layout);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        // Sliding Up Bar
        slideAlbumArt = (ImageView) findViewById(R.id.slide_albumart);
        slideSongArtist = (TextView) findViewById(R.id.slide_songartist);
        slideSongTitle = (TextView) findViewById(R.id.slide_songtitle);
        slideButton = (ImageButton) findViewById(R.id.slide_button);
        slideRelativeLayout = (RelativeLayout) findViewById(R.id.slide_layout);
        mDataAdapter = new DataAdapter(getContentResolver());

        // Expanded View of Sliding Up Bar
        slidedAlbumArt = (ImageView) findViewById(R.id.slided_image);
        mediaControls_PlayPause = (ImageButton) findViewById(R.id.media_controls_playpause);
        mediaControls_Previous = (ImageButton) findViewById(R.id.media_controls_previous);
        mediaControls_Next = (ImageButton) findViewById(R.id.media_controls_next);
        mediaControls_Shuffle = (ImageButton) findViewById(R.id.media_controls_shuffle);
        mediaControls_Repeat = (ImageButton) findViewById(R.id.media_controls_repeat);

        // Setup the notifications
        /*NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        initializeNotification();*/

        Handler mHandler = new Handler();
        Context appContext = getBaseContext().getApplicationContext();
        mHandler.post(new PersistentNotif(appContext));

        mDataAdapter.run();

        mediaManager.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaManager.mediaPlayer.setOnPreparedListener(this);

        // Ensure we have READ_EXTERNAL_STORAGE for Music database in LocalProvider
        // Ensure we have WRITE_EXTERNAL_STORAGE for Album arts storage
        /*if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getParent(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERM_REQUEST_APP_CORE_PERMISSIONS);
        }*/

        slidedLinearLayout.setAlpha(0);

        // Hide the panel first, since nothing is being played
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

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
                        /**
                         * Under the hood changes
                         */
                        Song nextSong = mediaManager.getNext(); // Call and get the next song with shuffling check
                        Uri sArtworkUri = Uri
                                .parse("content://media/external/audio/albumart");
                        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, nextSong.getAlbumId());

                        mediaManager.mediaPlayer.reset(); // Reset the mediaPlayer first
                        mediaManager.mediaPlayer.setDataSource("file://" + nextSong.getDataPath()); // Set the path via the next song
                        mediaManager.mediaPlayer.prepareAsync(); // prepare and play

                        /**
                         * User Interface Changes
                         */
                        slideSongTitle.setText(nextSong.getTitle());
                        slideSongArtist.setText(nextSong.getArtistName());
                        Glide.with(getApplicationContext()).load(albumArtUri).placeholder(R.drawable.untitled_album).into(slideAlbumArt);
                        Glide.with(getApplicationContext()).load(albumArtUri).placeholder(R.drawable.untitled_album).into(slidedAlbumArt);
                    } else if (mediaManager.PlayState != MediaManager.MPPlayState.NOREPEAT) {
                        /**
                         * Under The Hood changes
                         */
                        Song currentSong = mediaManager.songFiles.get(mediaManager.currentlyPlayingIndex); // Get the current song that just ended
                        mediaManager.mediaPlayer.reset(); // Reset the player first
                        Uri audioUri = Uri.parse("file://" + currentSong.getDataPath()); // Get the path of the song
                        mediaManager.mediaPlayer.setDataSource(getApplicationContext(), audioUri); // Set it again

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
                //int expandedOffset = (int) (slideOffset * 255); // For the expanded views

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

    // http://programmerguru.com/android-tutorial/how-to-change-the-back-button-behaviour/
    @Override
    public void onBackPressed() {
        //Include the code here
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        // Check to make sure it's not hidden
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        mediaPlayer.start();
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
                    //Log.e("AlbumFragment", "SectionsPagerAdapter/getItem Call");
                    return AlbumsFragment.newInstance(position);
                case 2:
                    return ListsFragment.newInstance(position);
                default:
                    return SongsFragment.newInstance(position);
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

    public void mediaControlsOnClickPrevious(View v) {
        try {
            final Song prevSong = mediaManager.getPrevious();

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
                // http://stackoverflow.com/questions/40452192/performing-album-art-checks-on-an-audio-file
                Glide.with(v.getContext())
                        .load(albumArtUri)
                        .asBitmap()
                        .placeholder(R.drawable.untitled_album)
                        .listener(new RequestListener<Uri, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                // Retrieve the palette of colors from the Bitmap first
                                Palette p = Palette.from(resource).generate();
                                Palette.Swatch swatch = p.getVibrantSwatch();
                                if (swatch != null) {
                                    int color = swatch.getRgb();
                                    slidedLinearLayout.setBackgroundColor(color);
                                } else {
                                    Palette.Swatch mutedSwatch = p.getMutedSwatch();
                                    if (mutedSwatch != null) {
                                        int color = mutedSwatch.getRgb();
                                        slidedLinearLayout.setBackgroundColor(color);
                                    }
                                }

                                // Set the images
                                slideAlbumArt.setImageURI(model);
                                slidedAlbumArt.setImageURI(model);

                                return true;
                            }
                        })
                        .into(slideAlbumArt);

                Glide.with(v.getContext())
                        .load(albumArtUri)
                        .asBitmap()
                        .placeholder(R.drawable.untitled_album)
                        .listener(new RequestListener<Uri, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                // Retrieve the palette of colors from the Bitmap first
                                Palette p = Palette.from(resource).generate();
                                Palette.Swatch swatch = p.getVibrantSwatch();
                                if (swatch != null) {
                                    int color = swatch.getRgb();
                                    slidedLinearLayout.setBackgroundColor(color);
                                } else {
                                    Palette.Swatch mutedSwatch = p.getMutedSwatch();
                                    if (mutedSwatch != null) {
                                        int color = mutedSwatch.getRgb();
                                        slidedLinearLayout.setBackgroundColor(color);
                                    }
                                }

                                // Set the images
                                slideAlbumArt.setImageURI(model);
                                slidedAlbumArt.setImageURI(model);

                                return true;
                            }
                        })
                        .into(slidedAlbumArt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mediaControlsOnClickNext(View v) {
        try {
            final Song nextSong = mediaManager.getNext();

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
                Glide.with(v.getContext())
                        .load(albumArtUri)
                        .asBitmap()
                        .placeholder(R.drawable.untitled_album)
                        .listener(new RequestListener<Uri, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                // Retrieve the palette of colors from the Bitmap first
                                Palette p = Palette.from(resource).generate();
                                Palette.Swatch swatch = p.getVibrantSwatch();
                                if (swatch != null) {
                                    int color = swatch.getRgb();
                                    slidedLinearLayout.setBackgroundColor(color);
                                } else {
                                    Palette.Swatch mutedSwatch = p.getMutedSwatch();
                                    if (mutedSwatch != null) {
                                        int color = mutedSwatch.getRgb();
                                        slidedLinearLayout.setBackgroundColor(color);
                                    }
                                }

                                // Set the images
                                slideAlbumArt.setImageURI(model);
                                slidedAlbumArt.setImageURI(model);

                                return true;
                            }
                        })
                        .into(slideAlbumArt);

                Glide.with(v.getContext())
                        .load(albumArtUri)
                        .asBitmap()
                        .placeholder(R.drawable.untitled_album)
                        .listener(new RequestListener<Uri, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                // Retrieve the palette of colors from the Bitmap first
                                Palette p = Palette.from(resource).generate();
                                Palette.Swatch swatch = p.getVibrantSwatch();
                                if (swatch != null) {
                                    int color = swatch.getRgb();
                                    slidedLinearLayout.setBackgroundColor(color);
                                } else {
                                    Palette.Swatch mutedSwatch = p.getMutedSwatch();
                                    if (mutedSwatch != null) {
                                        int color = mutedSwatch.getRgb();
                                        slidedLinearLayout.setBackgroundColor(color);
                                    }
                                }

                                // Set the images
                                slideAlbumArt.setImageURI(model);
                                slidedAlbumArt.setImageURI(model);

                                return true;
                            }
                        })
                        .into(slidedAlbumArt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            mediaControls_Repeat.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(MainActivity.this, "Permission denied to write your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
