package com.nixholas.materialtunes.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

/**
 * Created by nixho on 26-Nov-16.
 */

public class PreferenceHelper {
    public static final String INTRO_DONE = "intro_done";
    public static final String DARK_MODE = "dark_mode";

    // Playback Preferences
    public static final String SHUFFLE = "shuffle";
    public static final String REPEAT = "repeat";

    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mPreferenceEditor;

    public PreferenceHelper(Context mContext) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

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
        // http://stackoverflow.com/questions/16194567/android-sharedpreferences-how-to-save-a-simple-int-variable
        mPreferenceEditor = mPreferences.edit();
        mPreferenceEditor.putInt(REPEAT, value);
    }

    public final int getRepeat() { return mPreferences.getInt(REPEAT, 0); }
}
