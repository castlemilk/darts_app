package com.primewebtech.darts;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.primewebtech.darts.database.ScoreDatabase;
import com.primewebtech.darts.database.model.PegRecord;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static com.primewebtech.darts.database.model.ScoreSchema.TYPE_2;

/**
 * Created by benebsworth on 15/7/17.
 */
@RunWith(AndroidJUnit4.class)
public class SQLTestsGeneratePreviousDays {
    private static final String TAG = SQLTestsGeneratePreviousDays.class.getSimpleName();
    public ScoreDatabase mDatabase;
    final int pegValues[] = {
            40,
            32,
            24,
            36,
            50,
            4
    };
    final int pegValuesHundred[] = {
            100,
            140,
            180
    };
    @Test
    public void TestAddPegScoreWithinPreviousDays() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = new ScoreDatabase(appContext);
        mDatabase.open();
        for ( int pegValue : pegValues) {
            for (int i = 0; i < 6; i++) {
                int randomDay = 1 + (int) (Math.random() * 5);
                int randomMonth = 4 + (int) (Math.random() * 4);
                int randomScore = (int) (Math.random() * 200);
                int randomPegIndex = (int) (Math.random() * 6); // 0-6
                String date = String.format(Locale.ENGLISH, "2017-07-%02d", 29 - randomDay);
                System.out.println(date);
                System.out.println("PegValue: " + String.valueOf(pegValues[randomPegIndex]));
                System.out.println("PegCount: " + String.valueOf(randomScore));
                PegRecord pegRecord = new PegRecord(date, TYPE_2, pegValue, randomScore);
                ScoreDatabase.mScoreOneDoa.addPegValue(pegRecord);
            }
        }

    }
    @Test
    public void TestAddPegScoreHundred() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = new ScoreDatabase(appContext);
        mDatabase.open();
        for (int i = 0; i < 50; i++) {
            int randomDay = 1 + (int)(Math.random() * 27);
            int randomMonth = 4 + (int)(Math.random() * 4);
            int randomScore = (int)(Math.random() * 200);
            int randomPegIndex = (int)(Math.random() * 3); // 0-6
            String date = String.format("2017-%02d-%02d", randomMonth, randomDay);
            System.out.println(date);
            System.out.println("PegValue: "+String.valueOf(pegValuesHundred[randomPegIndex]));
            System.out.println("PegCount: "+String.valueOf(randomScore));
            PegRecord pegRecord = new PegRecord(date, TYPE_2, pegValuesHundred[randomPegIndex], randomScore);
            ScoreDatabase.mScoreHundredDoa.addPegValue(pegRecord);
        }

    }

    @Test
    public void TestgetHighestPreviousScoreDay() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = new ScoreDatabase(appContext);
        mDatabase.open();
        int previousScore = ScoreDatabase.mStatsOneDoa.getPreviousScore(40, "DAY", 1);
        System.out.println("PreviousScore[day]: "+String.valueOf(previousScore));
    }
    @Test
    public void TestgetPreviousDay() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = new ScoreDatabase(appContext);
        mDatabase.open();
        for (int i = 1; i <= 6; i++) {
            String previousDay = ScoreDatabase.mStatsOneDoa.getPreviousDay(i);
            int previousScore = ScoreDatabase.mStatsOneDoa.getPreviousScore(40, "DAY", i);
            Log.d(TAG, "TestgetPreviousDay:"+previousDay+"index: "+i+"score: "+previousScore);
        }
    }


}
