package com.primewebtech.darts.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.primewebtech.darts.database.model.ActionSchema;
import com.primewebtech.darts.database.model.ScoreSchema;
import com.primewebtech.darts.database.model.ScoreTwoSchema;

/**
 * Created by benebsworth on 27/5/17.
 */

public class ScoreDatabase implements ScoreSchema, ActionSchema{
    private static final String TAG = ScoreDatabase.class.getSimpleName();
    private static final String DATABASE_NAME    = "darts.db";
    private static final int    DATABASE_VERSION = 2;
    private DatabaseHelper mDbHelper;
    private final Context mContext;
    public static ScoreOneDao mScoreOneDoa;
    public static ScoreTwoDao mScoreTwoDoa;
    public static ScoreThreeDao mScoreThreeDoa;
    public static ScoreHundredDao mScoreHundredDoa;
    public static ActionDao mActionDoa;
    public static StatsOneDao mStatsOneDoa;
    public static StatsHundredDao mStatsHundredDoa;
    private SQLiteDatabase mDatabase;


    public ScoreDatabase open() throws SQLException {
        Log.d(TAG, "Opening DB");
        mDbHelper = new DatabaseHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();
        mScoreOneDoa = new ScoreOneDao(mDatabase);
        mScoreTwoDoa = new ScoreTwoDao(mDatabase);
        mScoreThreeDoa = new ScoreThreeDao(mDatabase);
        mScoreHundredDoa = new ScoreHundredDao(mDatabase);
        mActionDoa = new ActionDao(mDatabase);
        mStatsOneDoa = new StatsOneDao(mDatabase);
        mStatsHundredDoa = new StatsHundredDao(mDatabase);


        Log.d(TAG, "completed initialisation");
        return this;
    }
    public void close() {
        mDbHelper.close();
    }
    public void update() {
        mDbHelper.onCreate(mDatabase);
    }


    public ScoreDatabase(Context context) {
        this.mContext = context;
    }



    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creating database if doesnt exist");
            Log.d(TAG, CREATE_SCORE_TABLE_ONE);
            db.execSQL(CREATE_SCORE_TABLE_ONE);
            db.execSQL(ScoreTwoSchema.CREATE_SCORE_TABLE_TWO);
            db.execSQL(CREATE_SCORE_TABLE_THREE);
            db.execSQL(CREATE_SCORE_TABLE_HUNDRED);
            db.execSQL(CREATE_SCORE_TABLE_BEST);
            db.execSQL(CREATE_SCORE_TABLE_BEST_PREVIOUS);
            db.execSQL(CREATE_ACTION_TABLE);
            db.execSQL(deleteActionTrigger());
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            Log.w(TAG, "Upgrading database from version "
                    + oldVersion + " to "
                    + newVersion + " which destroys all old data");

            db.execSQL("DROP TABLE IF EXISTS "
                    + SCORE_TABLE_ONE);
            db.execSQL("DROP TABLE IF EXISTS "
                    + ScoreTwoSchema.SCORE_TABLE_TWO);
            db.execSQL("DROP TABLE IF EXISTS "
                    + SCORE_TABLE_THREE);
            db.execSQL("DROP TABLE IF EXISTS "
                    + SCORE_TABLE_HUNDRED);
            db.execSQL("DROP TABLE IF EXISTS "
                    + SCORE_TABLE_BEST);
            db.execSQL("DROP TABLE IF EXISTS "
                    + SCORE_TABLE_BEST_PREVIOUS);
            db.execSQL("DROP TABLE IF EXISTS "
                    + TODAY_SCORE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS "
                    + ACTION_TABLE);
            db.execSQL("DROP trigger IF EXISTS delete_action");
            onCreate(db);

        }
        public String deleteActionTrigger(){
            Log.d(TAG, "deleteActionTrigger");
            String deleteAction = "CREATE TRIGGER if not exists delete_action " +
                    " AFTER INSERT " +
                    " ON " + ACTION_TABLE +
                    " WHEN (SELECT COUNT(*) FROM " + ACTION_TABLE +") >" + HISTORY_LIMIT +
                    " BEGIN " +
                    "  DELETE FROM " + ACTION_TABLE +
                    "  WHERE " + ActionSchema.ID + " = (select "+ ActionSchema.ID +" from "+ ACTION_TABLE +
                    "  order by "+ ActionSchema.ID +" asc limit 1); "+
                    " END; ";
            Log.d(TAG, "deleteActionTrigger:"+deleteAction);
            return deleteAction;
        }
    }
}
