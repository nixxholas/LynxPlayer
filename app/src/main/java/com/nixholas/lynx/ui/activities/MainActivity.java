package com.nixholas.lynx.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nixholas.lynx.R;
import com.nixholas.lynx.media.MediaManager;
import com.nixholas.lynx.notification.LynxNotification;
import com.nixholas.lynx.ui.CustomSlidingUpLayout;
import com.nixholas.lynx.ui.SlidingBarUpdater;
import com.nixholas.lynx.ui.elements.button.CustomImageButton;
import com.nixholas.lynx.ui.fragments.AlbumFragment;
import com.nixholas.lynx.ui.fragments.PlaylistFragment;
import com.nixholas.lynx.ui.fragments.SongFragment;
import com.nixholas.lynx.utils.PreferenceHelper;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import static com.nixholas.lynx.ui.MediaControlUpdater.mediaControlsOnClickNext;
import static com.nixholas.lynx.ui.MediaControlUpdater.mediaControlsOnClickPlayPause;
import static com.nixholas.lynx.ui.MediaControlUpdater.mediaControlsOnClickPrevious;

/**
 * Android Security TTN (To Take Note) Of
 *
 * https://securityintelligence.com/new-vulnerability-android-framework-fragment-injection/
 *
 *
 */
public class MainActivity extends AppCompatActivity {
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
    public static CustomImageButton mediaControls_Shuffle;
    public static CustomImageButton mediaControls_Repeat;
    public static ImageButton slidedCloseButton;
    public static TextView mediaSeekText_Progress;
    public static TextView mediaSeekText_Maximum;
    public static TextView slided_SongTitle;
    public static TextView slided_SongArtist;
    public static SeekBar slidedSeekBar;

    // Publicly Accessible Entities
    public static MediaManager mediaManager;
    public static PreferenceHelper preferenceHelper;

    // Fragment Entities
    public AlbumFragment albumFragment;
    public PlaylistFragment playlistFragment;
    public SongFragment songFragment;

    // Notification Entities
    private static MainActivity finalMain;
    public static LynxNotification persistentNotif;

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


    /**
     * Implementing DrawerLayout
     * http://www.viralandroid.com/2016/02/drawerlayout-navigation-drawer-view-over-android-actionbar-toolbar.html
     */
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceHelper = new PreferenceHelper(getApplicationContext());

        if (!preferenceHelper.getIntroDone()) {
            startActivity(new Intent(this, IntroActivity.class));
            // http://stackoverflow.com/questions/8282569/oncreate-flow-continues-after-finish
            finish();
            return;
        }

