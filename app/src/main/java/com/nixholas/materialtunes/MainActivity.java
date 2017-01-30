package com.nixholas.materialtunes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nixholas.materialtunes.Fragments.AlbumFragment;
import com.nixholas.materialtunes.Fragments.PlaylistFragment;
import com.nixholas.materialtunes.Fragments.SongFragment;
import com.nixholas.materialtunes.Media.Adapter.DataAdapter;
import com.nixholas.materialtunes.Media.MediaManager;
import com.nixholas.materialtunes.Notification.PersistentNotification;
import com.nixholas.materialtunes.UI.CustomSlidingUpLayout;
import com.nixholas.materialtunes.UI.SlidingBarUpdater;
import com.nixholas.materialtunes.UI.ButtonHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import static com.nixholas.materialtunes.IntroActivity.preferenceHelper;
import static com.nixholas.materialtunes.UI.MediaControlUpdater.mediaControlsOnClickNext;
import static com.nixholas.materialtunes.UI.MediaControlUpdater.mediaControlsOnClickPlayPause;
import static com.nixholas.materialtunes.UI.MediaControlUpdater.mediaControlsOnClickPrevious;

/**
 * Android Security TTN (To Take Note) Of
 *
 * https://securityintelligence.com/new-vulnerability-android-framework-fragment-injection/
 *
 *
 */
public class MainActivity extends AppCompatActivity {
    // Protected Entities
    ButtonHelper buttonHelper = new ButtonHelper();

    // Sliding Up Bar
    public static ImageView slideAlbumArt;
    public static TextView slideSongArtist;
    public static TextView slideSongTitle;
    public static ImageButton slideButton;
    public static RelativeLayout slideRelativeLayout;
    public static CustomSlidingUpLayout slidingUpPanelLayout;
    public static SeekBar slidingSeekBar;

    // Expanded View of Sliding Up Bar
    public static ImageView slidedAlbumArt;
    public static RelativeLayout slidedRelativeLayout;
    public static ImageButton mediaControls_PlayPause;
    public static ImageButton mediaControls_Previous;
    public static ImageButton mediaControls_Next;
    public static ImageButton mediaControls_Shuffle;
    public static ImageButton mediaControls_Repeat;
    public static ImageButton slidedCloseButton;
    public static TextView mediaSeekText_Progress;
    public static TextView mediaSeekText_Maximum;
    public static TextView slided_SongTitle;
    public static TextView slided_SongArtist;
    public static SeekBar slidedSeekBar;

    // Publicly Accessible Entities
    public static MediaManager mediaManager;
    public static DataAdapter mDataAdapter;

    // Fragment Entities
    private AlbumFragment albumFragment;
    private PlaylistFragment playlistFragment;
    SongFragment songFragment;

    // Notification Entities
    private static MainActivity finalMain;
    public static PersistentNotification persistentNotif;

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
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 800;
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

        slidedRelativeLayout = (RelativeLayout) findViewById(R.id.slided_layout);
        slidingUpPanelLayout = (CustomSlidingUpLayout) findViewById(R.id.sliding_layout);

        slidingUpPanelLayout.setDragView(findViewById(R.id.dragView));

        // Sliding Up Bar
        slideAlbumArt = (ImageView) findViewById(R.id.slide_albumart);
        slideSongArtist = (TextView) findViewById(R.id.slide_songartist);
        slideSongTitle = (TextView) findViewById(R.id.slide_songtitle);
        slideButton = (ImageButton) findViewById(R.id.slide_button);
        slideRelativeLayout = (RelativeLayout) findViewById(R.id.slide_layout);
        mDataAdapter = new DataAdapter(getContentResolver());
        slidingSeekBar = (SeekBar) findViewById(R.id.slide_seekbar);
        slidingSeekBar.getThumb().mutate().setAlpha(0);

        // Expanded View of Sliding Up Bar
        slidedAlbumArt = (ImageView) findViewById(R.id.slided_image);
        slidedSeekBar = (SeekBar) findViewById(R.id.slided_seekbar);
        mediaSeekText_Progress = (TextView) findViewById(R.id.slided_seekTextCurrent);
        mediaSeekText_Maximum = (TextView) findViewById(R.id.slided_seekTextMax);
        slided_SongTitle = (TextView) findViewById(R.id.slided_title);
        slided_SongArtist = (TextView) findViewById(R.id.slided_artist);
        slidedCloseButton = (ImageButton) findViewById(R.id.slided_close);

        //slidingSeekBar.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        //slidedSeekBar.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

        slidedCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        mediaControls_PlayPause = (ImageButton) findViewById(R.id.media_controls_playpause);
        mediaControls_PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaControlsOnClickPlayPause();
            }
        });

        mediaControls_Previous = (ImageButton) findViewById(R.id.media_controls_previous);
        mediaControls_Previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaControlsOnClickPrevious(view);
            }
        });

        mediaControls_Next = (ImageButton) findViewById(R.id.media_controls_next);
        mediaControls_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaControlsOnClickNext(view);
            }
        });

        mediaControls_Shuffle = (ImageButton) findViewById(R.id.media_controls_shuffle);
        if (preferenceHelper.getShuffle()) {
            mediaControls_Shuffle.setAlpha(1f);
        } else {
            buttonHelper.greyOut(mediaControls_Shuffle);
        }

        mediaControls_Repeat = (ImageButton) findViewById(R.id.media_controls_repeat);
        mediaControls_Repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaControlsOnClickRepeat();
            }
        });

        switch (preferenceHelper.getRepeat()) {
            case 0: // Repeat None
                buttonHelper.greyOut(mediaControls_Repeat);
                break;
            case 1: // Repeat All
                // Don't have to greyout
                break;
            case 2: // Repeat One Only
                mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_one_white_48dp);
                break;
        }


        // Setup the MediaManager and hide the sliding bar if the MediaManager is null.
        //        if (mediaManager == null) {
        //            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        //            mediaManager = new MediaManager(this);
        //            finalMain = this;
        //        } else {
        //            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        //            SlidingBarUpdater.updateSlideBar(MainActivity.this);
        //        }

        mediaManager = new MediaManager(this);
        mediaManager.initializeMediaDB(this);
        finalMain = this;

        // Setup the notifications
        Handler mHandler = new Handler();
        //Context appContext = getBaseContext().getApplicationContext();
        mHandler.post(persistentNotif = new PersistentNotification(MainActivity.this));

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        mDataAdapter.run();

        mediaManager.mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        slidedRelativeLayout.setAlpha(0);

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

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i("PanelSlideListener", "onPanelSlide, offset " + slideOffset);

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

                // Set the Unexpanded Views first
                slideButton.setImageAlpha(unexpandedOffset);
                slideAlbumArt.setImageAlpha(unexpandedOffset);
                slideSongTitle.setAlpha(1 - slideOffset);
                slideSongArtist.setAlpha(1 - slideOffset);

                // Then the expanded views
                slidedRelativeLayout.setAlpha(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                //Log.i("PanelSlideListener", "onPanelStateChanged " + newState);
            }
        });
    }

    /**
     * http://stackoverflow.com/questions/22869928/android-broadcastreceiver-onreceive-update-textview-in-mainactivity
     * @return
     */
    public static MainActivity getInstance(){
        return finalMain;
    }

    @Override
    public void onResume() {
        super.onResume();

        SlidingBarUpdater.updateSlideBar(this);
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
                    songFragment = SongFragment.newInstance(position);
                    return songFragment;
                case 1:
                    //Log.e("AlbumFragment", "SectionsPagerAdapter/getItem Call");
                    return AlbumFragment.newInstance(position);
                case 2:
                    playlistFragment = PlaylistFragment.newInstance(position);
                    return playlistFragment;
                default:
                    return SongFragment.newInstance(position);
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

    public PlaylistFragment getPlaylistFragment() {
        return playlistFragment;
    }

    /**
     * The two buttons below must have the same binded views at the same time.
     * @param v
     */
    public void slideButtonOnClick(View v) {
        Log.e("Slide Button", "Clicked");

        if (mediaManager.mMediaPlayer.isPlaying() || mediaManager.mediaPlayerIsPaused) {
            // http://stackoverflow.com/questions/25381624/possible-to-detect-paused-state-of-mediaplayer
            if (mediaManager.mediaPlayerIsPaused) { // If the current song is paused,
                mediaManager.mMediaPlayer.start();
                mediaManager.mediaPlayerIsPaused = false;
                //http://stackoverflow.com/questions/7024881/replace-one-image-with-another-after-clicking-a-button
                slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
            } else { // Else we pause it
                mediaManager.mMediaPlayer.pause();
                mediaManager.mediaPlayerIsPaused = true;
                slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            }

            persistentNotif.updateNotification();
        }
    }

    public void clickPrevious(View view) {
        mediaControlsOnClickPlayPause();
    }

    public void clickPlayPause(View view) {
        mediaControlsOnClickPlayPause();
    }

    public void clickNext(View view) {
        mediaControlsOnClickNext(view);
    }

    public void mediaControlsOnClickRepeat () {
        if (mediaManager.getRepeatState() == MediaManager.RepeatState.NOREPEAT) {
            mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_white_24dp);

            // Next is repeat all..
            mediaManager.setRepeatState(MediaManager.RepeatState.REPEATALL);
            //Log.e("getmPlaybackState()", "Repeat All");
            buttonHelper.unGreyOut(mediaControls_Repeat);
            preferenceHelper.setRepeat(1);

        } else if (mediaManager.getRepeatState() == MediaManager.RepeatState.REPEATALL) {
            mediaManager.mMediaPlayer.setLooping(true);

            // Next is repeat one only..
            //http://stackoverflow.com/questions/9461270/media-player-looping-android
            mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_one_white_24dp);
            mediaManager.setRepeatState(MediaManager.RepeatState.REPEATONE);
            preferenceHelper.setRepeat(2);

        } else {
            mediaManager.mMediaPlayer.setLooping(false);
            mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_white_24dp);

            // Next is repeat nothing..
            mediaManager.setRepeatState(MediaManager.RepeatState.NOREPEAT);
            buttonHelper.greyOut(mediaControls_Repeat);
            preferenceHelper.setRepeat(0);

        }
    }

    public void mediaControlsOnClickShuffle(View v) {
        if (preferenceHelper.getShuffle()) {
            preferenceHelper.setShuffle(false);
            buttonHelper.greyOut(mediaControls_Shuffle);
        } else {
            preferenceHelper.setShuffle(true);
            buttonHelper.unGreyOut(mediaControls_Shuffle);
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
