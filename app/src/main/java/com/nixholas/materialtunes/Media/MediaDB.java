package com.nixholas.materialtunes.Media;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nixholas.materialtunes.Media.Entities.Song;

import java.util.HashMap;

import static android.provider.BaseColumns._ID;
import static com.nixholas.materialtunes.Utils.DBConstants.ALBUMTITLE;
import static com.nixholas.materialtunes.Utils.DBConstants.MEDIACOUNT_TABLE;
import static com.nixholas.materialtunes.Utils.DBConstants.PLAYCOUNT;
import static com.nixholas.materialtunes.Utils.DBConstants.TITLE;

/**
 * Created by nixholas on 25/1/17.
 */

public class MediaDB extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "MaterialTunes";
    private static final int DATABASE_VERSION = 7;

    public MediaDB(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("MediaDB", "onCreate");
        db.execSQL("CREATE TABLE " + MEDIACOUNT_TABLE + " (" + _ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PLAYCOUNT + "INTEGER, "
                + ALBUMTITLE + "TEXT, "
                + TITLE + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("MediaDB", "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + MEDIACOUNT_TABLE);
        onCreate(db);
    }

    public void addSongToMediaCount(Song song) {
        song.setCount(1);
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PLAYCOUNT, song.getCount()); // Song Play Count
        values.put(ALBUMTITLE, song.getAlbumName()); // Album ID
        values.put(TITLE, song.getTitle()); // Song Title

        // Inserting Row
        db.insert(MEDIACOUNT_TABLE, null, values);
        db.close(); // Closing database connection
    }

    public long retrieveSongCount(String albumTitle, String songTitle) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(MEDIACOUNT_TABLE, new String[]{ _ID,
                TITLE, ALBUMTITLE, PLAYCOUNT}, TITLE + "=?, " + ALBUMTITLE + "=?",
        new String[]{songTitle, albumTitle}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        // return shop
        return Long.parseLong(cursor.getString(3));
    }


    public void newCountToDB(String albumTitle, String title) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLAYCOUNT, 1);
        values.put(ALBUMTITLE, albumTitle);
        values.put(TITLE, title);
        db.insertOrThrow(MEDIACOUNT_TABLE, null, values);
        db.close();
    }

    public boolean checkMediaCountIfExists(String albumTitle, String title) {
        SQLiteDatabase db = getWritableDatabase();

        // Check if table has any rows first
        String count = "SELECT count(*) FROM " + MEDIACOUNT_TABLE;
        Cursor mcursor = db.rawQuery(count, null);

        mcursor.moveToFirst();
        int tableCount = mcursor.getInt(0);
        if (tableCount > 0) {
            // Finally, check if the actual song exists
            // http://stackoverflow.com/questions/9280692/android-sqlite-select-query
            Cursor c = db.rawQuery("SELECT " + _ID + " FROM " + MEDIACOUNT_TABLE
                    + " WHERE " + TITLE + " = '" + title + "'"
                    + " AND " + ALBUMTITLE + " = '"+ albumTitle + "'", null);
            if (c.moveToFirst()) {
                c.close();
                db.close();
                return true;
            } else {
                c.close();
                db.close();
                return false;
            }
        } else {
            return false;
        }
    }
}
