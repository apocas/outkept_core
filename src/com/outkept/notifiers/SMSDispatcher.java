package com.outkept.notifiers;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.outkept.Config;
import com.outkept.Outkept;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author pedrodias
 */
public class SMSDispatcher extends Thread {

    Queue<SMS> queue = new LinkedList<SMS>();
    public boolean running = false;
    private BufferedReader input = null;
    private OutputStream out = null;

    public SMSDispatcher() {
        try {
            Session session = Outkept.jsch.getSession(Config.sms_user, Config.sms_host, Config.port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "publickey");
            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("shell");
            ((ChannelShell) channel).setPty(false);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            out = channel.getOutputStream();

            channel.connect();

            input = new BufferedReader(new InputStreamReader(in));
        } catch (Exception ex) {
        }
    }

    public void sendSMS(String txt, String[] numbers) {
        queue.add(new SMS(txt, numbers));
        if (!running) {
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        running = true;
        while (!queue.isEmpty()) {
            SMS msg = (SMS) queue.remove();
            for (String number : msg.numbers) {

                try {
                    String cmd = "echo \"" + msg.msg + "\" | /usr/bin/gammu --sendsms TEXT " + number + ";echo ping\n";
                    out.write(cmd.getBytes());
                    out.flush();

                    while (!input.readLine().contains("ping")) {
                    }

                    SMSDispatcher.sleep(1000);
                } catch (Exception ex) {
                }

            }
        }
        running = false;
    }
}
