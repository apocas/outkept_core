package com.outkept.sensors;

/**
 *
 * @author pedrodias
 */
public abstract class Sensor {

    public String name;
    public String cmd;
    public String reactive;
    public String verifier;
    public Object value;
    public Object alarmValue;
    public Object warningValue;
    public boolean export;
    public boolean fired = false;
    public boolean twitter = true;
    public boolean zero = false;

    public abstract boolean isAlarmed();

    public abstract boolean isWarn();

    public abstract String alarm();

    public abstract boolean loadValue(String line);

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the cmd
     */
    public String getCmd() {
        return cmd;
    }

    /**
     * @return the reactive
     */
    public String getReactive() {
        return reactive;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return the alarmValue
     */
    public Object getAlarmValue() {
        return alarmValue;
    }

    public boolean isFired() {
        return fired;
    }

    public void fire() {
        fired = true;
    }

    public boolean isZeroed() {
        if ((((Double) value).compareTo((Double) 0.0) <= 0) && this.zero) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param alarmValue the alarmValue to set
     */
    public void setAlarmValue(Object alarmValue) {
        this.alarmValue = alarmValue;
    }

    /**
     * @return the warningValue
     */
    public Object getWarningValue() {
        return warningValue;
    }

    /**
     * @param warningValue the warningValue to set
     */
    public void setWarningValue(Object warningValue) {
        this.warningValue = warningValue;
    }

    public boolean isExport() {
        return export;
    }

    /**
     * @return the twitter
     */
    public boolean isTwitter() {
        return twitter;
    }
}
