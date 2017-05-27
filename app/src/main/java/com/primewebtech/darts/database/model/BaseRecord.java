package com.primewebtech.darts.database.model;

import android.content.Context;

/**
 * Created by benebsworth on 27/5/17.
 */

public class BaseRecord {

    protected final Context context;
    protected final int type;
    public long dateStored;



    public BaseRecord(Context context, long dateStored, int type) {
        this.context = context.getApplicationContext();
        this.type = type;
        this.dateStored = dateStored;
    }

    public BaseRecord() {
        this.context = null;
        this.type = 0;
        this.dateStored = 0;
    }

    public BaseRecord(long dateStored, int type) {
        this.context = null;
        this.type = type;
        this.dateStored = dateStored;
    }


    public long getDateStored() {
        return dateStored;
    }
    public int getType() {
        return type;
    }
}
