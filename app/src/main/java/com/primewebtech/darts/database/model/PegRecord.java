package com.primewebtech.darts.database.model;

/**
 * Created by benebsworth on 27/5/17.
 */

public class PegRecord extends BaseRecord{
    private static final String TAG = PegRecord.class.getSimpleName();
    public int pegValue;
    public int pegCount;

    public PegRecord() {
        super();
        this.pegValue = 0;
        this.pegCount = 0;
    }

    public PegRecord(String dateStored, int type,
                       int pegValue, int pegCount) {
        super(dateStored, type);
        this.pegValue = pegValue;
        this.pegCount = pegCount;
    }

    public int getPegCount() {
        return pegCount;
    }

    public int getPegValue() {
        return pegValue;
    }
    public String toString() {
        return "pegValue = "+pegValue+", pegCount = "+pegCount +", pegLastModified = "+dateStored;
    }
}
