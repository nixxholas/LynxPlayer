package com.nixholas.materialtunes.Utils;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by nixholas on 28/2/17.
 */

public class BitmapUtils {
    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        photo = Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }
}
