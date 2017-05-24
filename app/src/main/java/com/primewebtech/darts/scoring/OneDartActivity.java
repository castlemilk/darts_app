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
     * This activity is responsible for the display and logging of the One Dart scoring category
     * In this scoring category the user can swipe left and right the peg value and then increment
     * the count, which is then indicated on the rim of the pin in white circles. Each white circle
     * represents that value being pegged 100 times, if a full circle is completed then it cycles to
     * green cirlces around the rim then on the next completion it goes red.
     *
     * There is a number underneath the centre of the pin which represents the number of times a give
     * peg value has been completed for the day. It will reset on the next day with the historical data
     * being logged for the statistics/analystics stage.
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
