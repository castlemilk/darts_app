package com.primewebtech.darts.statistics;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.primewebtech.darts.R;

/**
 * Created by benebsworth on 17/6/17.
 */

public class StatsOneActivity extends FragmentActivity{
    /**
     * This activity is responsible for managing the swipable fragements that display the statistics
     * for each of the peg bords, 4,2,... etc in the one score mode.
     */
    StatsOnePagerAdapter mStatsPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Bundle b = getIntent().getExtras();

        String scoreType = "SUMMARY";
        if (b != null) {
            scoreType = b.getString("SUMMARY");
        }

        mStatsPagerAdapter = new StatsOnePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        // TODO: display the score stats thats currently been selected by the user. I.e if the user
        // is scoring on peg 40, then when they select stats, it should should the stats for peg 40.
        mViewPager.setAdapter(mStatsPagerAdapter);
        if (scoreType != null) {
            switch (scoreType) {
                case "one":
                    mViewPager.setCurrentItem(0);
                    break;
                case "hundred":
                    mViewPager.setCurrentItem(1);
                    break;
                default:
                    mViewPager.setCurrentItem(0);
                    break;
            }
        }
    }

}

