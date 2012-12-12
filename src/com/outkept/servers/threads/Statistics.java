package com.outkept.servers.threads;

import com.outkept.Outkept;
import com.outkept.sensors.Sensor;
import com.outkept.servers.ServerMonitor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author pedrodias
 */
public class Statistics extends Thread {

    private boolean running = true;

    @Override
    public void run() {
        while (running) {
            HashMap<String, Double> averages = new HashMap<String, Double>();
            HashMap<String, Integer> totals = new HashMap<String, Integer>();

            int sessions = 0;

            for (Iterator it = Outkept.servers.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, ServerMonitor> pairs = (Map.Entry) it.next();
                ServerMonitor s = pairs.getValue();

                if (s.getServer().isLoaded() && s.getServer().isConnected()) {
                    Sensor[] sensors = s.getServer().getSensors().values().toArray(new Sensor[0]);

                    sessions += (Integer) s.getServer().getSensors().get("users").getValue();

                    for (Sensor sensor : sensors) {
                        if (sensor.getValue() != null && !"lastip".equals(sensor.getName()) && !"users".equals(sensor.getName()) && !sensor.isInverted()) {
                            Double d = averages.get(sensor.name);
                            if (d == null) {
                                averages.put(sensor.name, (Double) sensor.getValue());
                            } else {
                                averages.put(sensor.name, averages.get(sensor.name) + (Double) sensor.getValue());
                            }

                            Integer i = totals.get(sensor.name);
                            if (i == null) {
                                totals.put(sensor.name, new Integer(0));
                            } else {
                                totals.put(sensor.name, totals.get(sensor.name) + new Integer(1));
                            }
                        }
                    }
                }
            }

            Jedis connr = Outkept.redis.getResource();
            try {
                connr.hset("statistics", "sessions", "" + sessions);
                for (Map.Entry<String, Double> entry : averages.entrySet()) {
                    Integer tt = totals.get(entry.getKey());

                    if (entry.getValue() != 0) {
                        connr.hset("statistics", entry.getKey(), "" + (entry.getValue() / tt));
                    } else {
                        connr.hset("statistics", entry.getKey(), "0");
                    }
                }
            } catch (JedisException e) {
                System.out.println("Jedis fail.");
                Outkept.redis.returnBrokenResource(connr);
            } finally {
                Outkept.redis.returnResource(connr);
            }

            try {
                sleep(30 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
