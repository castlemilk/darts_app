package com.primewebtech.darts.scoring;

import android.support.v7.app.AppCompatActivity;

import com.primewebtech.darts.database.model.ActionSchema;

/**
 * Created by benebsworth on 29/5/17.
 */

public class ThreeDartActivity extends AppCompatActivity implements ActionSchema {
    /**
     * The three dart activity is very similar to the 2 dart pin but represents scores that can only
     * be achieved via 3 darts and hence we only need the 3 dart counter indicator.
     *
     * In terms of UI we have a pinboard which changes completed per 10 peg value increment, so for
     * example
     * 100..109 -> pedboard_100s
     * 130..139 -> pegboard_130s
     *
     * We can change the peg value by swiping left or write to decrement/increment the value by one,
     * or pressing the -10 or +10 to change the score in 10's, obviously causing an immediate
     * transition in the pin board graphic.
     *
     * One special consideration is the transition from 110 to 110+ we lose the 2 darts score as its
     * not achievable without 3 darts.
     *
     * We have a range between 99 to 170, but we exclude 100 101 104 107 110 which are
     * all 2 dart pegs
     */
}
