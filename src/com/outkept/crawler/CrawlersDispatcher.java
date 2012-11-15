package com.outkept.crawler;

import com.outkept.Outkept;
import com.outkept.network.AddressRange;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedrodias
 */
public class CrawlersDispatcher extends Thread {

    private boolean running = true;

    @Override
    public void run() {

        while (running) {
            ArrayList<Thread> crawlers = new ArrayList<Thread>();
            for (AddressRange ar : Outkept.ipsDomain.ranges) {
                Crawler cc = new Crawler(ar);
                crawlers.add(cc);
                cc.start();
            }

            for (Thread thread : crawlers) {
                try {
                    thread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(CrawlersDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                Thread.sleep(1000 * 60 * 180);
            } catch (InterruptedException ex) {
            }
        }
    }
}
