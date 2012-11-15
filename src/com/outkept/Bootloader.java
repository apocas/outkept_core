/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.outkept;

import com.outkept.crawler.CrawlersDispatcher;
import com.outkept.feeds.FeedLoader;
import com.outkept.servers.threads.Gardener;
import com.outkept.servers.threads.RAIDVerifier;
import com.outkept.servers.threads.Refresher;
import com.outkept.servers.threads.Statistics;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author pedrodias
 */
public class Bootloader extends Thread {

    @Override
    public void run() {
        Refresher srr = new Refresher();
        srr.start();

        RAIDVerifier rv = new RAIDVerifier();
        rv.start();

        new FeedLoader().load();

        CrawlersDispatcher sc = new CrawlersDispatcher();
        sc.start();

        Gardener sg = new Gardener();
        sg.start();

        Statistics st = new Statistics();
        st.start();

        Jedis connr = Outkept.redis.getResource();
        try {
            connr.publish("outkeptc", "reboot");
        } catch (JedisException e) {
            System.out.println("Jedis fail.");
            Outkept.redis.returnBrokenResource(connr);
        } finally {
            Outkept.redis.returnResource(connr);
        }
    }
}
