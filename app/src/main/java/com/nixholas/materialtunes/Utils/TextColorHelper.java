package com.nixholas.materialtunes.Utils;

import android.graphics.Color;

/**
 * Created by nixholas on 10/1/17.
 */

public class TextColorHelper {
    public static int getBlackWhiteColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness <= 0.5) {
            return Color.WHITE;
        } else return Color.BLACK;
    }
}
