package com.outkept.sensors;

/**
 *
 * @author pedrodias
 */
public class Users extends Sensor {

    public Users() {
        name = "users";
        cmd = "echo -n \";users-#-\";w | wc -l;echo ';';";
        value = new Integer(-1);

        export = true;
    }

    @Override
    public boolean loadValue(String line) {
        String[] aux = line.split("-#-");
        for (int i = 0; i < aux.length - 1; i++) {
            if ("users".equals(aux[i])) {
                try {
                    value = Integer.parseInt(aux[++i].trim()) - 2;
                } catch (Exception e) {
                    value = new Integer(0);
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
