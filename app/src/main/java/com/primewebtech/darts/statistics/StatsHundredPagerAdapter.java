package com.primewebtech.darts.statistics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.primewebtech.darts.statistics.Fragments.StatsOneFragment;

public class StatsHundredPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 3;

    public StatsHundredPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return StatsOneFragment.newInstance(100);
            case 1:
                return StatsOneFragment.newInstance(140);
            case 2:
                return StatsOneFragment.newInstance(180);

            default:
                return StatsOneFragment.newInstance(100);
        }
    }
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}
