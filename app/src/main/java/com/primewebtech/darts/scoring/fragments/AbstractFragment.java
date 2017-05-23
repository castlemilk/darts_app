package com.primewebtech.darts.scoring.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Created by benebsworth on 23/5/17.
 */

public class AbstractFragment extends Fragment {

    public static final String TAG = AbstractFragment.class.getSimpleName();

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach (Activity activity) {
        super.onAttach(activity);
    }
}
