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
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by benebsworth on 27/5/17.
 */

public class StatsHundredDao extends DatabaseContentProvider implements ScoreSchema {

    private static final String TAG = StatsHundredDao.class.getSimpleName();
    private final SimpleDateFormat  df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private Cursor cursor;
    protected String getScoreTableBest() { return SCORE_TABLE_BEST; }
    protected String getScoreTableName() {
        return SCORE_TABLE_HUNDRED;
    }
    private String[] periods = { "DAY", "WEEK", "MONTH"};

    public StatsHundredDao(SQLiteDatabase database) {
        super(database);
    }
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
    public boolean addTodayPegValue(PegRecord scoreRecord) throws IOException {
        Log.d(TAG, "addPegValue:"+scoreRecord.toString());
        if (getTodayPegValue(scoreRecord.pegValue, scoreRecord.type) != null) {
            return updateTodayPegValue(scoreRecord);
        } else {
            return super.insert(getScoreTableName(), setContentValues(scoreRecord)) > 0;
        }
    }

    /**
     * Used for test case data injection etc.
     * @param scoreRecord
     * @return
     * @throws IOException
     */
    public boolean addPegValue(PegRecord scoreRecord) throws IOException {
        Log.d(TAG, "addPegValue:" + scoreRecord.toString());
        return super.insert(getScoreTableName(), setContentValues(scoreRecord)) > 0;
    }

