package com.primewebtech.darts.database.model;

/**
 * Created by benebsworth on 28/5/17.
 */

public class Action extends BaseRecord implements ActionSchema {

    public int actionType;
    public int pegValue;
    public int actionValue;
    public int pegCount;
    public int id;
    public Action() {
        super();
        this.pegValue = 0;
        this.actionValue = 0;
        this.actionType = 1;

        this.id = 0;
    }
    public Action(int actionType, int actionValue, int pegValue, int pegCount) {
        this.actionValue = actionValue;
        this.actionType = actionType;
        this.pegValue = pegValue;
        this.pegCount = pegCount;
    }

    public int getActionType() {
        return actionType;
    }
    public int getPegValue() {
        return pegValue;
    }
    public int getActionValue() {
        return actionValue;
    }
    public int getPegCount() {
        return pegCount;
    }
    public int getId() {
        return id;
    }
    public String getRollBackValue() {
        if (actionType == ADD) {
            return String.valueOf(pegCount - actionValue);
        } else {
            return String.valueOf(pegCount + actionValue);
        }
    }
    public String toString() {
        return "pegValue = " + pegValue + "pegCount = " + pegCount+ ", actionType = " +
                actionType + ", actionValue = " + actionValue +
                ", date = " + dateStored + ", id = " + id;
    }
}
