package com.primewebtech.darts.database.model;

/**
 * Created by benebsworth on 28/5/17.
 */

public interface ActionSchema {
    String ACTION_TABLE              = "darts_action_history";
    String ID                        = "_id";
    String ACTION_TYPE               = "action_type"; //i.e add/delete
    int ADD                       = 1;
    int DEL                       = -1;
    int MODE_ONE                  = 1;
    int MODE_TWO                  = 2;
    int MODE_THREE                = 3;
    int MODE_HUNDRED              = 100;
    int HISTORY_LIMIT             = 5000;
    String GAME_MODE                 = "game_mode"; //i.e one, two, three or hundred+
    String PEG_VALUE                 = "peg_value"; //i.e 40/2/50
    String PEG_TYPE                     = "peg_type"; //i.e 2 dart or 3 dart
    String PEG_COUNT                 = "peg_count"; // i.e 101
    String ACTION_VALUE              = "action_value"; //i.e +3/+1/-1
    String DATE                      = "date"; // i.e 2017-05-28
    String CREATE_ACTION_TABLE = "CREATE TABLE IF NOT EXISTS " + ACTION_TABLE + " (" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            GAME_MODE + " INTEGER, " +
            PEG_VALUE + " INTEGER, " +
            PEG_TYPE + " INTEGER, " +
            PEG_COUNT + " INTEGER, " +
            ACTION_TYPE + " INTEGER, " +
            ACTION_VALUE + " INTEGER, " +
            DATE + " DATETIME);";
    String[] ACTION_COLUMNS = new String[] {ID,
            GAME_MODE, PEG_VALUE, PEG_TYPE, PEG_COUNT, ACTION_TYPE, ACTION_VALUE, DATE };
    String ID_WHERE                  = ID + " = ?";
    String GAME_MODE_WHERE                  = GAME_MODE + " = ?";
    String PEG_TYPE_WHERE            = PEG_TYPE + " = ?";

}
