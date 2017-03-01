package com.nixholas.materialtunes.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.nixholas.materialtunes.Media.Entities.Song;

/**
 * Created by nixho on 26-Nov-16.
 */

public class PreferenceHelper {
    // Default Preferences
    private static final String INTRO_DONE = "intro_done";
    private static final String CURRENT_SONG = "current_song";

    // Theming Preferences
    private static final String DARK_MODE = "dark_mode";

    // Playback Preferences
    private static final String SHUFFLE = "shuffle";
    private static final String REPEAT = "repeat";

    // Last Played Song Preferences
    private static final String LAST_PLAYED_SONG = "last_played_song";

    private static SharedPreferences mPreferences;
    private SharedPreferences.Editor mPreferenceEditor;

    public PreferenceHelper(Context mContext) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void setCurrentSongId(long _id) {
        // http://stackoverflow.com/questions/16194567/android-sharedpreferences-how-to-save-a-simple-int-variable
        mPreferenceEditor = mPreferences.edit();
        mPreferenceEditor.putLong(CURRENT_SONG, _id);

        // http://stackoverflow.com/questions/17916873/how-to-save-and-fetch-integer-value-in-shared-preference-in-android
        mPreferenceEditor.commit(); // We need to commit..
    }

    public long getCurrentSongId() { return mPreferences.getLong(CURRENT_SONG, 0); }

    private void writeBoolean(final String key, final boolean value) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(key, value);
                editor.apply();

                return null;
            }
        }.execute();
    }

    public void setIntroDone(boolean value) {
        writeBoolean(INTRO_DONE, value);
    }

    public final boolean getIntroDone() {
        /**
         * https://developer.android.com/reference/android/content/SharedPreferences.html#getBoolean(java.lang.String, boolean)
         *
         * Parameters
         * key	String: The name of the preference to retrieve.
         * defValue	boolean: Value to return if this preference does not exist.
         */
        return mPreferences.getBoolean(INTRO_DONE, false);
    }

    public void setDarkMode(boolean value) { writeBoolean(DARK_MODE, value); }

    public final boolean getDarkMode() {
        return mPreferences.getBoolean(DARK_MODE, false);
    }

    public void setShuffle(boolean value) { writeBoolean(SHUFFLE, value); }

    public final boolean getShuffle() { return mPreferences.getBoolean(SHUFFLE, false); }

    public void setRepeat(int value) {
        /**
         * Integer Values
         *
         * 0 = Repeat none
         * 1 = Repeat All
         * 2 = Repeat One Only
         */
        // http://stackoverflow.com/questions/16194567/android-sharedpreferences-how-to-save-a-simple-int-variable
        mPreferenceEditor = mPreferences.edit();
        mPreferenceEditor.putInt(REPEAT, value);

        // http://stackoverflow.com/questions/17916873/how-to-save-and-fetch-integer-value-in-shared-preference-in-android
        assert mPreferenceEditor != null;
        mPreferenceEditor.apply(); // We need to commit..
    }

    public final int getRepeat() { return mPreferences.getInt(REPEAT, 0); }

    public final int getLastPlayedSong() {
        return mPreferences.getInt(LAST_PLAYED_SONG, 0);
    }

    public void setLastPlayedSong(int currentSongIndex) {
        mPreferenceEditor =  mPreferences.edit();
        mPreferenceEditor.putInt(LAST_PLAYED_SONG, currentSongIndex);
        mPreferenceEditor.apply();
    }
}
