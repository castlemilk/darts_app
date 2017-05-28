package com.primewebtech.darts.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.primewebtech.darts.database.model.Action;
import com.primewebtech.darts.database.model.ActionSchema;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by benebsworth on 27/5/17.
 */

public class ActionDao extends DatabaseContentProvider implements ActionSchema {

    private static final String TAG = ActionDao.class.getSimpleName();

    private Cursor cursor;
    protected String getActionTableName() {
        return ACTION_TABLE;
    }

    public ActionDao(SQLiteDatabase database) {
        super(database);
    }
    public boolean addAction(Action action) {
        Log.d(TAG, "addAction:actionType:"+action.getActionType());
        Log.d(TAG, "addAction:actionValue:"+action.getActionValue());
        Log.d(TAG, "addAction:pegValue:"+action.getPegValue());
        Log.d(TAG, "addAction:pegCount:"+action.getPegCount());

        return super.insert(getActionTableName(), setContentValues(action)) > 0;
    }

    public String getDateNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }

    public ContentValues setContentValues(Action action) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ACTION_TYPE, action.getActionType());
        contentValues.put(ACTION_VALUE, action.getActionValue());
        contentValues.put(PEG_VALUE, action.getPegValue());
        contentValues.put(PEG_COUNT, action.getPegCount());
        contentValues.put(DATE, getDateNow());
        return contentValues;
    }
    public Action getAndDeleteLastHistoryAction() {
        Log.d(TAG, "getAndDeleteLastestHistoryAction");
        String ORDER_BY = ID+" DESC";
        String LIMIT = "1";
        Action action;
        cursor = super.query(getActionTableName(), ACTION_COLUMNS, null,null,ORDER_BY, LIMIT);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                action = cursorToEntity(cursor);
                Log.d(TAG, "getAndDeleteLastestHistoryAction:foundMatch:"+action.toString());
                Log.d(TAG, "getAndDeleteLastestHistoryAction:deleting:id:"+action.id);
                boolean done = deleteAction(action.id);
                Log.d(TAG, "getAndDeleteLastestHistoryAction:delete:done:"+done);
                cursor.close();
                return action;
            }
        }
        return null;
    }
    public boolean deleteAction(int id) {
        String selection = ID_WHERE;
        final String selectionArgs[] = { String.valueOf(id)};
        return super.delete(getActionTableName(), selection, selectionArgs) > 0;

    }

    protected Action cursorToEntity(Cursor cursor) {
        int actionTypeIndex;
        int actionValueIndex;
        int actionPegValueIndex;
        int actionPegCountIndex;
        int dateIndex;
        int IDIndex;
        Action action = new Action();
        if (cursor != null) {
            if (cursor.getColumnIndex(ACTION_TYPE) != -1) {
                actionTypeIndex = cursor.getColumnIndexOrThrow(ACTION_TYPE);
                action.actionType = cursor.getInt(actionTypeIndex);
            }
            if (cursor.getColumnIndex(ACTION_VALUE) != -1) {
                actionValueIndex = cursor.getColumnIndexOrThrow(ACTION_VALUE);
                action.actionValue = cursor.getInt(actionValueIndex);
            }
            if (cursor.getColumnIndex(PEG_VALUE) != -1) {
                actionPegValueIndex = cursor.getColumnIndexOrThrow(PEG_VALUE);
                action.pegValue = cursor.getInt(actionPegValueIndex);
            }
            if (cursor.getColumnIndex(PEG_COUNT) != -1) {
                actionPegCountIndex = cursor.getColumnIndexOrThrow(PEG_COUNT);
                action.pegCount = cursor.getInt(actionPegCountIndex);
            }
            if (cursor.getColumnIndex(DATE) != -1) {
                dateIndex = cursor.getColumnIndexOrThrow(DATE);
                action.dateStored = cursor.getString(dateIndex);
            }
            if (cursor.getColumnIndex(ID) != -1) {
                IDIndex = cursor.getColumnIndexOrThrow(ID);
                action.id = cursor.getInt(IDIndex);
            }

        }
        return action;
    }
}
