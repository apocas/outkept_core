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
            System.out.println("Error while loading private key " + pkey + "id_rsa");
        }

        try {
            Utils.secureDelete(pkey + "id_rsa");
        } catch (IOException ex) {
            System.out.println("Private key missing " + pkey + "id_rsa. Add a key and restart the service.");
            System.exit(1);
        }

        Config.saveConfig();

        notifier = new Notifier();

        boot = new Bootloader();
        boot.start();

        System.out.println("Outkept booted...");
    }

    public static void main(String[] args) {
        Config.version = "1.1";
        System.out.println("Outkept v" + Config.version + " - http://outke.pt\n##############");
        while (Config.password == null || Config.password.isEmpty()) {
            try {
                Config.password = Utils.readFile(System.getProperty("user.home") + File.separator + ".ssh/key").trim();
                Utils.secureDelete(System.getProperty("user.home") + File.separator + ".ssh/key");
            } catch (IOException ex) {
                System.out.println("Will retry to load key's passphrase from " + System.getProperty("user.home") + File.separator + ".ssh/key in 30 seconds.");
                System.out.println("Fill the file's first line with the passphrase..");

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException exx) {
                    Logger.getLogger(Outkept.class.getName()).log(Level.SEVERE, null, exx);
                }
            }
        }

        new Outkept();
    }
}
