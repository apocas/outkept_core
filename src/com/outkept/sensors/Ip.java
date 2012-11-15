package com.outkept.sensors;

/**
 *
 * @author pedrodias
 */
public class Ip extends Sensor {

    public Ip() {
        name = "lastip";
        cmd = "echo -n \";lastip-#-\";last -1 -i | head -1 | awk -F ' ' '{print $3}';echo ';';";

        value = "-1";

        export = false;
    }

    @Override
    public boolean loadValue(String line) {
        String[] aux = line.split("-#-");
        for (int i = 0; i < aux.length - 1; i++) {
            if ("lastip".equals(aux[i])) {
                try {
                    value = aux[++i].trim();
                } catch (Exception e) {
                    value = "-1";
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAlarmed() {
        return false;
    }

    @Override
    public boolean isWarn() {
        return false;
    }

    @Override
    public String alarm() {
        return null;
    }
}
