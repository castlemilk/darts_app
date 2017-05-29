package com.primewebtech.darts.scoring;

import android.support.v7.app.AppCompatActivity;

import com.primewebtech.darts.database.model.ActionSchema;

/**
 * Created by benebsworth on 29/5/17.
 */

public class HundredDartActivity extends AppCompatActivity implements ActionSchema {
    /**
     * The HundredDart scoring layout follows a similar design to the one dart scoring
     *
     * This activity is responsible for the display and logging of the One Dart scoring category
     * In this scoring category the user can swipe left and right the peg value and then increment
     * the count, which is then indicated on the rim of the pin in white circles. Each white circle
     * represents that value being pegged 100 times, if a full circle is completed then it cycles to
     * green cirlces around the rim then on the next completion it goes red.
     *
     * There is a number underneath the centre of the pin which represents the number of times a given
     * peg value has been completed for the day. It will reset on the next day with the historical data
     * being logged for the statistics/analytics stage.
     *
     * Swiping left and right changes to the other peg values of 140+ and 180, where we can again
     * increment the score
     *
     * Thid module mirrors the functionality presented in the one dart scoring of a back/undo button
     * and the statistics view
     */
}
