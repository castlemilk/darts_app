package com.primewebtech.darts;

import android.content.SharedPreferences;
import android.util.Log;

import com.primewebtech.darts.database.ScoreDatabase;
import com.primewebtech.darts.database.model.PegRecord;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by benebsworth on 27/5/17.
 */

public class MainApplication extends android.app.Application {
    private static final String TAG = MainApplication.class.getSimpleName();
    private static MainApplication singleton = null;
    public ScoreDatabase mDatabase;
    SharedPreferences sharedPreferences = null;
    private int[] mPegs = {
            40, 32, 24,36,50,2
    };
    public static MainApplication getInstance() {
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("com.primewebtech.darts", MODE_PRIVATE);
        // Do first run stuff here then set 'firstrun' as false
        // using the following line to edit/commit prefs
        Log.i(TAG,"first_run:initialising DB");
        mDatabase = new ScoreDatabase(this);
        mDatabase.open();
        if (sharedPreferences.getBoolean("firstrun", true)) {
            Log.i(TAG, "FIRST_RUN:initialising system");
            initialisePegCounts();
            sharedPreferences.edit().putBoolean("firstrun", false).apply();
        }

        singleton = this;
    }

    public void initialisePegCounts() {
        for (int peg : mPegs) {
            PegRecord pegRecord = new PegRecord(getDate(), 0, peg, 0);
            try {
                ScoreDatabase.mScoreDoa.addTodayPegValue(pegRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }


    @Override
    public void onTerminate() {
        mDatabase.close();
        super.onTerminate();
    }
}
