package com.primewebtech.darts.statistics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.primewebtech.darts.R;
import com.primewebtech.darts.scoring.HundredDartActivity;
import com.primewebtech.darts.statistics.Fragments.StatsHundredFragment;

import org.malcdevelop.cyclicview.CyclicFragmentAdapter;
import org.malcdevelop.cyclicview.CyclicView;


/**
 * Created by benebsworth on 17/6/17.
 */

public class StatsHundredActivity extends FragmentActivity{
    /**
     * This activity is responsible for managing the swipable fragements that display the statistics
     * for each of the peg bords, 4,2,... etc in the one score mode.
     */
    CyclicView mViewPager;
    SharedPreferences prefs = null;
    private static final String TAG = StatsHundredActivity.class.getSimpleName();

    private boolean isFromScoring;

    private int[] pegValues = {
            100,
            140,
            180
    };
    private String[] pegValueStrings = {
            "100+",
            "140+",
            "180",
    };

    public interface OnBackClickListener {
        boolean onBackClick();
    }

    private OnBackClickListener onBackClickListener;

    public void setOnBackClickListener(OnBackClickListener onBackClickListener) {
        this.onBackClickListener = onBackClickListener;
    }

    @Override
    public void onBackPressed() {
        if (onBackClickListener != null && onBackClickListener.onBackClick()) {
            return;
        }
        Log.d(TAG, "onBackPressed:clicked:isFromScoring:"+isFromScoring);
        if (mViewPager != null && isFromScoring) {
            int position = mViewPager.getCurrentPosition();
            Log.d(TAG, "onBackPressed:clicked:cur_pos:"+position);
            Intent scoreHundredIntent = new Intent(StatsHundredActivity.this, HundredDartActivity.class);
//            Bundle b = new Bundle();
//            b.putInt("POSITION", position);
            prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
            prefs.edit().putInt("POSITION_HUNDRED", position).apply();
            startActivity(scoreHundredIntent);

        }


        super.onBackPressed();

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Bundle b = getIntent().getExtras();
        int pegValue = 0;
        isFromScoring = false;
        if (b != null) {
            pegValue = b.getInt("PEG_VALUE");
            isFromScoring = b.getBoolean("IS_FROM_SCORING");

        }
        mViewPager = (CyclicView) findViewById(R.id.pager);
        mViewPager.setChangePositionFactor(4000);
        // TODO: display the score stats thats currently been selected by the user. I.e if the user
        // is scoring on peg 140, then when they select stats, it should should the stats for peg 140.
        mViewPager.setAdapter(new CyclicFragmentAdapter(this, getSupportFragmentManager()) {
            @Override
            protected Fragment createFragment(int i) {
                return StatsHundredFragment.newInstance(pegValues[i]);
            }

            @Override
            public int getItemsCount() {
                return 3;
            }
        });
        if (pegValue != 0) {
            switch (pegValue) {
                case 100:
                    mViewPager.setCurrentPosition(0);
                    break;
                case 140:
                    mViewPager.setCurrentPosition(1);
                    break;
                case 180:
                    mViewPager.setCurrentPosition(2);
                    break;
                default:
                    mViewPager.setCurrentPosition(0);
                    break;
            }
        }
    }

}

