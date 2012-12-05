package com.outkept;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.outkept.network.AddressDomain;
import com.outkept.notifiers.Notifier;
import com.outkept.servers.ServerMonitor;
import com.outkept.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author pedrodias
 */
public class Outkept {

    public static ConcurrentHashMap<String, ServerMonitor> servers = new ConcurrentHashMap<String, ServerMonitor>();
    public static JedisPool redis = null;
    public static JSch jsch = null;
    public static TemplateManager sensorManager = null;
    public static AddressDomain ipsDomain = null;
    private Bootloader boot;
    public static Notifier notifier;
    private String pkey = System.getProperty("user.home") + File.separator + ".ssh/";

    public Outkept() {
        System.out.println("Outkept is booting...");

        ipsDomain = new AddressDomain();
        Config.loadConfig();

        redis = new JedisPool(new JedisPoolConfig(), Config.redis);
        jsch = new JSch();
        sensorManager = new TemplateManager();

        this.initialize();
    }

    private void initialize() {
        try {
            jsch.addIdentity(pkey + "id_rsa", Config.password);
        } catch (JSchException ex) {
            //Logger.getLogger(Outkept.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Private key missing " + pkey + "id_rsa");
            System.exit(1);
        }

        try {
            Utils.secureDelete(pkey + "id_rsa");
        } catch (IOException ex) {
            //Logger.getLogger(Outkept.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Private key missing " + pkey + "id_rsa. Add a key to " + pkey + " and restart the service.");
            System.exit(1);
        }

        Config.saveConfig();

        notifier = new Notifier();

        boot = new Bootloader();
        boot.start();

        System.out.println("Outkept booted...");
    }

    public static void main(String[] args) {

        while (Config.password == null || Config.password.isEmpty()) {
            try {
                Config.password = Utils.readFile(System.getProperty("user.home") + File.separator + ".ssh/key").trim();
            } catch (IOException ex) {
                //Logger.getLogger(Outkept.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Retrying to load key password from " + System.getProperty("user.home") + File.separator + ".ssh/key, add one line with the password. This file and key will be securely erased, they will have to be added each time the service is restarted.");
                
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException exx) {
                    Logger.getLogger(Outkept.class.getName()).log(Level.SEVERE, null, exx);
                }
            }
        }

        new Outkept();
    }
}
