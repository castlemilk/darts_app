package com.primewebtech.darts.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by benebsworth on 27/5/17.
 */

public abstract class DatabaseContentProvider {

    public SQLiteDatabase mDatabase;
    public int delete(String tableName, String selection,
                      String[] selectionArgs) {
        return mDatabase.delete(tableName, selection, selectionArgs);
    }
    public long insert(String tableName, ContentValues values) {
        return mDatabase.insert(tableName, null, values);
    }
    protected abstract <T> T cursorToEntity(Cursor cursor);

    public DatabaseContentProvider(SQLiteDatabase database) {
        this.mDatabase = database;
    }
    public Cursor query(String tableName, String[] columns,
                        String selection, String[] selectionArgs, String sortOrder) {

        final Cursor cursor = mDatabase.query(tableName, columns,
                selection, selectionArgs, null, null, sortOrder);

        return cursor;
    }
    public Cursor query(String tableName, String[] columns,
                        String selection, String[] selectionArgs, String sortOrder,
                        String limit) {

        return mDatabase.query(tableName, columns, selection,
                selectionArgs, null, null, sortOrder, limit);
    }
    public int update(String tableName, ContentValues values,
                      String selection, String[] selectionArgs) {
        return mDatabase.update(tableName, values, selection,
                selectionArgs);
    }
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDatabase.rawQuery(sql, selectionArgs);
    }

}