    public boolean setBestScore(String period, int pegValue, int pegCount) {
        /**
         * On the detection of a new personal best score being made for a given peg value then we
         * update the the value in the best scores table. This activity will be carried it out when
         * viewing the stats view. Alternatively this functionality could be implemented via a
         * set trigger.
         */
        Log.d(TAG, "addNewBestScore:period:"+period);
        Log.d(TAG, "addNewBestScore:pegValue:"+pegValue);
        Log.d(TAG, "addNewBestScore:pegCount:"+pegValue);
        if (getPeriodsHighestScore(pegValue, period) != null) {
            return updateBestScore(period, pegValue, pegCount);
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PEG_VALUE, pegValue);
            contentValues.put(PERIOD, period);
            contentValues.put(TYPE, TYPE_2);
            contentValues.put(PEG_COUNT, pegCount);
            contentValues.put(LAST_MODIFIED, getDateNow());
            return super.insert(getScoreTableBest(), contentValues) > 0;
        }
    }
    public void savePB(int pegValue) {
        for (String period : periods) {
            int bestScore = getHighestScore(pegValue, period);
            setBestScore(period, pegValue, bestScore);
        }

    }

    public boolean updateBestScore(String period, int pegValue, int pegCount){
        final String selectionArgs[] =  {period, String.valueOf(pegValue)};
        final String selection = PERIOD + "= ?"+ " AND "+ PEG_VALUE_WHERE;
        ContentValues contentValues = new ContentValues();
        contentValues.put(PEG_VALUE, pegValue);
        contentValues.put(PERIOD, period);
        contentValues.put(TYPE, TYPE_2);
        contentValues.put(PEG_COUNT, pegCount);
        contentValues.put(LAST_MODIFIED, getDateNow());
        return super.update(getScoreTableBest(), contentValues, selection,
                selectionArgs) > 0;
    }
    public int getCurrentScore(int pegValue, String period) {
        if (period.contains("DAY")) {
            return getTotalPegCountDay(pegValue);
        } else if (period.contains("WEEK")) {
            return getTotalPegCountWeek(pegValue);
        } else if (period.contains("MONTH")) {
            return getTotalPegCountMonth(pegValue);
        } else {
            return getTotalPegCountDay(pegValue);
        }
    }
    public PegRecord getPeriodsHighestScore(int pegValue, String period) {
        final String selection = PEG_VALUE_WHERE+ " AND "+ PERIOD_WHERE;
        final String selectionArgs[] = { String.valueOf(pegValue),
                period};
        PegRecord pegRecord;
        cursor = super.query(getScoreTableBest(), ScoreSchema.BEST_SCORE_COLUMNS, selection,selectionArgs, PEG_VALUE);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                pegRecord = cursorToEntity(cursor);
                Log.d(TAG, "foundMatch:HighestScore:["+period+"]:"+pegRecord.toString());
                cursor.close();
                return pegRecord;
            }
        }
        return null;
    }

    public int getHighestScore(int pegvalue, String period) {
        int highetScoreWithin6Months = getHighestScoreForPeriodToday(pegvalue, period);
        PegRecord highestScoreLogged = getPeriodsHighestScore(pegvalue, period);
        if (highestScoreLogged != null) {
            if (highestScoreLogged.getPegCount() > highetScoreWithin6Months) {
                return highestScoreLogged.getPegCount();
            } else {
                return highetScoreWithin6Months;
            }
        } else {
            return highetScoreWithin6Months;
        }

    }

    public int getHighestScoreForPeriodToday(int pegvalue, String period) {
        int highestValue = 0;
        int iterator = 6;
        if (period.contains("DAY")) {
            iterator = 180;
            int score = getPreviousScore(pegvalue, period, iterator);
            highestValue = score;
        } else if (period.contains("WEEK")) {
            iterator = 10;
            for (int i = 0; i <=iterator; i++) {
                int score = getPreviousScore(pegvalue, period, i);
                if (score >= highestValue) {
                    highestValue = score;
                }
            }
        } else {
            iterator = 5;
            for (int i = 0; i <=iterator; i++) {
                int score = getPreviousScore(pegvalue, period, i);
                if (score >= highestValue) {
                    highestValue = score;
                }
            }
        }

        return highestValue;
    }

    public int getPreviousScore(int pegValue, String period, int previousPeriodIndex) {
        if (period.equals("DAY")){
            String queryString;
            if (previousPeriodIndex <= 7) {
                // use this query when determing previous 7 day scores
                queryString = " SELECT SUM(" + PEG_COUNT + ") FROM " + getScoreTableName() +
                        " WHERE " + PEG_VALUE + "=" + String.valueOf(pegValue) +
                        " AND " + LAST_MODIFIED + " = '" + getPreviousDay(previousPeriodIndex) + "';";
                Log.d(TAG, "getPreviousScore:DAY:Query:"+queryString);
            } else {
                // use this query when determining max previous daily score over 6 months
                queryString = " SELECT MAX(" + PEG_COUNT + ") FROM " + getScoreTableName() +
                        " WHERE " + PEG_VALUE + "=" + String.valueOf(pegValue) +
                        " AND " + LAST_MODIFIED + " >= '" + getPreviousDay(previousPeriodIndex) + "';";
                Log.d(TAG, "getPreviousScore:DAY:Query:"+queryString);
            }
            Log.d(TAG, "Query:day:"+queryString);
            cursor = super.rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    Log.d(TAG, "foundMax[week]:"+cursor.getInt(0));
                    int pegCountTotal = cursor.getInt(0);
                    cursor.close();
                    return pegCountTotal;
                }
            }
            return 0;
        } else if (period.equals("WEEK")) {
            HashMap<String, String> datePeriod = getPreviousWeek(previousPeriodIndex);
            final String queryString = " SELECT SUM(" + PEG_COUNT + ") FROM " + getScoreTableName() +
                    " WHERE " + PEG_VALUE + "=" + String.valueOf(pegValue) +
                    " AND " + LAST_MODIFIED + ">='" + datePeriod.get("start") + "'" +
                    " AND " + LAST_MODIFIED + "<='" + datePeriod.get("end")+"';";
            Log.d(TAG, "Query: "+queryString);
            cursor = super.rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    Log.d(TAG, "foundMax[week]:"+cursor.getInt(0));
                    int maxPegValue = cursor.getInt(0);
                    cursor.close();
                    return maxPegValue;
                }
            }
            return 0;
        } else if (period.equals("MONTH")) {
            HashMap<String, String> datePeriod = getPreviousMonth(previousPeriodIndex);
            final String queryString = " SELECT SUM(" + PEG_COUNT + ") FROM " + getScoreTableName() +
                    " WHERE " + PEG_VALUE + "=" + String.valueOf(pegValue) +
                    " AND " + LAST_MODIFIED + ">='" + datePeriod.get("start") +"'"+
                    " AND " + LAST_MODIFIED + "<='" + datePeriod.get("end")+"';";
            Log.d(TAG, "Query: "+queryString);
            cursor = super.rawQuery(queryString, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    Log.d(TAG, "foundMax[month]:"+cursor.getInt(0));
                    int maxPegValue = cursor.getInt(0);
                    cursor.close();
                    return maxPegValue;
                }
            }
            return 0;
        } else {
            return 0;
        }
    }
    public String getDateNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }
    public String getPreviousDay(int previousDayIndex) {
        Calendar cal = Calendar.getInstance(Locale.US);
        SimpleDateFormat  df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        cal.add(Calendar.DAY_OF_YEAR, -1 * previousDayIndex);
        Date previousDate = cal.getTime();
        Log.d(TAG, "PreviousDateIndex:"+previousDayIndex);
        Log.d(TAG, "getPreviousDate:"+df.format(previousDate));
        return df.format(previousDate);
    }
    public HashMap<String, String> getPreviousWeek(int previousWeekIndex) {
        HashMap<String, String> previousWeekWindow = new HashMap<>();
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.add(Calendar.DAY_OF_WEEK, -7 * previousWeekIndex); // 1: -7, 2: -14, ...
        previousWeekWindow.put("start", df.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_WEEK, 6); //1: +6, 2: +6
        previousWeekWindow.put("end", df.format(cal.getTime()));
        Log.d(TAG, "PreviousWeekIndex:"+previousWeekIndex);

        return previousWeekWindow;
    }
    public HashMap<String, String> getPreviousMonth(int previousMonthIndex) {
        HashMap<String, String> previousMonthWindow = new HashMap<>();
        Calendar cal = Calendar.getInstance(Locale.US);
        SimpleDateFormat  df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -1 * previousMonthIndex);
        previousMonthWindow.put("start", df.format(cal.getTime()));
        cal.add(Calendar.MONTH, 1); //1: +6, 2: +6
        cal.add(Calendar.DAY_OF_YEAR, -1);
        previousMonthWindow.put("end", df.format(cal.getTime()));
        Log.d(TAG, "PreviousMonthIndex:"+previousMonthIndex);

        return previousMonthWindow;
    }

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
    public boolean rollbackScore(Action action) {
        if (action.actionType == ActionSchema.ADD) {
            return decreaseTodayPegValue(action.pegValue, action.type, action.actionValue);
        } else {
            return increaseTodayPegValue(action.pegValue, action.type, action.actionValue);
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
        cursor = super.rawQuery("select sum(" + PEG_COUNT + ") from " + getScoreTableName() +
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
    /***
     * Aggregate total peg counts over all time.
     * DON't need to differentiate here between TYPE_2 and TYPE_3 scores.
     * @param pegValue
     * @return
     */
    public int getTotalPegCountDay(int pegValue) {
        String selector = PEG_VALUE_WHERE+ " AND "+ DATE_WHERE;
        String selectorArgs[] = new String[]{String.valueOf(pegValue), getDateNow()};
        cursor = super.rawQuery("select sum(" + PEG_COUNT + ") from " + getScoreTableName() +
                " WHERE " + PEG_VALUE_WHERE + " AND " + DATE_WHERE + ";", selectorArgs);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int total = cursor.getInt(0);
                cursor.close();
                return total;
            }
        }
        return 0;
    }
    /***
     * Aggregate total peg counts over all time.
     * DON't need to differentiate here between TYPE_2 and TYPE_3 scores.
     * @param pegValue
     * @return
     */
    public int getTotalPegCountWeek(int pegValue) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        String selectorArgs[] = new String[]{String.valueOf(pegValue), df.format(cal.getTime())};
        String queryString = "select sum(" + PEG_COUNT + ") from " + SCORE_TABLE_HUNDRED +
                " WHERE " + PEG_VALUE_WHERE + " AND " + DATE_WHERE + ";";
        Log.d(TAG, "getTotalPegCountWeek:date:"+ df.format(cal.getTime()));
        Log.d(TAG, "getTotalPegCountWeek:queryString:"+queryString);
        cursor = super.rawQuery(queryString, selectorArgs);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int total = cursor.getInt(0);
                cursor.close();
                return total;
            }
        }
        return 0;
    }
    /***
     * Aggregate total peg counts over all time.
     * DON't need to differentiate here between TYPE_2 and TYPE_3 scores.
     * @param pegValue
     * @return
     */
    public int getTotalPegCountMonth(int pegValue) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat  df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = cal.getTime();
        String selectorArgs[] = new String[]{String.valueOf(pegValue), df.format(startOfMonth)};

        cursor = super.rawQuery("select sum(" + PEG_COUNT + ") from " + getScoreTableName() +
                " WHERE " + PEG_VALUE_WHERE + " AND " + DATE_WHERE + ";", selectorArgs);

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
        Log.d(TAG, "getTodaysDate:"+df.format(yesterday));
        return df.format(yesterday);
    }
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
        cursor = super.query(getScoreTableName(), ScoreSchema.SCORE_COLUMNS, selection,selectionArgs, PEG_VALUE);
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
