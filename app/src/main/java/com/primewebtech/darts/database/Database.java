package com.primewebtech.darts.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by benebsworth on 27/5/17.
 */

public abstract class Database {
    protected SQLiteOpenHelper databaseHelper;
    protected final Context context;
    public Database(Context context, SQLiteOpenHelper databaseHelper) {
        this.context        = context;
        this.databaseHelper = databaseHelper;
    }
    public void reset(SQLiteOpenHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

}
