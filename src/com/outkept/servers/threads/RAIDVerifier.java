package com.outkept.servers.threads;

import com.outkept.Outkept;
import com.outkept.servers.ServerMonitor;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedrodias
 */
public class RAIDVerifier extends Thread {

    private boolean running = false;

    public RAIDVerifier() {
    }

    public void start() {
        running = true;
        super.start();
    }

    @Override
    public void run() {
        while (running) {
            for (Iterator it = Outkept.servers.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, ServerMonitor> pairs = (Map.Entry) it.next();
                ServerMonitor sg = pairs.getValue();

                if (sg.getServer().isLoaded() && sg.getServer().isConnected()) {
                    sg.getServer().verifyRAID();
                }
            }

            try {
                RAIDVerifier.sleep(1000 * 3600 * 2);
            } catch (InterruptedException ex) {
                Logger.getLogger(RAIDVerifier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
