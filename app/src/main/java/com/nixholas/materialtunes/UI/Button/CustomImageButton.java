package com.nixholas.materialtunes.UI.Button;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

/**
 * Created by nixholas on 16/2/17.
 */

public class CustomImageButton extends ImageButton {
    public CustomImageButton(Context context) {
        super(context);
    }

    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * The most easy solution is to set color filter to the background image of a button as I saw here
     *
     *    You can do as follow:
     *
     *   if ('need to set button disable')
     *    button.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
     *    else
     *    button.getBackground().setColorFilter(null);
     *
     * http://stackoverflow.com/questions/8743120/how-to-grey-out-a-button
     *
     * @param enabled
     * The boolean of getShuffle() from sharedPreferences
     */
    public void
    setEnabledUI(boolean enabled) {
        Log.d("CustomImageButton", "setEnabledUI, bool enabled: " + enabled);

        Drawable background = getBackground();
        Drawable drawable = getDrawable();

        if (enabled) {
            if (background != null)
                background.setColorFilter(null);
            if (drawable != null)
                drawable.setColorFilter(null);
        } else {
            if (background != null)
                background.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
            if (drawable != null)
                drawable.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }
}
