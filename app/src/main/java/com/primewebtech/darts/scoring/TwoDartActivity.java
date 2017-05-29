package com.primewebtech.darts.scoring;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.primewebtech.darts.R;
import com.primewebtech.darts.database.model.ActionSchema;

/**
 * Created by benebsworth on 29/5/17.
 */

public class TwoDartActivity extends AppCompatActivity implements ActionSchema {
    /**
     * The two dart activity is a little different compared to the one dart activity in that there
     * will be two values which are incremented on the pin board, the two dot and three dot values.
     * These two scores represent the counts of times the selected pagerview value has been
     * pegged with two and three darts respectively. These respective counts are incremented via the
     * 2 and 3 buttons below the peg board.
     *
     * In terms of UI we have a pinboard which changes completed per 10 peg value increment, so for
     * example
     * 50..59 -> pedboard_50s
     * 60..69 -> pegboard_60s
     *
     * We can change the peg value by swiping left or write to decrement/increment the value by one,
     * or pressing the -10 or +10 to change the score in 10's, obviously causing an immediate
     * transition in the pin board graphic.
     *
     * Ranges frm 61 to 110 without 99 102 103 105 106 108 109, as they are not 2 dart pegs.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.two_dart_view);
    }
}
