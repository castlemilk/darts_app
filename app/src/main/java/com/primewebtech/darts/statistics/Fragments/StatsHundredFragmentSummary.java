package com.primewebtech.darts.statistics.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.primewebtech.darts.R;
import com.primewebtech.darts.database.ScoreDatabase;
import com.primewebtech.darts.statistics.StatsHundredActivity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by benebsworth on 18/6/17.
 */

public class StatsHundredFragmentSummary extends Fragment {

    private static final String TAG = StatsHundredFragmentSummary.class.getSimpleName();
    private Typeface tf_ios;
    private Typeface tf_pegValue;
    public int[] mStatsRow100 = {
            R.id.row_100_d,
            R.id.row_100_w,
            R.id.row_100_m,
    };
    public int[] mStatsRow140 = {
            R.id.row_140_d,
            R.id.row_140_w,
            R.id.row_140_m,
    };
    public int[] mStatsRow180 = {
            R.id.row_180_d,
            R.id.row_180_w,
            R.id.row_180_m,
    };
    public int[] mRowPegValues = {
            R.id.row_100,
            R.id.row_140,
            R.id.row_180,
    };
    public ArrayList<int[]> mStatsRows;
    int[] pegValues = {
        100,
        140,
        180,
    };
    private String type;
    public static StatsHundredFragmentSummary newInstance() {
        StatsHundredFragmentSummary statsHundredFragmentSummary = new StatsHundredFragmentSummary();
        Bundle args = new Bundle();
        args.putString("type", "one");
        statsHundredFragmentSummary.setArguments(args);
        return statsHundredFragmentSummary;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getString("type");
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats_hundred_summary, container, false);
        tf_ios = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "fonts/ios_reg.ttf");
        tf_pegValue = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "fonts/arial.ttf");
        TypedValue outValue1000 = new TypedValue();
        getResources().getValue(R.dimen.stats_summary_bubble_pegValue_text_1000,outValue1000, true);

        for (int i = 0; i<3; i++) {
            final int j = i;
            TextView rowPegValue = (TextView) rootView.findViewById(mRowPegValues[j]);
            rowPegValue.setTypeface(tf_pegValue);
            rowPegValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent statsIntent;
                    statsIntent = new Intent(getActivity(), StatsHundredActivity.class);
                    Bundle b = new Bundle();
                    b.putInt("PEG_VALUE",pegValues[j]);
                    statsIntent.putExtras(b);
                    startActivity(statsIntent);
                }
            });
        }

        mStatsRows = new ArrayList<>();
        mStatsRows.add(mStatsRow100);
        mStatsRows.add(mStatsRow140);
        mStatsRows.add(mStatsRow180);
        int index = 0;
        for ( int[] statRow : mStatsRows) {
            TextView statsScoreDay = (TextView) rootView.findViewById(statRow[0]);
            TextView statsScoreWeek = (TextView) rootView.findViewById(statRow[1]);
            TextView statsScoreMonth = (TextView) rootView.findViewById(statRow[2]);
//            statsScoreDay.setTypeface(tf_ios);
//            statsScoreWeek.setTypeface(tf_ios);
//            statsScoreMonth.setTypeface(tf_ios);
            int dailyScore = ScoreDatabase.mStatsHundredDoa.getTotalPegCountDay(pegValues[index]);
            int weeklyScore = ScoreDatabase.mStatsHundredDoa.getTotalPegCountWeek(pegValues[index]);
            int monthlyScore = ScoreDatabase.mStatsHundredDoa.getTotalPegCountMonth(pegValues[index]);
            int dailyPBscore = ScoreDatabase.mStatsHundredDoa.getHighestScore(pegValues[index], "DAY");
            int weeklyPBscore = ScoreDatabase.mStatsHundredDoa.getHighestScore(pegValues[index], "WEEK");
            int monthlyPBscore = ScoreDatabase.mStatsHundredDoa.getHighestScore(pegValues[index], "MONTH");
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
            if (dailyScore >= dailyPBscore && dailyScore > 0) {
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
            if (weeklyScore >= weeklyPBscore && weeklyScore > 0) {
                statsScoreWeek.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background_white));
                statsScoreWeek.setTextColor(Color.BLACK);
            } else {
                statsScoreWeek.setBackground(
                        getResources().getDrawable(R.drawable.peg_stats_score_background));
                statsScoreWeek.setTextColor(Color.WHITE);
            }
            //Set Month element:
            statsScoreMonth.setText(String.format(Locale.getDefault(),"%d",monthlyScore ));
            if (monthlyScore >= monthlyPBscore && monthlyScore > 0) {
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
