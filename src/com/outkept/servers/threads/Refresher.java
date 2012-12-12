package com.outkept.servers.threads;

import com.outkept.Outkept;
import com.outkept.crawler.CrawlerBot;
import com.outkept.servers.ServerLinux;
import com.outkept.servers.ServerMonitor;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author pedrodias
 */
public class Refresher extends Thread {

    private boolean running = true;

    public Refresher() {
    }

    @Override
    public void run() {
        System.out.println("Server refresher started.");
        while (running) {
            this.refreshServers();

            try {
                Refresher.sleep(1000 * 60 * 2);
            } catch (InterruptedException ex) {
                Logger.getLogger(Refresher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void refreshServers() {
        Map<String, String> auxx = null;
        Jedis conn = Outkept.redis.getResource();
        try {
            auxx = conn.hgetAll("macs");
        } catch (JedisException ex) {
            System.out.println("Jedis fail.");
            Outkept.redis.returnBrokenResource(conn);
        } finally {
            Outkept.redis.returnResource(conn);
        }

        conn = Outkept.redis.getResource();
        try {
            for (Map.Entry<String, String> entry : auxx.entrySet()) {
                String strLine = conn.hget(entry.getValue(), "address");
                if (strLine == null || strLine.isEmpty()) {
                    conn.del(entry.getValue());
                    conn.del(entry.getValue() + ";reactives");
                    conn.hdel("macs", entry.getKey());
                } else if (Outkept.servers.get(entry.getValue()) == null) {
                    loadServer(entry.getValue()).getServer().setHash(entry.getKey());
                }
            }
        } catch (JedisException ex) {
            System.out.println("Jedis fail.");
            Outkept.redis.returnBrokenResource(conn);
        } finally {
            Outkept.redis.returnResource(conn);
        }

        for (Iterator it = Outkept.servers.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, ServerMonitor> pairs = (Map.Entry) it.next();
            ServerMonitor sg = pairs.getValue();

            conn = Outkept.redis.getResource();
            long now = System.currentTimeMillis() / 1000;

            try {
                String aux = conn.hget(sg.getServer().getName(), "lastupdated");

                if (aux != null && (now - Long.parseLong(aux)) > 43200) {
                    System.out.println("Removing " + sg.getServer().getAddress());

                    Transaction t = conn.multi();

                    t.zadd("log", System.currentTimeMillis() / 1000, "{\"address\":\"" + sg.getServer().getAddress() + "\", \"message\":\"" + sg.getServer().getAddress() + " removed after being offline\"}");
                    t.del(sg.getServer().getAddress());
                    t.del(sg.getServer().getAddress() + ";reactives");
                    t.hdel("macs", sg.getServer().getHash());

                    t.exec();
                    it.remove();

                    sg.getServer().disconnect();

                    System.out.println("Removed " + sg.getServer().getAddress());
                }
            } catch (JedisException ex) {
                System.out.println("Jedis fail.");
                Outkept.redis.returnBrokenResource(conn);
            } finally {
                Outkept.redis.returnResource(conn);
            }
        }
    }

    public static ServerMonitor loadServer(String url) {
        Jedis conn = Outkept.redis.getResource();
        ServerMonitor sg = null;
        try {
            String addr = conn.hget(url, "address");
            if (addr != null) {
                String login[] = addr.split("@");
                boolean reactive = Boolean.parseBoolean(conn.hget(url, "reactive"));
                boolean sms = Boolean.parseBoolean(conn.hget(url, "sms"));
                String aux = conn.hget(url, "sensors");
                String[] sensors = aux.split(",");

                sg = new ServerMonitor(new ServerLinux(login[1], login[0], login[1].toUpperCase(), reactive, sensors, sms));

                if (sg.connect() || !Outkept.servers.containsKey(login[1].toUpperCase())) {
                    System.out.println("Connected to " + sg.getServer().getAddress());
                    Outkept.servers.put(login[1].toUpperCase(), sg);
                }
            }
        } catch (JedisException ex) {
            System.out.println("Jedis fail.");
            Outkept.redis.returnBrokenResource(conn);
        } finally {
            Outkept.redis.returnResource(conn);
        }


        return sg;
    }
}
