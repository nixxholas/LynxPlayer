package com.nixholas.materialtunes.Utils;

import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by nixholas on 12/1/17.
 */

public class AlbumService {
    public static Uri getAlbumArtUri(long albumID) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumID);
    }
}
