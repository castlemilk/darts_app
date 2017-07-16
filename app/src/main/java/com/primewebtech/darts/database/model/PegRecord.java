package com.primewebtech.darts.database.model;

/**
 * Created by benebsworth on 27/5/17.
 */

public class PegRecord extends BaseRecord{
    private static final String TAG = PegRecord.class.getSimpleName();
    public int pegValue;
    public int pegCount;
    public String period;

    public PegRecord() {
        super();
        this.pegValue = 0;
        this.pegCount = 0;
        this.period = "DAY";
    }

    public PegRecord(String dateStored, int type,
                       int pegValue, int pegCount) {
        super(dateStored, type);
        this.pegValue = pegValue;
        this.pegCount = pegCount;
    }
    public PegRecord(String dateStored, int type,
                     int pegValue, int pegCount, String period) {
        super(dateStored, type);
        this.pegValue = pegValue;
        this.pegCount = pegCount;
        this.period = period;
    }

    public int getPegCount() {
        return pegCount;
    }

    public int getPegValue() {
        return pegValue;
    }
    public String toString() {
        return "pegType = " + type + ", pegValue = " +
                pegValue + ", pegCount = " + pegCount +
                ", pegLastModified = " + dateStored +
                ", period = " + period;
    }
}
