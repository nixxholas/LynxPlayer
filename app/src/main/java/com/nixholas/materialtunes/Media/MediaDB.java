package com.nixholas.materialtunes.Media;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nixholas.materialtunes.Media.Entities.Song;

import static android.provider.BaseColumns._ID;
import static com.nixholas.materialtunes.Utils.DBConstants.ALBUM;
import static com.nixholas.materialtunes.Utils.DBConstants.COUNT;
import static com.nixholas.materialtunes.Utils.DBConstants.MEDIACOUNT_TABLE;
import static com.nixholas.materialtunes.Utils.DBConstants.TITLE;

/**
 * Created by nixholas on 25/1/17.
 */

public class MediaDB extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "MaterialTunes";
    private static final int DATABASE_VERSION = 5;

    public MediaDB(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("MediaDB", "onCreate");
        db.execSQL("CREATE TABLE " + MEDIACOUNT_TABLE + " (" + _ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COUNT + "INTEGER, "
                + ALBUM + "INTEGER, "
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
        values.put(COUNT, song.getCount()); // Song Play Count
        values.put(ALBUM, song.getAlbumId()); // Album ID
        values.put(TITLE, song.getTitle()); // Song Title

        // Inserting Row
        db.insert(MEDIACOUNT_TABLE, null, values);
        db.close(); // Closing database connection
    }

    public long retrieveSongCount(long albumId, String songTitle) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(MEDIACOUNT_TABLE, new String[]{ _ID,
                TITLE, ALBUM, COUNT}, TITLE + "=?, " + ALBUM + "=?",
        new String[]{songTitle, String.valueOf(albumId)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        // return shop
        return Long.parseLong(cursor.getString(3));
    }


}
