package com.nixholas.materialtunes.Utils.UI;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.widget.ImageButton;

/**
 * Created by nixholas on 19/12/16.
 */

public class ButtonHelper {
    public void setDisabled(ImageButton imageButton) {
        // This works so, let's use this instead
        imageButton.setAlpha(.5f);
    }
}
