package com.primewebtech.darts;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.primewebtech.darts.database.ScoreDatabase;
import com.primewebtech.darts.database.model.PegRecord;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.primewebtech.darts.database.model.ScoreSchema.TYPE_2;

/**
 * Created by benebsworth on 15/7/17.
 */
@RunWith(AndroidJUnit4.class)
public class SQLTests {
    public ScoreDatabase mDatabase;
    private static final String TAG = SQLTests.class.getSimpleName();
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
    public void TestAddPegScore() throws Exception {
        // Context of the app under test.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = new ScoreDatabase(appContext);
        mDatabase.open();
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 30 * 6; i++) {
//            int randomDay = 1 + (int)(Math.random() * 27);
//            int randomMonth = 4 + (int)(Math.random() * 4);
//                int randomScore = i % 8;
                int randomScore = 7;
//                int randomPegIndex = (int)(Math.random() * 6); // 0-6
//            String date = String.format("2017-%02d-%02d", randomMonth, randomDay);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -1 * i);
                Date previousDay = cal.getTime();
                System.out.println(dateFormat.format(previousDay));
                System.out.println("PegValue: " + String.valueOf(pegValues[j]));
                System.out.println("PegCount: " + String.valueOf(randomScore));
                PegRecord pegRecord = new PegRecord(dateFormat.format(previousDay), TYPE_2, pegValues[j], randomScore);
                ScoreDatabase.mScoreOneDoa.addPegValue(pegRecord);
            }
        }
    }
        @Test
        public void TestAddPegScoreHundred() throws Exception {
            // Context of the app under test.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Context appContext = InstrumentationRegistry.getTargetContext();
            mDatabase = new ScoreDatabase(appContext);
            mDatabase.open();
            for (int j = 0; j < 3; j++ ){
                for (int i = 0; i < 30 * 6; i++) {
//            int randomDay = 1 + (int)(Math.random() * 27);
//            int randomMonth = 4 + (int)(Math.random() * 4);
//                    int randomScore = i % 8;
                    int randomScore = 7;
//                int randomPegIndex = (int)(Math.random() * 6); // 0-6
//            String date = String.format("2017-%02d-%02d", randomMonth, randomDay);
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, -1 * i);
                    Date previousDay = cal.getTime();
                    System.out.println(dateFormat.format(previousDay));
                    System.out.println("PegValue: "+String.valueOf(pegValuesHundred[j]));
                    System.out.println("PegCount: "+String.valueOf(randomScore));
                    PegRecord pegRecord = new PegRecord(dateFormat.format(previousDay), TYPE_2, pegValuesHundred[j], randomScore);
                    ScoreDatabase.mScoreHundredDoa.addPegValue(pegRecord);
                }
            }


    }
//    @Test
//    public void TestAddPegScoreHundred() throws Exception {
//        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getTargetContext();
//        mDatabase = new ScoreDatabase(appContext);
//        mDatabase.open();
//        for (int i = 0; i < 50; i++) {
//            int randomDay = 1 + (int)(Math.random() * 27);
//            int randomMonth = 4 + (int)(Math.random() * 4);
//            int randomScore = (int)(Math.random() * 200);
//            int randomPegIndex = (int)(Math.random() * 3); // 0-6
//            String date = String.format("2017-%02d-%02d", randomMonth, randomDay);
//            System.out.println(date);
//            System.out.println("PegValue: "+String.valueOf(pegValuesHundred[randomPegIndex]));
//            System.out.println("PegCount: "+String.valueOf(randomScore));
//            PegRecord pegRecord = new PegRecord(date, TYPE_2, pegValuesHundred[randomPegIndex], randomScore);
//            ScoreDatabase.mScoreHundredDoa.addPegValue(pegRecord);
//        }
//
//    }
    @Test
    public void TestgetPreviousWeek() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = new ScoreDatabase(appContext);
        mDatabase.open();
        for (int i = 1; i <= 6; i++) {
            HashMap<String, String> previousWeek = ScoreDatabase.mStatsOneDoa.getPreviousWeek(i);
            System.out.println(previousWeek.toString());
        }
    }

    @Test
    public void TestgetPreviousMonth() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = new ScoreDatabase(appContext);
        mDatabase.open();
        for (int i = 1;i <= 6; i++) {
            HashMap<String,String> previousMonth = ScoreDatabase.mStatsOneDoa.getPreviousMonth(i);
            System.out.println(previousMonth.toString());
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
    public void TestgetHighestPreviousScoreWeek() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = new ScoreDatabase(appContext);
        mDatabase.open();
        int previousScore = ScoreDatabase.mStatsOneDoa.getPreviousScore(40, "WEEK", 3);
        System.out.println("PreviousScore[week]: "+String.valueOf(previousScore));
    }
    @Test
    public void TestgetHighestPreviousScoreMonth() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = new ScoreDatabase(appContext);
        mDatabase.open();
        int previousScore = ScoreDatabase.mStatsOneDoa.getPreviousScore(40, "MONTH", 1);
        System.out.println("PreviousScore[month]: "+String.valueOf(previousScore));
    }


}
