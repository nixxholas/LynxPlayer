package com.nixholas.materialtunes.Utils;

import android.provider.BaseColumns;

/**
 * Created by nixholas on 25/1/17.
 */

public interface DBConstants extends BaseColumns {
    public static final String TABLE_NAME = "medialog";

    // columns
    public static final String TIME = "time";
    public static final String TITLE = "title";
    public static final String COUNT = "count";
}
