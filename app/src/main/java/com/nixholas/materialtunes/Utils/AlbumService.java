package com.nixholas.materialtunes.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nixholas on 12/1/17.
 */

public class AlbumService {
    public static Uri getAlbumArtUri(long albumID) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumID);
    }

    // http://stackoverflow.com/questions/1954434/cover-art-on-android
    public static Bitmap getAlbumArt(Context context, Long album_id)
    {
        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bm;
    }
}

