package com.primewebtech.darts.database.model;

import android.content.Context;

/**
 * Created by benebsworth on 27/5/17.
 */

public class BaseRecord {

    protected final Context context;
    public int type;
    public String dateStored;



    public BaseRecord(Context context, String dateStored, int type) {
        this.context = context.getApplicationContext();
        this.type = type;
        this.dateStored = dateStored;
    }

    public BaseRecord() {
        this.context = null;
        this.type = 0;
        this.dateStored = "";
    }

    public BaseRecord(String dateStored, int type) {
        this.context = null;
        this.type = type;
        this.dateStored = dateStored;
    }


    public String getDateStored() {
        return dateStored;
    }
    public int getType() {
        return type;
    }
}
