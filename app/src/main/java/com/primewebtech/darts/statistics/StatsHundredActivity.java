package com.primewebtech.darts.statistics;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.primewebtech.darts.R;

/**
 * Created by benebsworth on 17/6/17.
 */

public class StatsHundredActivity extends FragmentActivity{
    /**
     * This activity is responsible for managing the swipable fragements that display the statistics
     * for each of the peg bords, 4,2,... etc in the one score mode.
     */
    StatsHundredPagerAdapter mStatsPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Bundle b = getIntent().getExtras();
        int pegValue = 0;
        if (b != null) {
            pegValue = b.getInt("PEG_VALUE");
        }

        mStatsPagerAdapter = new StatsHundredPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        // TODO: display the score stats thats currently been selected by the user. I.e if the user
        // is scoring on peg 140, then when they select stats, it should should the stats for peg 140.
        mViewPager.setAdapter(mStatsPagerAdapter);
        if (pegValue != 0) {
            switch (pegValue) {
                case 100:
                    mViewPager.setCurrentItem(0);
                    break;
                case 140:
                    mViewPager.setCurrentItem(1);
                    break;
                case 180:
                    mViewPager.setCurrentItem(2);
                    break;
                default:
                    mViewPager.setCurrentItem(0);
                    break;
            }
        }
    }

}

