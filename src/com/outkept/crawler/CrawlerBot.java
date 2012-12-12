package com.outkept.crawler;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.outkept.Config;
import com.outkept.Outkept;
import com.outkept.sensors.LoadedSensor;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.MessageDigest;

/**
 *
 * @author pedrodias
 */
public class CrawlerBot {

    private String address;
    private BufferedReader input;
    private OutputStream out;
    private String mac;
    private String hostname;
    private Channel channel;
    private Session session;
    private String id;

    public CrawlerBot(String address) {
        this.address = address;
    }

    public String getMac() {
        return mac;
    }

    public String getID() {
        return id;
    }

    public boolean connect() {
        try {
            SocketAddress sockaddr = new InetSocketAddress(address, Config.port);
            Socket socket = new Socket();
            socket.connect(sockaddr, 2000);
            socket.close();

            session = Outkept.jsch.getSession(Config.crawler_user, address, Config.port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = session.openChannel("shell");
            ((ChannelShell) channel).setPty(false);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            out = channel.getOutputStream();

            channel.connect();

            input = new BufferedReader(new InputStreamReader(in));

            out.write("echo ping\n".getBytes());
            out.flush();

            while (!input.readLine().contains("ping")) {
            }

            String macc = "ifconfig | grep `route | grep default | awk {'print $8'}` | tr -s ' ' | cut -d ' ' -f5 | tail -1\n";
            out.write(macc.getBytes());
            out.flush();
            mac = input.readLine().trim();

            out.write("hostname\n".getBytes());
            out.flush();
            hostname = input.readLine().trim();


            if (mac.contains("00-00-00-00-00-00")) {
                mac = "00:00:00:00:00:00";
                id = mac + hostname;
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.reset();
                md.update(id.getBytes());
                BigInteger bigInt = new BigInteger(1, md.digest());
                String hashtext = bigInt.toString(16);

                while (hashtext.length() < 32) {
                    hashtext = "0" + hashtext;
                }
                id = hashtext;
            } else {
                id = mac;
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String checkSensors() throws IOException, CloneNotSupportedException {
        return CrawlerBot.checkSensors(out, input);
    }

    public static String checkSensors(OutputStream out, BufferedReader input) throws IOException, CloneNotSupportedException {
        String senss = "";
        for (Object sensor : Outkept.sensorManager.getTemplates()) {
            LoadedSensor s = (LoadedSensor) sensor;
            if (s.verifier.isEmpty()) {
                senss += s.name + ",";
            } else {
                out.write(s.verifier.getBytes());
                out.flush();

                String cp = input.readLine();
                if (cp.trim().equals("yes")) {
                    senss += s.name + ",";
                }
            }
        }
        if (!senss.isEmpty()) {
            senss = senss.substring(0, senss.length() - 1);
        }
        return senss;
    }

    public void close() {
        channel.disconnect();
        session.disconnect();
    }
}
