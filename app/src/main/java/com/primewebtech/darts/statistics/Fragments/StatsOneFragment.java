package com.primewebtech.darts.statistics.Fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.primewebtech.darts.R;
import com.primewebtech.darts.database.ScoreDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by benebsworth on 17/6/17.
 */

public class StatsOneFragment extends Fragment {

    private static final String TAG = StatsOneFragment.class.getSimpleName();
    private Typeface tf_ios;
    private Typeface tf_pegValue;

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
    public int[] mStatsPB = {
            R.id.best_score_daily,
            R.id.best_score_weekly,
            R.id.best_score_monthly,
    };
    public int[] mStatsCurrentPeriod = {
            R.id.score_today_total,
            R.id.score_week_total,
            R.id.score_month_total,
    };
    public ArrayList<int[]> mStatsRows;
    String[] periods = {
            "DAY",
            "WEEK",
            "MONTH",
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
        tf_ios = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "fonts/ios_reg.ttf");
        tf_pegValue = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "fonts/arial.ttf");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats_one, container, false);

        mStatsRows = new ArrayList<>();
        mStatsRows.add(mStatsRowLD);
        mStatsRows.add(mStatsRowLW);
        mStatsRows.add(mStatsRowLM);
        HashMap<String, ArrayList<Integer>> scoreMap = new HashMap<>();
        int period_index = 0;
        for ( int[] statRow : mStatsRows) {
            // iterate over day, week then month
            int previous_period_index = 1; //this is the first previous period
            ArrayList<Integer> scores = new ArrayList<>();
            for ( int resourceID : statRow) {
                // iterate over the 6 items for each period
                TextView rowNode = (TextView) rootView.findViewById(resourceID);
//                int previousInex = previous_period_index + 1;
                Log.d(TAG, "previous_period_index:"+previous_period_index);
                int previousScore = ScoreDatabase.mStatsOneDoa.getPreviousScore(pegValue,
                        periods[period_index], previous_period_index);
                scores.add(previousScore);
                if (previousScore > 1000) {
                    rowNode.setTextSize(12);
                }
                rowNode.setText(String.format(Locale.getDefault(),"%d", previousScore));

                previous_period_index++;
            }
            scoreMap.put(periods[period_index], scores);
            period_index++;
        }
        // POST PAINT ACTIVITY: we now determine what needs to be highlighted as PB's. This is to
        // save us re-evaluating any SQL or building more SQL functions etc. could be a TODO.
        Log.d(TAG, "scoreMap: "+scoreMap.toString());
        period_index = 0;
        for ( int[] statRow : mStatsRows) {
            int previous_period_index = 0; //this is the first previous period
            int allTimeHighestScoreForPeriodToday = ScoreDatabase.mStatsOneDoa
                    .getHighestScore(pegValue, periods[period_index]);
            for ( int resourceID : statRow) {
                TextView rowNode = (TextView) rootView.findViewById(resourceID);
                int previousScore = scoreMap.get(periods[period_index]).get(previous_period_index);
                Log.d(TAG, "Painting:white:previousScore:"+previousScore);
                Log.d(TAG, "Painting:white:period"+periods[period_index]);
                Log.d(TAG, "Painting:white:alltimehighest:" + allTimeHighestScoreForPeriodToday);
                if (previousScore >= allTimeHighestScoreForPeriodToday &&
                        previousScore > 0) {

                    rowNode.setBackground(
                            getResources().getDrawable(R.drawable.peg_stats_score_background_white));
                    rowNode.setTextColor(Color.BLACK);
                } else {
                    rowNode.setBackground(
                            getResources().getDrawable(R.drawable.peg_stats_score_background));
                    rowNode.setTextColor(Color.WHITE);
                }
                rowNode.setTypeface(tf_ios);

                previous_period_index++;
            }
            period_index++;
        }

        // DAILY BEST EVER SCORE
        int k = 0;
        for ( String period : periods) {
            int bestScore = ScoreDatabase.mStatsOneDoa.getHighestScore(pegValue, period);
            int currentScore = ScoreDatabase.mStatsOneDoa.getCurrentScore(pegValue, period);
            TextView currentPeriodNode = (TextView) rootView.findViewById(mStatsCurrentPeriod[k]);
            TextView bestNode = (TextView) rootView.findViewById(mStatsPB[k]);
            bestNode.setText(String.valueOf(bestScore));
            currentPeriodNode.setText(String.valueOf(currentScore));
            if ( currentScore >= bestScore && currentScore > 0) {
                currentPeriodNode.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background_white));
                currentPeriodNode.setTextColor(Color.BLACK);
            } else {
                currentPeriodNode.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background));
                currentPeriodNode.setTextColor(Color.WHITE);
            }
            if (bestScore >= 0) {
                bestNode.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background_white));
                bestNode.setTextColor(Color.BLACK);
            } else {
                bestNode.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background));
                bestNode.setTextColor(Color.WHITE);
            }
            bestNode.setTypeface(tf_ios);
            currentPeriodNode.setTypeface(tf_ios);
            k++;
        }
        TextView pegValueIndicator = (TextView) rootView.findViewById(R.id.peg_value_indicator);
        pegValueIndicator.setTypeface(tf_pegValue);
        pegValueIndicator.setText(String.valueOf(pegValue));
        TextView pegValueTotal = (TextView) rootView.findViewById(R.id.stats_total_peg_count);
        int totalPegCount = ScoreDatabase.mStatsOneDoa.getTotalPegCount(pegValue);
        if (totalPegCount > 1000) {
            pegValueTotal.setTextSize(12);

        } else {
            pegValueTotal.setTextSize(18);
        }
        pegValueTotal.setText(String.valueOf(totalPegCount));


        return rootView;
    }
}
