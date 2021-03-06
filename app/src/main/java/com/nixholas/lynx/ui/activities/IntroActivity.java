package com.nixholas.lynx.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.nixholas.lynx.ui.fragments.IntroFragment;
import com.nixholas.lynx.R;

/**
 * Created by nixho on 10-Nov-16.
 */

public class IntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(IntroFragment.newInstance(R.layout.intro));
        addSlide(IntroFragment.newInstance(R.layout.intro2));
        addSlide(IntroFragment.newInstance(R.layout.intro3));
        addSlide(IntroFragment.newInstance(R.layout.intro_last));

        // Note here that we DO NOT use setContentView();
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.VIBRATE}, 2);

        askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 3);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        /*addSlide(AppIntroFragment.newInstance("Welcome to Lynx.",
                "Lynx provides you a simple and intuitive interface" +
                        "for you to listen to your audio content.", R.drawable.untitled_album, Color.parseColor("#3F51B5")));*/

        // OPTIONAL METHODS
        // Override bar/separator color.
        /*setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));*/

        // Hide Skip/Done button.
        showSkipButton(false);
        skipButtonEnabled = false;
        setProgressButtonEnabled(true);
        showStatusBar(false);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(true);
        setVibrateIntensity(30);
        setSlideOverAnimation();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        /**
         *  Make sure we set intro done to true so that the user doesn't have
         *  to face this nonsense again
         */
        MainActivity.preferenceHelper.setIntroDone(true);

        // Do something when users tap on Done button.
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
