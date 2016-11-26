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
    private static SharedPreferences mPreferences;

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
}
