package com.outkept.servers;

import com.outkept.Outkept;
import com.outkept.notifiers.Notifier;
import com.outkept.sensors.Sensor;
import java.io.*;
import java.util.HashMap;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author pedrodias
 */
public abstract class Server {

    protected String address = null;
    protected String username = null;
    protected BufferedReader input;
    protected boolean connected = false;
    protected OutputStream out;
    protected int retries = 0;
    protected boolean alarm_triggered = false;
    protected String name = "";
    protected ServerMonitor sgui = null;
    protected boolean raid_alerted = false;
    protected HashMap<String, Sensor> sensors = new HashMap<String, Sensor>();
    protected int passages = 0;
    protected boolean reactive = false;
    protected boolean sms = false;
    protected String hostname = "";
    protected boolean loaded = false;
    protected String hash;
    protected long lastupdated;
    private boolean notified = false;

    public Server(String address, String username, String name, boolean react, boolean sms) {
        this.address = address;
        this.username = username;
        this.name = name;
        this.reactive = react;
        this.sms = sms;
    }

    public abstract void connect() throws Exception;

    public void setRAIDStatus(boolean status) {
        this.raid_alerted = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Server && this.address.equals(((Server) o).address)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hashh = 3;
        hashh = 59 * hashh + (this.address != null ? this.address.hashCode() : 0);
        return hashh;
    }

    public abstract void disconnect();

    public abstract void sendCommand(String cmd) throws IOException;

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    public String getHostname() {
        return hostname;
    }

    public Long getLastUpdated() {
        return lastupdated;
    }

    public boolean hasSMS() {
        return sms;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getName() {
        return name;
    }

    public boolean isReactive() {
        return reactive;
    }

    public void fireReactives() throws IOException {
        for (Sensor sensor : sensors.values()) {
            if (sensor.isAlarmed() && connected && !sensor.isFired()) {
                Jedis conn = Outkept.redis.getResource();

                try {
                    Transaction t = conn.multi();
                    long tt = System.currentTimeMillis() / 1000;
                    t.zadd(this.address + ";reactives", tt, "{\"time\":\"" + tt + "\", \"sensor\":\"" + sensor.getName() + "\", \"value\":\"" + sensor.getValue() + "\"}");
                    t.zadd("log", tt, "{\"time\":\"" + tt + "\", \"address\":\"" + this.address + "\", \"message\":\"" + this.address + " triggered " + sensor.getName() + " sensor with " + sensor.getValue() + "\",\"sensor\":\"" + sensor.getName() + "\"}");
                    t.exec();
                } catch (JedisException ex) {
                    System.out.println("Jedis fail.");
                    Outkept.redis.returnBrokenResource(conn);
                } finally {
                    Outkept.redis.returnResource(conn);
                }

                sensor.fire();

                if (sensor.getReactive() != null && !sensor.reactive.isEmpty() && !sensor.isZeroed()) {
                    sendCommand(sensor.getReactive());

                    if (sensor.isTwitter()) {
                        Outkept.notifier.notify(Notifier.TWITTER, this.hostname + " " + sensor.getName() + " reactive.");
                    }
                }
            }
        }
    }

    public boolean isAlarmed() {
        for (Sensor sensor : sensors.values()) {
            if (sensor.isAlarmed()) {
                return true;
            }
        }
        return false;
    }

    public HashMap<String, Sensor> getSensors() {
        return sensors;
    }

    public int getPassages() {
        return passages;
    }

    public void setPassages(int n) {
        passages = n;
    }

    public boolean isWarn() {
        for (Sensor sensor : sensors.values()) {
            if (sensor.isWarn()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }

    public abstract void updateFields() throws IOException;

    /**
     * @return the retries
     */
    public int getRetries() {
        return retries;
    }

    /**
     * @param retries the retries to set
     */
    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * @return the sgui
     */
    public ServerMonitor getGUI() {
        return sgui;
    }

    /**
     * @param sgui the sgui to set
     */
    public void setGUI(ServerMonitor sgui) {
        this.sgui = sgui;
    }

    public abstract void verifyRAID();

    public abstract boolean globalRun(String line);

    protected void updateStatus() {
        Jedis conn = Outkept.redis.getResource();
        try {
            if (!this.connected) {
                conn.hset(this.name, "status", "3");
            } else if (this.isAlarmed()) {
                conn.hset(this.name, "status", "2");
            } else if (this.isWarn()) {
                conn.hset(this.name, "status", "1");
            } else {
                conn.hset(this.name, "status", "0");
            }
        } catch (JedisException e) {
            System.out.println("Jedis fail.");
            Outkept.redis.returnBrokenResource(conn);
        } finally {
            Outkept.redis.returnResource(conn);
        }
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String key) {
        hash = key;
    }

    public boolean notified() {
        return this.notified;
    }

    public void notifyO() {
        this.notified = true;
    }
}
