package com.nixholas.materialtunes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.nixholas.materialtunes.R;


/**
 * Created by nixho on 10-Nov-16.
 *
 * http://stackoverflow.com/questions/35599108/how-to-use-preferencescreen-in-android
 */

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    // Object References
    static SharedPreferences mPreference;

    static SwitchPreference amoledPref;

    // Option References
    public static final String AMOLED_PREF = "amoled_pref";
    public static final String PRIMARY_COLOR_PREF = "primary_color_pref";
    public static final String SECONDARY_COLOR_PREF = "secondary_color_pref";
    public static final String ALBUM_GRID_COLS_PREF = "album_grid_cols_pref";
    public static final String GAPLESS_PLAYBACK_PREF = "gapless_playback_pref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fragment_preference);

            //amoledPref = (SwitchPreference)
        }
    }
}
