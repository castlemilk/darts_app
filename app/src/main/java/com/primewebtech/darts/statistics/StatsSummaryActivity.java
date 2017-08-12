package com.primewebtech.darts.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.primewebtech.darts.R;
import com.primewebtech.darts.statistics.Fragments.StatsHundredFragmentSummary;
import com.primewebtech.darts.statistics.Fragments.StatsOneFragmentSummary;

import org.malcdevelop.cyclicview.CyclicFragmentAdapter;
import org.malcdevelop.cyclicview.CyclicView;


/**
 * Created by benebsworth on 17/6/17.
 */

public class StatsSummaryActivity extends FragmentActivity{
    StatsSummaryPagerAdapter mStatsPagerAdapter;
    CyclicView mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Bundle b = getIntent().getExtras();

        String scoreType = "SUMMARY";
        if (b != null) {
            scoreType = b.getString("SUMMARY");
        }

        mStatsPagerAdapter = new StatsSummaryPagerAdapter(getSupportFragmentManager());
        mViewPager = (CyclicView) findViewById(R.id.pager);
        mViewPager.setChangePositionFactor(4000);

        mViewPager.setAdapter(new CyclicFragmentAdapter(this, getSupportFragmentManager()) {
            @Override
            protected Fragment createFragment(int i) {
                switch (i) {
                    case 0:
                        return StatsOneFragmentSummary.newInstance();
                    case 1:
                        return StatsHundredFragmentSummary.newInstance();
                    default:
                        return StatsOneFragmentSummary.newInstance();
                }

            }

            @Override
            public int getItemsCount() {
                return 2;
            }
        });
        if (scoreType != null) {
            switch (scoreType) {
                case "one":
                    mViewPager.setCurrentPosition(0);
                    break;
                case "hundred":
                    mViewPager.setCurrentPosition(1);
                    break;
                default:
                    mViewPager.setCurrentPosition(0);
                    break;
            }
        }
    }

}

