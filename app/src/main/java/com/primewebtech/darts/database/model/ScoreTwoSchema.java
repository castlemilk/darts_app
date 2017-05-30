package com.primewebtech.darts.database.model;

/**
 * Created by benebsworth on 27/5/17.
 */

public interface ScoreTwoSchema {
    String SCORE_TABLE_TWO              = "darts_score_two";
    String ID                             = "_id";
    String PEG_VALUE               = "peg_value"; //i.e peg 40
    String PEG_TYPE                = "peg_type"; //i.e 2 dart or 3 dart
    String PEG_COUNT               = "peg_count"; //i.e 40 times
    String LAST_MODIFIED           = "last_modified"; // i.e 12412412412412

    String CREATE_SCORE_TABLE_TWO = "CREATE TABLE IF NOT EXISTS " + SCORE_TABLE_TWO + " (" + ID + " INTEGER PRIMARY KEY, " +
            PEG_VALUE + " INTEGER, " +
            PEG_TYPE + " INTEGER, " +
            PEG_COUNT + " INTEGER DEFAULT 0, " +
            LAST_MODIFIED + " DATETIME);";
    String[] SCORE_TWO_COLUMNS = new String[] { ID,
            PEG_VALUE, PEG_TYPE, PEG_COUNT, LAST_MODIFIED };
    String PEG_VALUE_WHERE              = PEG_VALUE + " = ?";
    String DATE_WHERE                   = LAST_MODIFIED + " >= ?";

}
