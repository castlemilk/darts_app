package com.primewebtech.darts.database.model;

/**
 * Created by benebsworth on 27/5/17.
 */

public interface ScoreSchema {
    String TODAY_SCORE_TABLE              = "darts_score_today";
    String SCORE_TABLE_ONE              = "darts_score_one";
    String SCORE_TABLE_TWO              = "darts_score_two";
    String SCORE_TABLE_THREE              = "darts_score_three";
    String SCORE_TABLE_HUNDRED              = "darts_score_hundred";
    String ID                             = "_id";
    String PEG_VALUE               = "peg_value"; //i.e peg 40
    String PEG_COUNT               = "peg_count"; //i.e 40 times
    String LAST_MODIFIED           = "last_modified"; // i.e 12412412412412
//    String CREATE_TODAY_SCORE_TABLE = "CREATE TABLE IF NOT EXISTS " + TODAY_SCORE_TABLE + " (" + PEG_VALUE + " INTEGER PRIMARY KEY, " +
//            PEG_COUNT + " INTEGER DEFAULT 0, " +
//            LAST_MODIFIED + " DATETIME);";
    String CREATE_SCORE_TABLE_ONE = "CREATE TABLE IF NOT EXISTS " + SCORE_TABLE_ONE + " (" + ID + " INTEGER PRIMARY KEY, " +
            PEG_VALUE + " INTEGER, " +
            PEG_COUNT + " INTEGER DEFAULT 0, " +
            LAST_MODIFIED + " DATETIME);";
    String CREATE_SCORE_TABLE_TWO = "CREATE TABLE IF NOT EXISTS " + SCORE_TABLE_TWO + " (" + ID + " INTEGER PRIMARY KEY, " +
            PEG_VALUE + " INTEGER, " +
            PEG_COUNT + " INTEGER DEFAULT 0, " +
            LAST_MODIFIED + " DATETIME);";
    String CREATE_SCORE_TABLE_THREE = "CREATE TABLE IF NOT EXISTS " + SCORE_TABLE_THREE + " (" + ID + " INTEGER PRIMARY KEY, " +
            PEG_VALUE + " INTEGER, " +
            PEG_COUNT + " INTEGER DEFAULT 0, " +
            LAST_MODIFIED + " DATETIME);";
    String CREATE_SCORE_TABLE_HUNDRED = "CREATE TABLE IF NOT EXISTS " + SCORE_TABLE_HUNDRED + " (" + ID + " INTEGER PRIMARY KEY, " +
            PEG_VALUE + " INTEGER, " +
            PEG_COUNT + " INTEGER DEFAULT 0, " +
            LAST_MODIFIED + " DATETIME);";
    String[] SCORE_COLUMNS = new String[] {PEG_VALUE,
            PEG_COUNT, LAST_MODIFIED };
    String PEG_VALUE_WHERE              = PEG_VALUE + " = ?";
    String DATE_WHERE                   = LAST_MODIFIED + " > ?";

}
