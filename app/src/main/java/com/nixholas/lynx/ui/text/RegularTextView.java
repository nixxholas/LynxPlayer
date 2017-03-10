package com.nixholas.lynx.ui.text;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by nixholas on 28/1/17.
 *
 * http://stackoverflow.com/questions/27588965/how-to-use-custom-font-in-android-studio
 */

public class RegularTextView extends TextView {

    public RegularTextView(Context context) {
        super(context);
        init();
    }

    public RegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RegularTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RegularTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/lineto-circular-pro-book.ttf");
        setTypeface(tf);
    }
}
