package com.primewebtech.darts.statistics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.primewebtech.darts.R;
import com.primewebtech.darts.scoring.OneDartActivity;
import com.primewebtech.darts.statistics.Fragments.StatsOneFragment;

import org.malcdevelop.cyclicview.CyclicFragmentAdapter;
import org.malcdevelop.cyclicview.CyclicView;


/**
 * Created by benebsworth on 17/6/17.
 */

public class StatsOneActivity extends FragmentActivity{
    /**
     * This activity is responsible for managing the swipable fragements that display the statistics
     * for each of the peg bords, 4,2,... etc in the one score mode.
     */
    CyclicView mViewPager;
    private int pegValue;
    SharedPreferences prefs = null;
    private static final String TAG = StatsOneActivity.class.getSimpleName();
    private boolean isFromScoring;
    int[] pegValues = {
            40,
            32,
            24,
            36,
            50,
            4
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
            Intent scoreOneIntent = new Intent(StatsOneActivity.this, OneDartActivity.class);
//            Bundle b = new Bundle();
//            b.putInt("POSITION", position);
            prefs = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
            prefs.edit().putInt("POSITION", position).apply();
            startActivity(scoreOneIntent);

        }
        super.onBackPressed();

    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Bundle b = getIntent().getExtras();
        isFromScoring = false;
        if (b != null) {
            pegValue = b.getInt("PEG_VALUE");
            isFromScoring = b.getBoolean("IS_FROM_SCORING");
        }

        mViewPager = (CyclicView) findViewById(R.id.pager);
        mViewPager.setChangePositionFactor(4000);
        // TODO: display the score stats thats currently been selected by the user. I.e if the user
        // is scoring on peg 40, then when they select stats, it should should the stats for peg 40.
        mViewPager.setAdapter(new CyclicFragmentAdapter(this, getSupportFragmentManager()) {
            @Override
            protected Fragment createFragment(int i) {
                return StatsOneFragment.newInstance(pegValues[i]);
            }

            @Override
            public int getItemsCount() {
                return 6;
            }
        });
        if (b != null) {
            switch (pegValue) {
                case 40:
                    mViewPager.setCurrentPosition(0);
                    break;
                case 32:
                    mViewPager.setCurrentPosition(1);
                    break;
                case 24:
                    mViewPager.setCurrentPosition(2);
                    break;
                case 36:
                    mViewPager.setCurrentPosition(3);
                    break;
                case 50:
                    mViewPager.setCurrentPosition(4);
                    break;
                case 4:
                    mViewPager.setCurrentPosition(5);
                    break;

                default:
                    mViewPager.setCurrentPosition(0);
                    break;
            }
        }
    }

}

