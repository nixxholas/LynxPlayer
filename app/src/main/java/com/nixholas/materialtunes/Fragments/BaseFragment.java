package com.nixholas.materialtunes.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Adapted from http://stackoverflow.com/questions/6215239/getactivity-returns-null-in-fragment-function
 *
 * Created by nixholas on 24/12/16.
 */

public class BaseFragment extends Fragment {
    protected FragmentActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity) activity;
    }
}
