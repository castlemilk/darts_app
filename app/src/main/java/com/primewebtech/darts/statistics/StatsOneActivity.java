package com.primewebtech.darts.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.primewebtech.darts.R;
import com.primewebtech.darts.database.ScoreDatabase;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by benebsworth on 4/6/17.
 */

public class StatsOneActivity extends AppCompatActivity {
    /**
     * Shows the statistics for the one dart activity scoring mode. With the maximum value obtained
     * over the periods of Day, Week and Month.
     */
    private static final String TAG = StatsOneActivity.class.getSimpleName();
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
    public int[] mStatsRow2 = {
            R.id.row_2_d,
            R.id.row_2_w,
            R.id.row_2_m,
    };

    public ArrayList<int[]> mStatsRows;
    public int[] mStatsDay = {
            R.id.row_40_d,
            R.id.row_32_d,
            R.id.row_24_d,
            R.id.row_36_d,
            R.id.row_50_d,
            R.id.row_2_d,
    };

    public int[] mStatsWeek = {
            R.id.row_40_w,
            R.id.row_32_w,
            R.id.row_24_w,
            R.id.row_36_w,
            R.id.row_50_w,
            R.id.row_2_w,
    };

    public int[] mStatsMonth = {
            R.id.row_40_m,
            R.id.row_32_m,
            R.id.row_24_m,
            R.id.row_36_m,
            R.id.row_50_m,
            R.id.row_2_m,
    };

    public int[] mScoreNodes = {
            R.id.row_40_d,
            R.id.row_40_w,
            R.id.row_40_m,
            R.id.row_32_d,
            R.id.row_32_w,
            R.id.row_32_m,
            R.id.row_24_d,
            R.id.row_24_w,
            R.id.row_24_m,
            R.id.row_36_d,
            R.id.row_36_w,
            R.id.row_36_m,
            R.id.row_50_d,
            R.id.row_50_w,
            R.id.row_50_m,
            R.id.row_2_d,
            R.id.row_2_w,
            R.id.row_2_m,
    };
    int[] pegValues = {
            40,
            32,
            24,
            36,
            50,
            2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_one);
        mStatsRows = new ArrayList<>();
        mStatsRows.add(mStatsRow40);
        mStatsRows.add(mStatsRow32);
        mStatsRows.add(mStatsRow24);
        mStatsRows.add(mStatsRow36);
        mStatsRows.add(mStatsRow50);
        mStatsRows.add(mStatsRow2);
//        TextView statsScoreDay = (TextView) findViewById(R.id.row_40_d);
//        statsScoreDay.setText(
//                String.format(Locale.getDefault(),"%d",
//                        ScoreDatabase.mStatsOneDoa.getTotalPegCountDay(40)));
        int index = 0;
        for ( int[] statRow : mStatsRows) {


            TextView statsScoreDay = (TextView) findViewById(statRow[0]);
            statsScoreDay.setText(String.format(Locale.getDefault(),"%d",
                    ScoreDatabase.mStatsOneDoa.getTotalPegCountDay(pegValues[index])));
            statsScoreDay.setBackground(
                    getResources().getDrawable(R.drawable.peg_stats_score_background_white));
            statsScoreDay.setTextColor(Color.BLACK);

            TextView statsScoreWeek = (TextView) findViewById(statRow[1]);
            statsScoreWeek.setText(String.format(Locale.getDefault(),"%d",
                    ScoreDatabase.mStatsOneDoa.getTotalPegCountWeek(pegValues[index])));
            statsScoreWeek.setBackground(
                    getResources().getDrawable(R.drawable.peg_stats_score_background_white));
            statsScoreWeek.setTextColor(Color.BLACK);

            TextView statsScoreMonth = (TextView) findViewById(statRow[2]);
            statsScoreMonth.setText(String.format(Locale.getDefault(),"%d",
                    ScoreDatabase.mStatsOneDoa.getTotalPegCountMonth(pegValues[index])));
            statsScoreMonth.setBackground(
                    getResources().getDrawable(R.drawable.peg_stats_score_background_white));
            statsScoreMonth.setTextColor(Color.BLACK);

            index++;
        }

        Log.d(TAG, "todayPegValue:"+ScoreDatabase.mStatsOneDoa.getTotalPegCountDay(40));
        Log.d(TAG, "weekPegValue:"+ScoreDatabase.mStatsOneDoa.getTotalPegCountWeek(40));
        Log.d(TAG, "monthPegValue:"+ScoreDatabase.mStatsOneDoa.getTotalPegCountMonth(40));


    }





}
