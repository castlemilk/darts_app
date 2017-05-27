package com.primewebtech.darts.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.primewebtech.darts.database.model.PegRecord;
import com.primewebtech.darts.database.model.ScoreSchema;

import java.io.IOException;

/**
 * Created by benebsworth on 27/5/17.
 */

public class ScoreDao extends DatabaseContentProvider implements ScoreSchema {

    private static final String TAG = ScoreDao.class.getSimpleName();

    private Cursor cursor;

    protected String getTableName() {
        return SCORE_TABLE;
    }

    public ScoreDao(SQLiteDatabase database) {
        super(database);
    }
    public void updatePegValue(int pegValue, PegRecord scoreRecord) throws IOException {
        super.update(getTableName(), setContentValues(scoreRecord), PEG_VALUE_WHERE,
                new String[]{String.valueOf(pegValue)});

    }
    public boolean addPegValue(PegRecord scoreRecord) throws IOException {
        Log.d(TAG, "addPegValue:");

        return super.insert(SCORE_TABLE, setContentValues(scoreRecord)) > 0;
    }
    public PegRecord getPegValue(int pegValue) {
        final String selectionArgs[] = { String.valueOf(pegValue) };
        final String selection = PEG_VALUE + " = ?";
        PegRecord pegRecord = new PegRecord();
        cursor = super.query(getTableName(), SCORE_COLUMNS, selection,selectionArgs, PEG_VALUE);
        if (cursor != null) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                pegRecord = cursorToEntity(cursor);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return pegRecord;
    }

    public ContentValues setContentValues(PegRecord scoreRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PEG_VALUE, scoreRecord.getPegValue());
        contentValues.put(PEG_COUNT, scoreRecord.getPegCount());
        contentValues.put(LAST_MODIFIED, scoreRecord.getDateStored());
        return contentValues;
    }

    protected PegRecord cursorToEntity(Cursor cursor) {
        int pegValue;
        int pegCount;
        int lastModified;
        PegRecord pegRecord = new PegRecord();
        if (cursor != null) {
            if (cursor.getColumnIndex(PEG_VALUE) != -1) {
                pegValue = cursor.getColumnIndexOrThrow(PEG_VALUE);
                pegRecord.pegValue = pegValue;
            }
            if (cursor.getColumnIndex(PEG_COUNT) != -1) {
                pegCount = cursor.getColumnIndexOrThrow(PEG_COUNT);
                pegRecord.pegCount = pegCount;
            }
            if (cursor.getColumnIndex(LAST_MODIFIED) != -1) {
                lastModified = cursor.getColumnIndexOrThrow(LAST_MODIFIED);
                pegRecord.dateStored = lastModified;
            }

        }
        return pegRecord;
    }
}
