package com.primewebtech.darts.scoring.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.primewebtech.darts.R;

/**
 * Created by benebsworth on 23/5/17.
 */

public class FragmentOneDart extends AbstractFragment{

    private View oneDartView;
    private ImageView pin;

    public static FragmentOneDart newInstance() {
        FragmentOneDart fragment = new FragmentOneDart();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //TODO: check date
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        oneDartView = inflater.inflate(R.layout.one_dart_view, container, false);
        pin = (ImageView) oneDartView.findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_60s);
        return oneDartView;
    }

}
