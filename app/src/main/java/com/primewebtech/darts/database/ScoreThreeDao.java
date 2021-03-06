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

public class ScoreThreeDao extends DatabaseContentProvider implements ScoreSchema {

    private static final String TAG = ScoreTwoDao.class.getSimpleName();

    private Cursor cursor;
    protected String getScoreTableName() {
        return SCORE_TABLE_THREE;
    }

    public ScoreThreeDao(SQLiteDatabase database) {
        super(database);
    }
    public boolean updatePegValue(PegRecord scoreRecord) throws IOException {
        if (scoreRecord != null) {
            String selector = PEG_VALUE_WHERE+ " AND "+ TYPE_WHERE;
            String selectorArgs[] = new String[]{String.valueOf(scoreRecord.getPegValue()),
                     String.valueOf(scoreRecord.type)};
            return super.update(getScoreTableName(), setContentValues(scoreRecord), selector,
                    selectorArgs) > 0;
        } else {
            return false;
        }


    }
    public boolean addPegValue(PegRecord scoreRecord) throws IOException {
        Log.d(TAG, "addPegValue:"+scoreRecord.toString());
        if (getPegValue(scoreRecord.pegValue, scoreRecord.type) != null) {
            return updatePegValue(scoreRecord);
        } else {
            return super.insert(getScoreTableName(), setContentValues(scoreRecord)) > 0;
        }


    }
    public String getDateNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }

    public boolean increasePegValue(int pegValue, int type, int increment) {
        Log.d(TAG, "increaseTodayPegValue:pegValue:"+pegValue);
        Log.d(TAG, "increaseTodayPegValue:increment:"+increment);
        PegRecord pegRecord = getPegValue(pegValue, type);
        final String selection = PEG_VALUE_WHERE+ " AND " + TYPE_WHERE;
        final String selectionArgs[] = { String.valueOf(pegValue),
                String.valueOf(type)};

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
    public boolean decreasePegValue(int pegValue, int type, int decrement) {
        Log.d(TAG, "decreaseTodayPegValue:pegValue:"+pegValue);
        Log.d(TAG, "decreaseTodayPegValue:decrement:"+decrement);
        PegRecord pegRecord = getPegValue(pegValue, type);
        String selector = PEG_VALUE_WHERE+  "AND " + TYPE_WHERE;
        String selectorArgs[] = new String[]{String.valueOf(pegValue), String.valueOf(type)};

        if (pegRecord != null) {
            Log.d(TAG, "decreasingTodayPegValue:currentPegCount:"+pegRecord.getPegCount());
            ContentValues contentValues = new ContentValues();
            int newCount = pegRecord.getPegCount()-decrement;
            if (newCount >= 0 ) {
                contentValues.put(PEG_COUNT, pegRecord.getPegCount()-decrement);
                contentValues.put(LAST_MODIFIED, getDateNow());
                return super.update(getScoreTableName(), contentValues,selector,
                        selectorArgs) > 0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public boolean rollbackScore(Action action) {
        if (action.actionType == ActionSchema.ADD) {
            Log.d(TAG, "rollbackScore:actionType:ADD");
            Log.d(TAG, "rollbackScore:actionType:DECREASING SCORE TO RESVERSE");
            return decreasePegValue(action.pegValue, action.type, action.actionValue);
        } else {
            Log.d(TAG, "rollbackScore:actionType:DEL");
            Log.d(TAG, "rollbackScore:actionType:INCREASING SCORE TO RESVERSE");
            return increasePegValue(action.pegValue, action.type, action.actionValue);
        }

    }

    /***
     * Aggregate total peg counts over all time.
     * DON't need to differentiate here between TYPE_2 and TYPE_3 scores.
     * @param pegValue
     * @return
     */
    public int getTotalPegCount(int pegValue) {
        final String selectionArgs[] =  {String.valueOf(pegValue)};
        cursor = super.rawQuery("select sum(" + PEG_COUNT + ") from " + SCORE_TABLE_THREE +
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
        cal.add(Calendar.DAY_OF_YEAR, 0);
        Date yesterday = cal.getTime();
        return df.format(yesterday);
    }
    public PegRecord getPegValue(int pegValue, int type) {

        final String selection = PEG_VALUE_WHERE+ " AND " + TYPE_WHERE;
        final String selectionArgs[] = { String.valueOf(pegValue),
                 String.valueOf(type)};

        PegRecord pegRecord;
        Log.d(TAG, "getTodayPegValue:value:"+pegValue);
        Log.d(TAG, "getTodayPegValue:selection:"+selection);
        Log.d(TAG, "getTodayPegValue:selectionArgs:pegValue:"+selectionArgs[0]);
        Log.d(TAG, "getTodayPegValue:selectionArgs:type:"+selectionArgs[1]);
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
