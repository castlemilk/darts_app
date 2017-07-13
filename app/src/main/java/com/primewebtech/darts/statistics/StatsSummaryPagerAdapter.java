package com.primewebtech.darts.statistics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.primewebtech.darts.statistics.Fragments.StatsHundredFragmentSummary;
import com.primewebtech.darts.statistics.Fragments.StatsOneFragmentSummary;

public class StatsSummaryPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;

    public StatsSummaryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return StatsOneFragmentSummary.newInstance();
            case 1:
                return StatsHundredFragmentSummary.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}
