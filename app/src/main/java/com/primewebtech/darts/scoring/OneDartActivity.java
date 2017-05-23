package com.primewebtech.darts.scoring;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.primewebtech.darts.R;

/**
 * Created by benebsworth on 24/5/17.
 */

public class OneDartActivity extends AppCompatActivity {
    /**
     *
     */

    private ImageView pin;
    private int pegsCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_dart_view);
        pin = (ImageView) findViewById(R.id.pin);
        pin.setImageResource(R.drawable.pin_60s);
    }
}
