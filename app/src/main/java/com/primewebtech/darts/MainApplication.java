package com.primewebtech.darts;

import android.util.Log;

import com.primewebtech.darts.database.ScoreDatabase;

/**
 * Created by benebsworth on 27/5/17.
 */

public class MainApplication extends android.app.Application {
    private static final String TAG = MainApplication.class.getSimpleName();
    private static MainApplication singleton = null;
    public ScoreDatabase mDatabase;
    public static MainApplication getInstance() {
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new ScoreDatabase(this);
        mDatabase.open();
        Log.i(TAG,"initialised objects..");
        singleton = this;
    }

    @Override
    public void onTerminate() {
        mDatabase.close();
        super.onTerminate();
    }
}
