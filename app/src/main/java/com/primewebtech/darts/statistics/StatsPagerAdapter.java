package com.primewebtech.darts.statistics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.primewebtech.darts.statistics.Fragments.StatsHundredFragment;
import com.primewebtech.darts.statistics.Fragments.StatsOneFragment;

public class StatsPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;

    public StatsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return StatsOneFragment.newInstance();
            case 1:
                return StatsHundredFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}
