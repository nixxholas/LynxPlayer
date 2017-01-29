package com.nixholas.materialtunes.Media;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nixholas.materialtunes.Media.Entities.Song;

import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.BaseColumns._ID;
import static com.nixholas.materialtunes.Utils.DBConstants.MEDIACOUNT_TABLE;
import static com.nixholas.materialtunes.Utils.DBConstants.MEDIASTOREID;
import static com.nixholas.materialtunes.Utils.DBConstants.PLAYCOUNT;
import static com.nixholas.materialtunes.Utils.DBConstants.TITLE;

/**
 * Created by nixholas on 25/1/17.
 *
 * Useful guides
 * http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/
 */

public class MediaDB extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "MaterialTunes";
    private static final int DATABASE_VERSION = 10;

    public MediaDB(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("MediaDB", "onCreate");
        db.execSQL("CREATE TABLE " + MEDIACOUNT_TABLE + " (" + _ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PLAYCOUNT + " INTEGER, "
                + MEDIASTOREID + " INTEGER, "
                + TITLE + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("MediaDB", "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + MEDIACOUNT_TABLE);
        onCreate(db);
    }

    public void addSongToMediaCount(Song song) {
        try {
            song.setCount(1);
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(PLAYCOUNT, song.getCount()); // Song Play Count
            values.put(MEDIASTOREID, song.getId()); // Song's MediaStore Id

            String title = song.getTitle().replaceAll("([^a-zA-Z0-9])", "\\$1");
            values.put(TITLE, title); // Song Title

            // Inserting Row
            db.insert(MEDIACOUNT_TABLE, null, values);
            db.close(); // Closing database connection
        } catch (Exception ex) {
            Log.d("addSongToMediaCount", ex.toString());
        }
    }

    public void incrementMediaCount(Song song) {
        SQLiteDatabase db = getWritableDatabase();

        // http://stackoverflow.com/questions/25149580/sql-server-existing-column-and-value-incrementing
        String increment = "UPDATE " + MEDIACOUNT_TABLE
                + " SET " + PLAYCOUNT + " = " + PLAYCOUNT + " + 1 "
                + "WHERE " + TITLE + " = '" + song.getTitle().replaceAll("([^a-zA-Z0-9])", "\\$1") + "' "
                + "AND " + MEDIASTOREID + " = " + song.getId();

        db.execSQL(increment);
    }

    public long retrieveSongCount(long mediaStoreId, String songTitle) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(MEDIACOUNT_TABLE, new String[]{ _ID,
                TITLE, MEDIASTOREID, PLAYCOUNT}, TITLE + "=?, " + MEDIASTOREID + "=?",
        new String[]{songTitle, String.valueOf(mediaStoreId)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        // return shop
        return Long.parseLong(cursor.getString(3));
    }

//    public void newCountToDB(String albumTitle, String title) {
//        SQLiteDatabase db = getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(PLAYCOUNT, 1);
//        values.put(ALBUMTITLE, albumTitle);
//        values.put(TITLE, title);
//        db.insertOrThrow(MEDIACOUNT_TABLE, null, values);
//        db.close();
//    }

    public boolean checkMediaCountIfExists(long mediaStoreId, String title) {
        SQLiteDatabase db = getWritableDatabase();

        // Check if table has any rows first
        // http://stackoverflow.com/questions/22630307/sqlite-check-if-table-is-empty
        String count = "SELECT count(*) FROM " + MEDIACOUNT_TABLE;
        Cursor mcursor = db.rawQuery(count, null);

        mcursor.moveToFirst();
        int tableCount = mcursor.getInt(0);
        if (tableCount > 0) {
            mcursor.close();

            // Perform a string check
            title = title.replaceAll("([^a-zA-Z0-9])", "\\$1");

            // Finally, check if the actual song exists
            // http://stackoverflow.com/questions/9280692/android-sqlite-select-query
            Cursor c = db.rawQuery("SELECT " + _ID + " FROM " + MEDIACOUNT_TABLE
                    + " WHERE " + TITLE + " = '" + title + "'"
                    + " AND " + MEDIASTOREID + " = "+ mediaStoreId, null);
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
            mcursor.close();
            db.close();
            return false;
        }
    }

    public List<Long> retrieveTopPlayed() {
        SQLiteDatabase db = getReadableDatabase();
        List<Long> topPlayedId = new ArrayList<>();

        /**
         * SELECT column1, column2, hit_pages,...
         * FROM YourTable
         * ORDER BY hit_pages DESC
         * LIMIT 5
         */
        String topPlayedQuery = "SELECT " + MEDIASTOREID + " "
                + "FROM " + MEDIACOUNT_TABLE + " "
                + "ORDER BY " + PLAYCOUNT + " DESC "
                + "LIMIT 20"; // We'll limit it to the top 20 Played, we can make this dynamic with
                              // SharedPref in the future.

        Cursor c = db.rawQuery(topPlayedQuery, null);

        // Loop throughout all of the results and add it to the topPlayed ArrayList
        if (c.moveToFirst()) {
            do {
                // adding to topPlayedId
                //topPlayedId.add(c.getLong(c.getColumnIndex(MEDIASTOREID)));
                topPlayedId.add(c.getLong(0)); // 0 since we only have selected one column
            } while (c.moveToNext());
        }

        return topPlayedId;
    }

    public HashMap<Long, Long> retrieveCountFromDB() {
        SQLiteDatabase db = getReadableDatabase();
        HashMap<Long, Long> mediaCount = new HashMap<>();

        String countAndId = "SELECT " + MEDIASTOREID + ", " + PLAYCOUNT
                + " FROM " + MEDIACOUNT_TABLE;

        Cursor c = db.rawQuery(countAndId, null);

        // Loop throughout all of the results and add it to the topPlayed ArrayList
        if (c.moveToFirst()) {
            do {
                // Add to the hashmap
                Log.d("Column 0 ", c.getLong(0) + "");
                Log.d("Column 1 ", c.getLong(1) + "");
                mediaCount.put(c.getLong(0), c.getLong(1));
            } while (c.moveToNext());
        }

        return mediaCount;
    }
}
