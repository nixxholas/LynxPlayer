package com.nixholas.materialtunes.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.nixholas.materialtunes.R;

import java.io.File;
import java.io.FileDescriptor;

import static com.nixholas.materialtunes.MainActivity.getInstance;

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

            // Prevent FileNotFoundException
            // http://stackoverflow.com/questions/16237950/android-check-if-file-exists-without-creating-a-new-one
            if (!new File("content://media/external/audio/albumart/" + album_id).exists()) {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inSampleSize = 8;

                return BitmapFactory.decodeResource(getInstance().getResources()
                        , R.drawable.untitled_album, o);
            }

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BitmapFactory.decodeResource(getInstance().getResources()
                    , R.drawable.untitled_album);
        }

        return bm;
    }
}

