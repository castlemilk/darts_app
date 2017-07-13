package com.primewebtech.darts.statistics.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.primewebtech.darts.R;
import com.primewebtech.darts.database.ScoreDatabase;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by benebsworth on 17/6/17.
 */

public class StatsOneFragment extends Fragment {

    private static final String TAG = StatsOneFragment.class.getSimpleName();

    public int[] mStatsRowLD = {
            R.id.row_ld_2,
            R.id.row_ld_3,
            R.id.row_ld_4,
            R.id.row_ld_5,
            R.id.row_ld_6,
    };

    public int[] mStatsRowLW = {
            R.id.row_lw_2,
            R.id.row_lw_3,
            R.id.row_lw_4,
            R.id.row_lw_5,
            R.id.row_lw_6,
    };

    public int[] mStatsRowLM = {
            R.id.row_lm_2,
            R.id.row_lm_3,
            R.id.row_lm_4,
            R.id.row_lm_5,
            R.id.row_lm_6,
    };
    public ArrayList<int[]> mStatsRows;


    int[] pegValues = {
            40,
            32,
            24,
            36,
            50,
            4
    };
    private String type;
    private int pegValue;

    public static StatsOneFragment newInstance(int pegValue) {
        StatsOneFragment statsOneFragment = new StatsOneFragment();
        Bundle args = new Bundle();
        args.putInt("PEG_VALUE", pegValue);
        statsOneFragment.setArguments(args);
        return statsOneFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getString("type");
        pegValue = getArguments().getInt("PEG_VALUE");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats_one, container, false);

        mStatsRows = new ArrayList<>();
        mStatsRows.add(mStatsRowLD);
        mStatsRows.add(mStatsRowLW);
        mStatsRows.add(mStatsRowLM);
        int index = 0;
        for ( int[] statRow : mStatsRows) {


            TextView statsScorePosOne = (TextView) rootView.findViewById(statRow[0]);
            TextView statsScorePosTwo = (TextView) rootView.findViewById(statRow[1]);
            TextView statsScorePosThree = (TextView) rootView.findViewById(statRow[2]);
            TextView statsScorePosFour = (TextView) rootView.findViewById(statRow[3]);
            TextView statsScorePosFive = (TextView) rootView.findViewById(statRow[4]);
            TextView statsScorePosSix = (TextView) rootView.findViewById(statRow[5]);
            int last_day_index = 0; //this is the first previous day
            for ( int resourceID : statRow) {
                TextView rowNode = (TextView) rootView.findViewById(resourceID);
                int previousScore = ScoreDatabase.mStatsOneDoa.getPreviousScore(pegValue, last_day_index);
                if (previousScore > 1000) {
                    rowNode.setTextSize(12);
                }
            }
            //Set Day element:
            statsScoreDay.setText(String.format(Locale.getDefault(),"%d", dailyScore));
            statsScoreDay.setTextColor(Color.BLACK);
            if (dailyScore != 0) {
                statsScoreDay.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background_white));
                statsScoreDay.setTextColor(Color.BLACK);
            } else {
                statsScoreDay.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background));
                statsScoreDay.setTextColor(Color.WHITE);
            }

            //Set Week element:
            statsScoreWeek.setText(String.format(Locale.getDefault(),"%d",
                    weeklyScore));
            if (weeklyScore != 0) {
                statsScoreWeek.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background_white));
                statsScoreWeek.setTextColor(Color.BLACK);
            } else {
                statsScoreWeek.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background));
                statsScoreWeek.setTextColor(Color.WHITE);
            }
            //Set Month element:
            statsScoreMonth.setText(String.format(Locale.getDefault(),"%d",monthlyScore));
            if (weeklyScore != 0) {
                statsScoreMonth.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background_white));
                statsScoreMonth.setTextColor(Color.BLACK);
            } else {
                statsScoreMonth.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background));
                statsScoreMonth.setTextColor(Color.WHITE);
            }

            index++;
        }

        return rootView;
    }
}