        // Request for proper permissions first
        // https://developer.android.com/training/permissions/requesting.html
        ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1); // Reading
        ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2); // Writing

        finalMain = this;

        slidedRelativeLayout = (RelativeLayout) findViewById(R.id.slided_layout);
        slidingUpPanelLayout = (CustomSlidingUpLayout) findViewById(R.id.sliding_layout);

        slidingUpPanelLayout.setDragView(findViewById(R.id.dragView));

        // Prepare the button animator
        // http://stackoverflow.com/questions/7564372/animating-imagebutton-in-android
        Animation rAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);

        // Sliding Up Bar
        slideAlbumArt = (ImageView) findViewById(R.id.slide_albumart);
        slideSongArtist = (TextView) findViewById(R.id.slide_songartist);
        slideSongTitle = (TextView) findViewById(R.id.slide_songtitle);
        slideButton = (ImageButton) findViewById(R.id.slide_button);
        slideRelativeLayout = (RelativeLayout) findViewById(R.id.slide_layout);
        slidingSeekBar = (SeekBar) findViewById(R.id.slide_seekbar);
        slidingSeekBar.getThumb().mutate().setAlpha(0);

        // Sliding Up Bar element animation binding
        slideButton.setAnimation(rAnim);

        // Expanded View of Sliding Up Bar
        slidedAlbumArt = (ImageView) findViewById(R.id.slided_image);
        slidedSeekBar = (SeekBar) findViewById(R.id.slided_seekbar);
        mediaSeekText_Progress = (TextView) findViewById(R.id.slided_seekTextCurrent);
        mediaSeekText_Maximum = (TextView) findViewById(R.id.slided_seekTextMax);
        slided_SongTitle = (TextView) findViewById(R.id.slided_title);
        slided_SongArtist = (TextView) findViewById(R.id.slided_artist);
        slidedCloseButton = (ImageButton) findViewById(R.id.slided_close);

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
        mediaControls_PlayPause.setAnimation(rAnim);

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

        // Setup the notifications
        Handler mHandler = new Handler();
        //Context appContext = getBaseContext().getApplicationContext();
        mHandler.post(persistentNotif = new LynxNotification(getApplicationContext()));

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

        while (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Do nothing
            // This prevents any exception from happening.
        }

        mediaControls_Shuffle = (CustomImageButton) findViewById(R.id.media_controls_shuffle);
        //Log.d("MainActivity", "getShuffle(): " + preferenceHelper.getShuffle());
        mediaControls_Shuffle.setEnabledUI(preferenceHelper.getShuffle());

        mediaControls_Repeat = (CustomImageButton) findViewById(R.id.media_controls_repeat);

        // Finally initialize the data responsible for media playback along with the database and
        // the MediaPlayer
        mediaManager = new MediaManager(getContentResolver());
        mediaManager.mLynxMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        switch (preferenceHelper.getRepeat()) {
            case 0: // Repeat None
                mediaManager.setRepeatState(MediaManager.RepeatState.NOREPEAT);
                mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_white_24dp);
                mediaControls_Repeat.setEnabledUI(false);
                break;
            case 1: // Repeat All
                // Don't have to greyout
                mediaManager.setRepeatState(MediaManager.RepeatState.REPEATALL);
                mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_white_24dp);
                mediaControls_Repeat.setEnabledUI(true);
                break;
            case 2: // Repeat One Only
                mediaManager.setRepeatState(MediaManager.RepeatState.REPEATONE);
                mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                mediaControls_Repeat.setEnabledUI(true);
                break;
            default:
                Log.d("MainActivity", "-> repeat: Something bad happened");
        }

        slidedRelativeLayout.setAlpha(0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.app_name, R.string.app_name);
        //drawerLayout.addDrawerListener(drawerToggle);

        // Setup the navigationView and its item selected listener
