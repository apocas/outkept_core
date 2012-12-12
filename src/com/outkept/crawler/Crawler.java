package com.outkept.crawler;

import com.outkept.Config;
import com.outkept.Outkept;
import com.outkept.network.AddressIterator;
import com.outkept.network.AddressRange;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author pedrodias
 */
public class Crawler extends Thread {

    private AddressRange range;

    public Crawler(AddressRange range) {
        this.range = range;
    }

    @Override
    public void run() {
        System.out.println("Server crawler running on " + range);
        AddressIterator ai = this.range.getIterator();

        while (ai.hasNext()) {
            String address = ai.next();
            String exists = null;
            Jedis conn = Outkept.redis.getResource();
            try {
                exists = conn.hget(address, "config");
            } catch (JedisException ex) {
                System.out.println("Jedis fail.");
                Outkept.redis.returnBrokenResource(conn);
            } finally {
                Outkept.redis.returnResource(conn);
            }

            if (exists == null) {
                if (Config.debug) {
                    System.out.println("Crawler trying " + address);
                }

                CrawlerBot lc = new CrawlerBot(address);
                if (lc.connect()) {
                    conn = Outkept.redis.getResource();

                    try {
                        if (conn.hget("macs", lc.getID().toUpperCase()) == null) {

                            conn.hset("macs", lc.getID().toUpperCase(), address);

                            if (Config.debug) {
                                System.out.println("Crawler looking for sensors in " + address);
                            }

                            conn.hset(address, "sensors", lc.checkSensors());

                            System.out.println("FOUND " + Config.crawler_user + "@" + address + " - " + lc.getID());

                            conn.hset(address, "address", Config.crawler_user + "@" + address);
                            conn.hset(address, "reactive", "true");
                            conn.hset(address, "sms", "false");
                        }
                    } catch (Exception ex) {
                        System.out.println("Jedis fail.");
                        Outkept.redis.returnBrokenResource(conn);
                    } finally {
                        Outkept.redis.returnResource(conn);
                    }

                    lc.close();
                }
            }
        }
    }
}
