package com.nixholas.materialtunes.Utils;

import android.provider.BaseColumns;

/**
 * Created by nixholas on 25/1/17.
 */

public interface DBConstants extends BaseColumns {
    public static final String MEDIACOUNT_TABLE = "mediacount";

    // columns
    public static final String ALBUM = "album";
    public static final String TITLE = "title";
    public static final String COUNT = "count";
}
