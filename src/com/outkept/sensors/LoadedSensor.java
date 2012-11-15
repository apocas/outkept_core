package com.outkept.sensors;

/**
 *
 * @author pedrodias
 */
public class LoadedSensor extends Sensor implements Cloneable {

    public boolean inverted = false;

    public LoadedSensor(String name, String cmd, String reactive, String verifier, Object alarmValue, Object warningValue, boolean export, boolean inverted, boolean zero) {
        this.name = name;
        this.cmd = cmd;
        this.reactive = reactive;
        this.verifier = verifier;
        this.alarmValue = alarmValue;
        this.warningValue = warningValue;
        this.export = export;
        this.inverted = inverted;
        this.zero = zero;
    }

    public LoadedSensor(LoadedSensor o) {
        this.name = o.name;
        this.cmd = o.cmd;
        this.reactive = o.reactive;
        this.verifier = o.verifier;
        this.alarmValue = o.alarmValue;
        this.warningValue = o.warningValue;
        this.export = o.export;
        this.inverted = o.inverted;
        this.zero = o.zero;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new LoadedSensor(this);
    }

    @Override
    public boolean loadValue(String line) {
        String[] aux = line.split("-#-");
        for (int i = 0; i < aux.length - 1; i++) {
            if (name.equals(aux[i])) {
                try {
                    value = Double.parseDouble(aux[++i].trim());
                } catch (Exception e) {
                    value = new Double(-1);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAlarmed() {
        if (alarmValue != null && value != null) {
            if ((!this.inverted && (((Double) value).compareTo((Double) alarmValue) > 0 || ((((Double) value).compareTo((Double) 0.0) <= 0) && this.zero))) || (this.inverted && ((Double) value).compareTo((Double) alarmValue) < 0)) {
                return true;
            } else {
                fired = false;
                return false;
            }
        } else {
            return false;
        }

    }

    @Override
    public boolean isWarn() {
        if (warningValue != null && value != null) {
            if ((!inverted && ((Double) value).compareTo((Double) warningValue) > 0) || (inverted && ((Double) value).compareTo((Double) warningValue) < 0)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String alarm() {
        return "firing " + name + " sensor";
    }
}
