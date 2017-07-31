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

     /** Creates another row in the ACTION_TABLE table, where there is a defined mapping/structure
      * present in the Action object which allows the correct ContentValues to be set
      * Note:  The table has a has a historical limit of 25 entires, policed by a trigger policy in
      * the database.
     * @param action The action object to add to the historical action table
     * @return Returns a boolean representing the successful insertion or failure.
     */
    public boolean addAction(Action action) {
        Log.d(TAG, "addAction:actionType:"+action.getActionType());
        Log.d(TAG, "addAction:actionValue:"+action.getActionValue());
        Log.d(TAG, "addAction:gameMode:"+action.getGameMode());
        Log.d(TAG, "addAction:pegValue:"+action.getPegValue());
        Log.d(TAG, "addAction:pegCount:"+action.getPegCount());

        return super.insert(getActionTableName(), setContentValues(action)) > 0;
    }
    /** Fetches the current date and time. Used for logging and apply timestamp the table entries
     * @return Returns the current date and time in the format yyyy-MM-dd hh:mm:ss
     */
    public String getDateNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        Date now = new Date();
        return dateFormat.format(now);
    }
    /** Takes the Action object and translates it into the corresponding columns within the action
     * table.
     * @param action The action object to add to the historical action table
     * @return ContentValues object which is inserted into database.
     */
    public ContentValues setContentValues(Action action) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ACTION_TYPE, action.getActionType());
        contentValues.put(ACTION_VALUE, action.getActionValue());
        contentValues.put(GAME_MODE, action.getGameMode());
        contentValues.put(PEG_VALUE, action.getPegValue());
        contentValues.put(PEG_TYPE, action.getType());
        contentValues.put(PEG_COUNT, action.getPegCount());
        contentValues.put(DATE, getDateNow());
        return contentValues;
    }
    /** Fetches from the "top" of the stack in terms of stored actions and removes row. This will
     * correspond with user hitting the back button and an action being reversed.
     * @param gameMode Type of scoring type, i.e one, two, three or 100. It used to ensure that we
     * only make changes for the viewed scoring mode, avoiding changing the scores of other scoring
     * modes
     * @return The action object that was at the top of the stack (now removed)
     */
    public Action getAndDeleteLastHistoryAction(int gameMode) {
        Log.d(TAG, "getAndDeleteLastestHistoryAction");
        String ORDER_BY = ID+" DESC";
        String LIMIT = "1";
        Action action;
        final String selection = GAME_MODE_WHERE;
        final String selectionArgs[] = { String.valueOf(gameMode)};

        cursor = super.query(getActionTableName(), ACTION_COLUMNS, selection,
                selectionArgs, ORDER_BY, LIMIT);
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
    /** Deletes an action from the action table with a given ID
     * @param id the id of the action to be deleted
     * @return Boolean representing the success or failure of deletion.
     */
    public boolean deleteAction(int id) {
        String selection = ID_WHERE;
        final String selectionArgs[] = { String.valueOf(id)};
        return super.delete(getActionTableName(), selection, selectionArgs) > 0;

    }
    /** Converts the database cursor into an action item, effectively deserialising a action item
     * row in SQL into a POJO.
     * @param cursor Database cursor based on some selection query etc.
     * @return Action item object corresponding to the selected row in SQL.
     */
    protected Action cursorToEntity(Cursor cursor) {
        int actionTypeIndex;
        int actionValueIndex;
        int actionGameModeIndex;
        int actionPegValueIndex;
        int actionPegTypeIndex;
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
            if (cursor.getColumnIndex(GAME_MODE) != -1) {
                actionGameModeIndex = cursor.getColumnIndexOrThrow(GAME_MODE);
                action.gameMode = cursor.getInt(actionGameModeIndex);
            }
            if (cursor.getColumnIndex(PEG_VALUE) != -1) {
                actionPegValueIndex = cursor.getColumnIndexOrThrow(PEG_VALUE);
                action.pegValue = cursor.getInt(actionPegValueIndex);
            }
            if (cursor.getColumnIndex(PEG_TYPE) != -1) {
                actionPegTypeIndex = cursor.getColumnIndexOrThrow(PEG_TYPE);
                action.type = cursor.getInt(actionPegTypeIndex);
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
