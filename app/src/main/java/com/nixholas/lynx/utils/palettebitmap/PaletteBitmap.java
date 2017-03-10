package com.nixholas.lynx.utils.palettebitmap;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

/**
 * Created by nixholas on 25/12/16.
 */

public class PaletteBitmap {
    public final Palette palette;
    public final Bitmap bitmap;

    public PaletteBitmap(@NonNull Bitmap bitmap, @NonNull Palette palette) {
        this.bitmap = bitmap;
        this.palette = palette;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
