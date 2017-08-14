package com.primewebtech.darts.statistics.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.primewebtech.darts.R;
import com.primewebtech.darts.database.ScoreDatabase;
import com.primewebtech.darts.statistics.StatsOneActivity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by benebsworth on 17/6/17.
 */

public class StatsOneFragmentSummary extends Fragment {

    private static final String TAG = StatsOneFragmentSummary.class.getSimpleName();
    public int[] mStatsRow40 = {
            R.id.row_40_d,
            R.id.row_40_w,
            R.id.row_40_m,
    };
    public int[] mStatsRow32 = {
            R.id.row_32_d,
            R.id.row_32_w,
            R.id.row_32_m,
    };
    public int[] mStatsRow24 = {
            R.id.row_24_d,
            R.id.row_24_w,
            R.id.row_24_m,
    };
    public int[] mStatsRow36 = {
            R.id.row_36_d,
            R.id.row_36_w,
            R.id.row_36_m,
    };
    public int[] mStatsRow50 = {
            R.id.row_50_d,
            R.id.row_50_w,
            R.id.row_50_m,
    };
    public int[] mStatsRow4 = {
            R.id.row_4_d,
            R.id.row_4_w,
            R.id.row_4_m,
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

    public int[] mRowPegValues = {
            R.id.row_40,
            R.id.row_32,
            R.id.row_24,
            R.id.row_36,
            R.id.row_50,
            R.id.row_4,
    };
    private String type;

    public static StatsOneFragmentSummary newInstance() {
        StatsOneFragmentSummary statsOneFragment = new StatsOneFragmentSummary();
        Bundle args = new Bundle();
        args.putString("type", "one");
        statsOneFragment.setArguments(args);
        return statsOneFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getString("type");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats_one_summary, container, false);
        TypedValue outValue1000 = new TypedValue();
        getResources().getValue(R.dimen.stats_summary_bubble_pegValue_text_1000,outValue1000, true);
        mStatsRows = new ArrayList<>();
        mStatsRows.add(mStatsRow40);
        mStatsRows.add(mStatsRow32);
        mStatsRows.add(mStatsRow24);
        mStatsRows.add(mStatsRow36);
        mStatsRows.add(mStatsRow50);
        mStatsRows.add(mStatsRow4);
        for (int i = 0; i<6; i++) {
            final int j = i;
            TextView rowPegValue = (TextView) rootView.findViewById(mRowPegValues[j]);
            rowPegValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent statsIntent;
                    statsIntent = new Intent(getActivity(), StatsOneActivity.class);
                    Bundle b = new Bundle();
                    b.putString("type", String.valueOf(pegValues[j]));
                    statsIntent.putExtras(b);
                    startActivity(statsIntent);
                }
            });
        }

        int index = 0;
        for ( int[] statRow : mStatsRows) {


            TextView statsScoreDay = (TextView) rootView.findViewById(statRow[0]);
            TextView statsScoreWeek = (TextView) rootView.findViewById(statRow[1]);
            TextView statsScoreMonth = (TextView) rootView.findViewById(statRow[2]);
            int dailyScore = ScoreDatabase.mStatsOneDoa.getTotalPegCountDay(pegValues[index]);
            int weeklyScore = ScoreDatabase.mStatsOneDoa.getTotalPegCountWeek(pegValues[index]);
            int monthlyScore = ScoreDatabase.mStatsOneDoa.getTotalPegCountMonth(pegValues[index]);
            int dailyPBscore = ScoreDatabase.mStatsOneDoa.getHighestScore(pegValues[index], "DAY");
            int weeklyPBscore = ScoreDatabase.mStatsOneDoa.getHighestScore(pegValues[index], "WEEK");
            int monthlyPBscore = ScoreDatabase.mStatsOneDoa.getHighestScore(pegValues[index], "MONTH");

            if (dailyScore >= 1000) {
                statsScoreDay.setTextSize(outValue1000.getFloat());

            }
            if (weeklyScore >= 1000) {
                statsScoreWeek.setTextSize(outValue1000.getFloat());

            }
            if (monthlyScore >= 1000) {
                statsScoreMonth.setTextSize(outValue1000.getFloat());
            }
            //Set Day element:
            statsScoreDay.setText(String.format(Locale.getDefault(),"%d", dailyScore));
            statsScoreDay.setTextColor(Color.BLACK);
            if (dailyScore >= dailyPBscore) {
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
            if (weeklyScore >= weeklyPBscore) {
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
            if (monthlyScore >= monthlyPBscore) {
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
