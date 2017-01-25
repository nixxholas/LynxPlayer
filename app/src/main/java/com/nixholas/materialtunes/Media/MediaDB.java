package com.nixholas.materialtunes.Media;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;
import static com.nixholas.materialtunes.Utils.DBConstants.COUNT;
import static com.nixholas.materialtunes.Utils.DBConstants.TABLE_NAME;
import static com.nixholas.materialtunes.Utils.DBConstants.TIME;
import static com.nixholas.materialtunes.Utils.DBConstants.TITLE;

/**
 * Created by nixholas on 25/1/17.
 */

public class MediaDB extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "mtunes.db";
    private static final int DATABASE_VERSION = 1;

    public MediaDB(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + _ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TIME + " INTEGER,"
                + COUNT + "INTEGER,"
                + TITLE + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
