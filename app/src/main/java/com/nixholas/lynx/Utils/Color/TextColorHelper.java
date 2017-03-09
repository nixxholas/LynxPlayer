package com.nixholas.lynx.Utils.Color;

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

    // http://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
    public static boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }
}
