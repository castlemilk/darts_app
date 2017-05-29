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

public class ScoreOneDao extends DatabaseContentProvider implements ScoreSchema {

    private static final String TAG = ScoreOneDao.class.getSimpleName();

    private Cursor cursor;

    protected String getTodayScoreTableName() {
        return TODAY_SCORE_TABLE;
    }
    protected String getScoreTableName() {
        return SCORE_TABLE_ONE;
    }

    public ScoreOneDao(SQLiteDatabase database) {
        super(database);
    }
    public boolean updateTodayPegValue(PegRecord scoreRecord) throws IOException {
        if (scoreRecord != null) {
            return super.update(getScoreTableName(), setContentValues(scoreRecord), PEG_VALUE_WHERE,
                    new String[]{String.valueOf(scoreRecord.getPegValue())}) > 0;
        } else {
            return false;
        }


    }
    public boolean addTodayPegValue(PegRecord scoreRecord) throws IOException {
        Log.d(TAG, "addPegValue:"+scoreRecord.toString());
        if (getTodayPegValue(scoreRecord.getPegValue()) != null) {
            return updateTodayPegValue(scoreRecord);
        } else {
            return super.insert(getScoreTableName(), setContentValues(scoreRecord)) > 0;
        }


    }
    public String getDateNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }

    public boolean increaseTodayPegValue(int pegValue, int increment) {
        Log.d(TAG, "increaseTodayPegValue:pegValue:"+pegValue);
        Log.d(TAG, "increaseTodayPegValue:increment:"+increment);
        PegRecord pegRecord = getTodayPegValue(pegValue);

        if (pegRecord != null) {
            Log.d(TAG, "increaseTodayPegValue:currentPegCount:"+pegRecord.getPegCount());
            ContentValues contentValues = new ContentValues();
            contentValues.put(PEG_COUNT, pegRecord.getPegCount()+increment);
            contentValues.put(LAST_MODIFIED, getDateNow());
            return super.update(getScoreTableName(), contentValues, PEG_VALUE_WHERE,
                    new String[]{String.valueOf(pegValue)}) > 0;
        } else {
            return false;
        }
    }
    public boolean decreaseTodayPegValue(int pegValue, int decrement) {
        Log.d(TAG, "decreaseTodayPegValue:pegValue:"+pegValue);
        Log.d(TAG, "decreaseTodayPegValue:decrement:"+decrement);
        PegRecord pegRecord = getTodayPegValue(pegValue);

        if (pegRecord != null) {
            Log.d(TAG, "increaseTodayPegValue:currentPegCount:"+pegRecord.getPegCount());
            ContentValues contentValues = new ContentValues();
            contentValues.put(PEG_COUNT, pegRecord.getPegCount()-decrement);
            contentValues.put(LAST_MODIFIED, getDateNow());
            return super.update(getScoreTableName(), contentValues, PEG_VALUE_WHERE,
                    new String[]{String.valueOf(pegValue)}) > 0;
        } else {
            return false;
        }
    }
    public boolean rollbackScore(Action action) {
        if (action.actionType == ActionSchema.ADD) {
            return decreaseTodayPegValue(action.pegValue, action.actionValue);
        } else {
            return increaseTodayPegValue(action.pegValue, action.actionValue);
        }

    }

    /***
     * Aggregate total peg counts over all time.
     * @param pegValue
     * @return
     */
    public int getTotalPegCount(int pegValue) {
        final String selectionArgs[] =  {String.valueOf(pegValue)};
        cursor = super.rawQuery("select sum(" + PEG_COUNT + ") from " + SCORE_TABLE_ONE +
                " WHERE " + PEG_VALUE_WHERE + ";", selectionArgs);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int total = cursor.getInt(0);
                cursor.close();
                return total;
            }
        }
        return 0;
    }

    public String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat  df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = cal.getTime();
        return df.format(yesterday);
    }
    public PegRecord getTodayPegValue(int pegValue) {
        final String selectionArgs[] = { String.valueOf(pegValue),  getTodaysDate()};
        final String selection = PEG_VALUE_WHERE+ " AND "+ DATE_WHERE;

        PegRecord pegRecord;
        Log.d(TAG, "getTodayPegValue:value:"+pegValue);
        Log.d(TAG, "getTodayPegValue:selection:"+selection);
        Log.d(TAG, "getTodayPegValue:selectionArgs:pegValue:"+selectionArgs[0]);
        Log.d(TAG, "getTodayPegValue:selectionArgs:getTodaysDate"+selectionArgs[1]);
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
        contentValues.put(PEG_COUNT, scoreRecord.getPegCount());
        contentValues.put(LAST_MODIFIED, scoreRecord.getDateStored());
        return contentValues;
    }

    protected PegRecord cursorToEntity(Cursor cursor) {
        int pegValueIndex;
        int pegCountIndex;
        int lastModifiedIndex;
        PegRecord pegRecord = new PegRecord();
        if (cursor != null) {
            if (cursor.getColumnIndex(PEG_VALUE) != -1) {
                pegValueIndex = cursor.getColumnIndexOrThrow(PEG_VALUE);
                pegRecord.pegValue = cursor.getInt(pegValueIndex);
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