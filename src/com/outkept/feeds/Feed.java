package com.outkept.feeds;

import com.outkept.Outkept;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author pedrodias
 */
public abstract class Feed extends Thread {

    protected String name;
    protected int pooling;
    protected boolean running = false;

    @Override
    public synchronized void start() {
        running = true;
        super.start();
        System.out.println("Feed " + name + " started!");
    }

    public void stopFeed() {
        running = false;
    }

    protected void fire(String identifier) {
        Jedis connr = Outkept.redis.getResource();
        try {
            connr.sadd("fired_feeds", identifier);
        } catch (JedisException e) {
            System.out.println("Jedis fail.");
            Outkept.redis.returnBrokenResource(connr);
        } finally {
            Outkept.redis.returnResource(connr);
        }
    }

    protected boolean wasFired(String identifier) {
        boolean b = true;
        Jedis connr = Outkept.redis.getResource();
        try {
            b = connr.sismember("fired_feeds", identifier);
        } catch (JedisException e) {
            System.out.println("Jedis fail.");
            Outkept.redis.returnBrokenResource(connr);
        } finally {
            Outkept.redis.returnResource(connr);
        }
        return b;
    }
}
