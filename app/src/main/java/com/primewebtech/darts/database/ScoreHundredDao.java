package com.primewebtech.darts.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.primewebtech.darts.database.model.Action;
import com.primewebtech.darts.database.model.ActionSchema;
import com.primewebtech.darts.database.model.PegRecord;
import com.primewebtech.darts.database.model.ScoreSchema;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by benebsworth on 27/5/17.
 */

public class ScoreHundredDao extends DatabaseContentProvider implements ScoreSchema {
    /**
     * ScoreHundredDao is the interface between the application and the score tables for the Hundred
     * scoring mode. The logic between the different scoring modes is fairly homogenous and could
     * definitely be consolidated into a singular interface that handles the content of the scoring
     * mode.
     * TODO: Consolidate scoring Doa into singular class/interface which handles scoring content.
     */

    private static final String TAG = ScoreHundredDao.class.getSimpleName();

    private Cursor cursor;

    protected String getScoreTableName() {
        return SCORE_TABLE_HUNDRED;
    }

    public ScoreHundredDao(SQLiteDatabase database) {
        super(database);
    }


    /** Fetches a given score record based on a scoreRecord object and then makes the required updates
     * effectively fetching a cursor position based off the scoreRecord and replacing with the passed
     * pegRecord.
     * @param scoreRecord PegRecord object that we would like to update.
     * @return Boolean repesenting the success/failure of row update.
     */
    public boolean updateTodayPegValue(PegRecord scoreRecord) throws IOException {
        if (scoreRecord != null) {
            String selector = PEG_VALUE_WHERE+ " AND "+ DATE_WHERE + " AND "+ TYPE_WHERE;
            String selectorArgs[] = new String[]{String.valueOf(scoreRecord.getPegValue()),
                    getDateNow(), String.valueOf(scoreRecord.type)};
            return super.update(getScoreTableName(), setContentValues(scoreRecord), selector,
                    selectorArgs) > 0;
        } else {
            return false;
        }
    }

