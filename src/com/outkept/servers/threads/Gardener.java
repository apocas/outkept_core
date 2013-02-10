package com.outkept.servers.threads;

import com.outkept.Config;
import com.outkept.Outkept;
import com.outkept.notifiers.Notifier;
import com.outkept.servers.ServerMonitor;
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
public class Gardener extends Thread {

    private boolean running = true;

    public Gardener() {
        this.running = true;
    }

    @Override
    public void run() {
        System.out.println("Server gardener started.");

        while (running) {

            for (Iterator it = Outkept.servers.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, ServerMonitor> pairs = (Map.Entry) it.next();
                ServerMonitor s = pairs.getValue();

                if (s.getServer().isLoaded()) {
                    long now = System.currentTimeMillis() / 1000;

                    if (now - s.getServer().getLastUpdated() > 60) {
                        if (now - s.getServer().getLastUpdated() > 120 && !s.getServer().notified()) {
                            if (Config.sms_enable && s.getServer().hasSMS()) {
                                Outkept.notifier.notify(Notifier.SMS, s.getServer().getHostname() + "(" + s.getServer().getAddress() + ")" + " offline!");
                            }

                            Outkept.notifier.notify(Notifier.TWITTER, s.getServer().getHostname() + "(" + s.getServer().getAddress() + ")" + " OFFLINE!");

                            Jedis conn = Outkept.redis.getResource();
                            try {
                                conn.hset(s.getServer().getName(), "status", "3");
                                conn.zadd("log", System.currentTimeMillis() / 1000, "{\"address\":\"" + s.getServer().getAddress() + "\", \"message\":\"Connection lost with " + s.getServer().getAddress() + "\"}");
                            } catch (JedisException e) {
                                System.out.println("Jedis fail.");
                                Outkept.redis.returnBrokenResource(conn);
                            } finally {
                                Outkept.redis.returnResource(conn);
                            }

                            s.getServer().notifyO();
                        }

                        s.getServer().disconnect();
                        Refresher.loadServer(s.getServer().getAddress());
                    }
                }
            }

            try {
                Thread.sleep(1000 * 60 * 2);
            } catch (InterruptedException ex) {
                Logger.getLogger(Gardener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
