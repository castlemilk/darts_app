package com.primewebtech.darts.statistics;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.primewebtech.darts.R;

/**
 * Created by benebsworth on 17/6/17.
 */

public class StatsActivity  extends FragmentActivity{
    StatsPagerAdapter mStatsPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Bundle b = getIntent().getExtras();

        String scoreType = "one";
        if (b != null) {
            scoreType = b.getString("scoreType");
        }

        mStatsPagerAdapter = new StatsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);

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

