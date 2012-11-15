package com.outkept.servers;

import com.outkept.Config;
import com.outkept.Outkept;
import com.outkept.notifiers.Notifier;
import com.outkept.sensors.Sensor;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedrodias
 */
public class ServerMonitor implements Runnable {

    protected Server serv = null;
    protected int users = -1;
    protected ServerMonitor instance = null;

    public ServerMonitor(Server s) {
        this.serv = s;
    }

    public void playAlarm() {
        for (Sensor sensor : serv.getSensors().values()) {
            if (sensor.isAlarmed()) {
                sensor.alarm();
            }
        }
    }

    public boolean connect() {
        try {
            serv.connect();
            new Thread(this).start();
        } catch (Exception ex) {
            serv.connected = false;
            serv.updateStatus();
        }

        return serv.connected;
    }

    public void stop() {
        serv.disconnect();
    }

    /**
     * @return the serv
     */
    public Server getServer() {
        return serv;
    }

    @Override
    public void run() {
        while (serv.connected) {

            try {
                serv.updateFields();

                if (serv.isAlarmed()) {
                    serv.setPassages(serv.getPassages() + 1);
                    if (serv.getPassages() > 0 && serv.getPassages() % 3 == 0) {
                        if (serv.isReactive() && Config.reactive) {
                            serv.fireReactives();
                        }
                    }
                } else {
                    serv.setPassages(0);
                }

                if (this.getServer() instanceof ServerLinux) {
                    if (users != -1 && ((Integer) serv.getSensors().get("users").getValue()).intValue() - this.users > 0) {
                        boolean fip = false;
                        for (String iip : Config.ignored_ips) {
                            if (((String) serv.getSensors().get("lastip").getValue()).contains(iip)) {
                                fip = true;
                                break;
                            }
                        }

                        if (!fip) {
                            Outkept.notifier.notify(Notifier.TWITTER, this.getServer().hostname + " login " + ((String) serv.getSensors().get("lastip").getValue()));
                        }
                    }

                    this.users = ((Integer) serv.getSensors().get("users").getValue()).intValue();
                }

            } catch (IOException e) {
                break;
            }

            try {
                Thread.sleep(Config.timer);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serv.disconnect();

        System.out.println("CONNECTION LOST WITH " + this.getServer().address);
    }
}
