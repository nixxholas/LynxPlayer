package com.nixholas.materialtunes.UI;

import android.widget.ImageButton;

/**
 * Created by nixholas on 19/12/16.
 */

public class ButtonHelper {

    // We'll need to have a method to "grey" out a button
    // http://stackoverflow.com/questions/8743120/how-to-grey-out-a-button
    public void greyOut(ImageButton imageButton) {
        // This works so, let's use this instead
        imageButton.setAlpha(.5f);
    }

    public void unGreyOut(ImageButton imageButton) {
        imageButton.setAlpha(1f);
    }
}
