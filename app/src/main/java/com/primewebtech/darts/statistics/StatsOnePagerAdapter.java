package com.primewebtech.darts.statistics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.primewebtech.darts.statistics.Fragments.StatsOneFragment;

public class StatsOnePagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 6;

    public StatsOnePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return StatsOneFragment.newInstance(40);
            case 1:
                return StatsOneFragment.newInstance(32);
            case 2:
                return StatsOneFragment.newInstance(24);
            case 3:
                return StatsOneFragment.newInstance(36);
            case 4:
                return StatsOneFragment.newInstance(50);
            case 5:
                return StatsOneFragment.newInstance(4);

            default:
                return StatsOneFragment.newInstance(40);
        }
    }
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}