    /** Adds a peg value. But first checks to see if a row exists for the day and if so then we
     * carry out and update instead of an insert.
     * @param scoreRecord PegRecord object that we would like to add
     * @return Boolean repesenting the success/failure of row update.
     */
    public boolean addTodayPegValue(PegRecord scoreRecord) throws IOException {
        Log.d(TAG, "addPegValue:"+scoreRecord.toString());
        if (getTodayPegValue(scoreRecord.pegValue, scoreRecord.type) != null) {
            return updateTodayPegValue(scoreRecord);
        } else {
            return super.insert(getScoreTableName(), setContentValues(scoreRecord)) > 0;
        }
    }
    /** Adds a peg value to Score mode hundred table.
     * @param scoreRecord PegRecord object that we would like to add
     * @return Boolean repesenting the success/failure of row update.
     */
    public boolean addPegValue(PegRecord scoreRecord) throws IOException {
        Log.d(TAG, "addPegValue:" + scoreRecord.toString());
        return super.insert(getScoreTableName(), setContentValues(scoreRecord)) > 0;
    }
    /** Adds a peg value to Score mode hundred table.
     * @return String representing the date in the format yyyy-MM-dd
     */
    public String getDateNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }
    /** increases a given pegvalue by a specified increment
     * @param pegValue Integer - the pegValue to increment (i.e peg 40, 50 etc)
     * @param type Integer - score type i.e 2 dart or 3 dart
     * @param increment Integer - the amount to increment a given pegValue by.
     * @return Boolean repesenting the success/failure of row update.
     */
    public boolean increaseTodayPegValue(int pegValue, int type, int increment) {
        Log.d(TAG, "increaseTodayPegValue:pegValue:"+pegValue);
        Log.d(TAG, "increaseTodayPegValue:increment:"+increment);
        PegRecord pegRecord = getTodayPegValue(pegValue, type);
        final String selection = PEG_VALUE_WHERE+ " AND "+ DATE_WHERE + " AND " + TYPE_WHERE;
        final String selectionArgs[] = { String.valueOf(pegValue),
                getTodaysDate(), String.valueOf(type)};

        if (pegRecord != null) {
            Log.d(TAG, "increaseTodayPegValue:currentPegCount:"+pegRecord.getPegCount());
            ContentValues contentValues = new ContentValues();
            contentValues.put(PEG_COUNT, pegRecord.getPegCount()+increment);
            contentValues.put(LAST_MODIFIED, getDateNow());
            return super.update(getScoreTableName(), contentValues, selection,
                    selectionArgs) > 0;
        } else {
            return false;
        }
    }
    /** decreases a given pegvalue by a specified decrement
     * @param pegValue Integer - the pegValue to increment (i.e peg 40, 50 etc)
     * @param type Integer - score type i.e 2 dart or 3 dart
     * @param decrement Integer - the amount to decrement a given pegValue by.
     * @return Boolean repesenting the success/failure of row update.
     */
    public boolean decreaseTodayPegValue(int pegValue, int type, int decrement) {
        Log.d(TAG, "decreaseTodayPegValue:pegValue:"+pegValue);
        Log.d(TAG, "decreaseTodayPegValue:decrement:"+decrement);
        PegRecord pegRecord = getTodayPegValue(pegValue, type);
        String selector = PEG_VALUE_WHERE+ " AND "+ DATE_WHERE+ "AND " + TYPE_WHERE;
        String selectorArgs[] = new String[]{String.valueOf(pegValue), getDateNow(), String.valueOf(type)};

        if (pegRecord != null) {
            Log.d(TAG, "increaseTodayPegValue:currentPegCount:"+pegRecord.getPegCount());
            ContentValues contentValues = new ContentValues();
            contentValues.put(PEG_COUNT, pegRecord.getPegCount()-decrement);
            contentValues.put(LAST_MODIFIED, getDateNow());
            return super.update(getScoreTableName(), contentValues,selector,
                   selectorArgs) > 0;
        } else {
            return false;
        }
    }
    /** Implements a rollback based on the given action.
     * @param action Action - The action that we are rolling back.
     * @return Boolean repesenting the success/failure of rollback.
     */
    public boolean rollbackScore(Action action) {
        if (action.actionType == ActionSchema.ADD) {
            Log.d(TAG, "rollbackScore:DECREASING:"+action.toString());
            return decreaseTodayPegValue(action.pegValue, action.type, action.actionValue);
        } else {
            Log.d(TAG, "rollbackScore:INCREASING:"+action.toString());
            return increaseTodayPegValue(action.pegValue, action.type, action.actionValue);
        }

    }

    /***
     * Aggregate total peg counts over all time for a given peg value.
     * DON't need to differentiate here between TYPE_2 and TYPE_3 scores.
     * @param pegValue
     * @return Integer representing the total peg count for a given pegValue.
     */
    public int getTotalPegCount(int pegValue) {
        final String selectionArgs[] =  {String.valueOf(pegValue)};
        Log.d(TAG, "getTotalPegCount:pegValue:"+pegValue);
        cursor = super.rawQuery("select sum(" + PEG_COUNT + ") from " + SCORE_TABLE_HUNDRED +
                " WHERE " + PEG_VALUE_WHERE + ";", selectionArgs);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int total = cursor.getInt(0);
                Log.d(TAG, "getTotalPegCount:total:"+total);
                cursor.close();
                return total;
            }
        }
        return 0;
    }
    /***
     * gets todays date.
     * @return String presenting todays date in format yyyy-MM-dd
     */
    public String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat  df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        cal.add(Calendar.DAY_OF_YEAR, 0);
        Date yesterday = cal.getTime();
        return df.format(yesterday);
    }

    /***
     * Aggregate total peg counts over all time for a given peg value AND a given peg type.
     * @param pegValue
     * @param type differentiate here between TYPE_2 and TYPE_3 scores.
     * @return Integer representing the total peg count for a given pegValue.
     */
    public PegRecord getTodayPegValue(int pegValue, int type) {

        final String selection = PEG_VALUE_WHERE+ " AND "+ DATE_WHERE + " AND " + TYPE_WHERE;
        final String selectionArgs[] = { String.valueOf(pegValue),
                getTodaysDate(), String.valueOf(type)};

        PegRecord pegRecord;
        Log.d(TAG, "getTodayPegValue:value:"+pegValue);
        Log.d(TAG, "getTodayPegValue:selection:"+selection);
        Log.d(TAG, "getTodayPegValue:selectionArgs:pegValue:"+selectionArgs[0]);
        Log.d(TAG, "getTodayPegValue:selectionArgs:Date:"+selectionArgs[1]);
        Log.d(TAG, "getTodayPegValue:selectionArgs:type:"+selectionArgs[2]);
        cursor = super.query(getScoreTableName(), SCORE_COLUMNS, selection,selectionArgs, PEG_VALUE);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                pegRecord = cursorToEntity(cursor);
                Log.d(TAG, "foundMatch:"+pegRecord.toString());
                cursor.close();
                return pegRecord;
            }


        }
        return null;
    }

    public ContentValues setContentValues(PegRecord scoreRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PEG_VALUE, scoreRecord.getPegValue());
        contentValues.put(TYPE, scoreRecord.getType());
        contentValues.put(PEG_COUNT, scoreRecord.getPegCount());
        contentValues.put(LAST_MODIFIED, scoreRecord.getDateStored());
        return contentValues;
    }

    protected PegRecord cursorToEntity(Cursor cursor) {
        int pegValueIndex;
        int pegCountIndex;
        int typeIndex;
        int lastModifiedIndex;

        PegRecord pegRecord = new PegRecord();
        if (cursor != null) {
            if (cursor.getColumnIndex(PEG_VALUE) != -1) {
                pegValueIndex = cursor.getColumnIndexOrThrow(PEG_VALUE);
                pegRecord.pegValue = cursor.getInt(pegValueIndex);
            }
            if (cursor.getColumnIndex(TYPE) != -1) {
                typeIndex = cursor.getColumnIndexOrThrow(TYPE);
                pegRecord.type = cursor.getInt(typeIndex);
            }
            if (cursor.getColumnIndex(PEG_COUNT) != -1) {
                pegCountIndex = cursor.getColumnIndexOrThrow(PEG_COUNT);
                pegRecord.pegCount = cursor.getInt(pegCountIndex);
            }
            if (cursor.getColumnIndex(LAST_MODIFIED) != -1) {
                lastModifiedIndex = cursor.getColumnIndexOrThrow(LAST_MODIFIED);
                pegRecord.dateStored = cursor.getString(lastModifiedIndex);
            }

        }
        return pegRecord;
    }
}