//        navigationView = (NavigationView) findViewById(R.id.navigation_drawer);
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                // Have an intent for any selection
//                Intent intent;
//
//                switch (item.getItemId()) {
//                    case R.id.navigation_drawer_main:
//                        drawerLayout.closeDrawers();
//                        return true;
//                    case R.id.navigation_drawer_spotify:
//                        Toast.makeText(getApplicationContext(), "Coming soon!",
//                                Toast.LENGTH_SHORT).show();
//
//                        drawerLayout.closeDrawers();
//                        return true;
//                    case R.id.navigation_drawer_about:
//                        try {
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                            Toast.makeText(getApplicationContext(), "An error has occured",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//
//                        // Temporary Information Toast
//                        Toast.makeText(getApplicationContext(), "An error has occured",
//                                Toast.LENGTH_SHORT).show();
//
//                        drawerLayout.closeDrawers();
//                        return true;
//                    case R.id.navigation_drawer_settings:
//                        intent = new Intent(getApplicationContext(), SettingsActivity.class);
//                        startActivity(intent);
//                        return true;
//                }
//
//                return false;
//            }
//        });

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("Home");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        SmartTabLayout tabLayout = (SmartTabLayout) findViewById(R.id.tabs);
        tabLayout.setViewPager(mViewPager);

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

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
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

        Log.d("onResume(): ", "is running");

        SlidingBarUpdater.updateSlideBar(finalMain);
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
        // Create an intent which will handle any of the cases.
        Intent intent;

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            // http://stackoverflow.com/questions/26582075/cannot-catch-toolbar-home-button-click-event
            case android.R.id.home:
                // http://stackoverflow.com/questions/17821532/how-to-open-navigation-drawer-with-no-actionbar-open-with-just-a-button
                drawerLayout.openDrawer(Gravity.LEFT);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
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
                    //Log.d("AlbumFragment", "SectionsPagerAdapter/getItem Call");
                    albumFragment = AlbumFragment.newInstance(position);
                    return albumFragment;
                case 2:
                    playlistFragment = PlaylistFragment.newInstance(position);
                    return playlistFragment;
                default:
                    songFragment = SongFragment.newInstance(position);
                    return songFragment;
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
        Log.d("Slide Button", "Clicked");

        if (mediaManager.mLynxMediaPlayer.isPlaying() || mediaManager.mediaPlayerIsPaused) {
            // http://stackoverflow.com/questions/25381624/possible-to-detect-paused-state-of-mediaplayer
            if (mediaManager.mediaPlayerIsPaused) { // If the current song is paused,
                mediaManager.mLynxMediaPlayer.start();
                mediaManager.mediaPlayerIsPaused = false;
                //http://stackoverflow.com/questions/7024881/replace-one-image-with-another-after-clicking-a-button
                slideButton.setImageResource(R.drawable.ic_pause_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_pause_white_36dp);
            } else { // Else we pause it
                mediaManager.mLynxMediaPlayer.pause();
                mediaManager.mediaPlayerIsPaused = true;
                slideButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                mediaControls_PlayPause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            }

            persistentNotif.updateNotification();
        }
    }

    public void clickRepeat(View view) { mediaControlsOnClickRepeat(); }

    public void clickShuffle(View view) { mediaControlsOnClickShuffle(); }

    public void clickPrevious(View view) {
        mediaControlsOnClickPlayPause();
    }

    public void clickPlayPause(View view) {
        mediaControlsOnClickPlayPause();
    }

    public void clickNext(View view) {
        mediaControlsOnClickNext(view);
    }

    public void mediaControlsOnClickRepeat() {
        Log.d("MainActivity", "mediaControlsOnClickRepeat -> getRepeat(): " + preferenceHelper.getRepeat());
        switch (preferenceHelper.getRepeat()) {
            case 0:
                mediaManager.mLynxMediaPlayer.setLooping(false);
                mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_white_24dp);
                mediaControls_Repeat.setEnabledUI(true);

                // Next is repeat all..
                mediaManager.setRepeatState(MediaManager.RepeatState.REPEATALL);
                //Log.d("getmPlaybackState()", "Repeat All");
                preferenceHelper.setRepeat(1);
                break;
            case 1:
                mediaManager.mLynxMediaPlayer.setLooping(true);
                mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                mediaControls_Repeat.setEnabledUI(true);

                // Next is repeat one only..
                //http://stackoverflow.com/questions/9461270/media-player-looping-android
                mediaManager.setRepeatState(MediaManager.RepeatState.REPEATONE);
                preferenceHelper.setRepeat(2);
                break;
            case 2:
                mediaManager.mLynxMediaPlayer.setLooping(false);
                mediaControls_Repeat.setImageResource(R.drawable.ic_repeat_white_24dp);
                mediaControls_Repeat.setEnabledUI(false);

                // Next is repeat nothing..
                mediaManager.setRepeatState(MediaManager.RepeatState.NOREPEAT);
                preferenceHelper.setRepeat(0);
                break;
            default:
                // Something bad happened lol
                Log.d("mControlsOnClickRepeat", "Something bad happened");
                break;
        }
    }

    public void mediaControlsOnClickShuffle() {
        boolean result = !preferenceHelper.getShuffle();

        Log.d("MainActivity", "mediaControlsOnClickShuffle, bool result: " + result);

        mediaControls_Shuffle.setEnabledUI(result);
        preferenceHelper.setShuffle(result);
    }

    /**
     * Method used to refresh the UI contents.
     */
    public void refreshMediaInterface() {

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
