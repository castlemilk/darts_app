package com.primewebtech.darts.database.model;

/**
 * Created by benebsworth on 27/5/17.
 */

public interface ScoreSchema {
    String SCORE_TABLE              = "darts_score";
    String PEG_VALUE               = "peg_value"; //i.e peg 40
    String PEG_COUNT               = "peg_count"; //i.e 40 times
    String LAST_MODIFIED           = "last_modified"; // i.e 12412412412412
    String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + SCORE_TABLE + " (" + PEG_VALUE + " INTEGER PRIMARY KEY, " +
            PEG_COUNT + " INTEGER DEFAULT 0, " +
            LAST_MODIFIED + " BIGINT);";
    String[] SCORE_COLUMNS = new String[] {PEG_VALUE,
            PEG_COUNT, LAST_MODIFIED };
    String PEG_VALUE_WHERE              = "peg_value = ?";

}
