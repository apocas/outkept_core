package com.outkept.servers;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.outkept.Outkept;
import com.outkept.notifiers.Notifier;
import com.outkept.sensors.Ip;
import com.outkept.sensors.Sensor;
import com.outkept.sensors.Users;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author pedrodias
 */
public class ServerLinux extends Server implements Runnable {

    private Session session;
    private Channel channel;
    private String controller = "";

    public ServerLinux(String address, String username, String name, boolean react, String[] senss, boolean sms) {
        super(address, username, name, react, sms);

        sensors.put("users", new Users());
        sensors.put("lastip", new Ip());

        for (String sensorn : senss) {
            Sensor s;
            try {
                s = Outkept.sensorManager.getSensor(sensorn);
                sensors.put(s.getName(), s);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(ServerLinux.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void connect() throws Exception {
        try {
            session = Outkept.jsch.getSession(username, address, 22);
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

            out.write("hostname\n".getBytes());
            out.flush();
            hostname = input.readLine().trim();


            int caux = isAdaptec();
            if (caux != 0) {
                controller = "adaptec" + caux;
            } else if ((caux = isLSI()) != 0) {
                controller = "lsi" + caux;
            }

            connected = true;
            new Thread(this).start();

            loaded = true;

            Jedis connr = Outkept.redis.getResource();

            try {
                connr.hset(this.name, "hostname", hostname);
                connr.hset(this.name, "name", name);

                String aux = "lastupdated;";
                for (Sensor sensor : sensors.values()) {
                    if (sensor.isExport()) {
                        aux += sensor.getName() + ";";
                    }
                }
                connr.hset(this.name, "fields", aux);
            } catch (JedisException e) {
                System.out.println("Jedis fail.");
                Outkept.redis.returnBrokenResource(connr);
            } finally {
                Outkept.redis.returnResource(connr);
            }
        } catch (Exception ex) {
            connected = false;
            loaded = true;
            throw new Exception(ex.getMessage());
        }
    }

    public int isAdaptec() throws IOException {
        String cpanel = "if [ -f /usr/sbin/arcconf ]; then\necho yes\nelse\necho no\nfi\n";
        out.write(cpanel.getBytes());
        out.flush();

        String cp = input.readLine();
        if (cp.trim().equals("yes")) {
            return 2;
        } else {
            cpanel = "if [ -f /usr/StorMan/arcconf ]; then\necho yes\nelse\necho no\nfi\n";
            out.write(cpanel.getBytes());
            out.flush();

            cp = input.readLine();
            if (cp.trim().equals("yes")) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public int isLSI() throws IOException {
        String cpanel = "if [ -f /usr/sbin/megasasctl ]; then\necho yes\nelse\necho no\nfi\n";
        out.write(cpanel.getBytes());
        out.flush();

        String cp = input.readLine();
        if (cp.trim().equals("yes")) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void disconnect() {
        try {
            connected = false;

            updateStatus();

            if (input != null) {
                input.close();
            }

            if (out != null) {
                out.close();
            }

            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerLinux.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendCommand(String cmd) throws IOException {
        cmd += "\n";
        out.write(cmd.getBytes());
        out.flush();
    }

    @Override
    public void run() {
        while (connected) {
            try {
                char c;
                String line = "";

                do {
                    c = (char) input.read();
                    if (c != ';' && c != -1) {
                        line += c;
                    } else {
                        break;
                    }
                } while (c > 0);



                if (line != null && !line.trim().isEmpty() && line.trim().contains("-#-")) {
                    line = line.replaceAll("\\n", "").trim();
                    line = line.trim();

                    if (!globalRun(line)) {
                        for (Sensor sensor : sensors.values()) {
                            if (sensor.loadValue(line)) {
                                Jedis connr = Outkept.redis.getResource();
                                try {
                                    connr.hset(this.name, sensor.getName(), sensor.getValue().toString());
                                    this.lastupdated = System.currentTimeMillis() / 1000;
                                    connr.hset(this.name, "lastupdated", this.lastupdated + "");
                                } catch (JedisException e) {
                                    System.out.println("Jedis fail.");
                                    Outkept.redis.returnBrokenResource(connr);
                                } finally {
                                    Outkept.redis.returnResource(connr);
                                }
                                updateStatus();
                                break;
                            }
                        }

                    }
                }
            } catch (Exception ex) {
                this.disconnect();
            }
        }

        System.out.println("CONNECTION LOST WITH " + this.address);
    }

    @Override
    public void updateFields() throws IOException {
        String command = "";
        for (Sensor sensor : sensors.values()) {
            command += sensor.getCmd();
        }
        sendCommand(command);
    }

    @Override
    public void verifyRAID() {
        if (connected) {
            try {
                if ("adaptec1".equals(controller)) {
                    sendCommand("if [ -f /usr/StorMan/arcconf ]; then\ncmd=`/usr/StorMan/arcconf getconfig 1 | grep -i optimal | wc -l`\nif [ \"$cmd\" -lt \"2\" ]; then\necho \"raid-#-nok;\";\nelse\necho \"raid-#-ok;\";\nfi\nelse\necho \"raid-#-na;\";\nfi");
                } else if ("adaptec2".equals(controller)) {
                    sendCommand("if [ -f /usr/sbin/arcconf ]; then\ncmd=`/usr/sbin/arcconf getconfig 1 | grep -i optimal | wc -l`\nif [ \"$cmd\" -lt \"2\" ]; then\necho \"raid-#-nok;\";\nelse\necho \"raid-#-ok;\";\nfi\nelse\necho \"raid-#-na;\";\nfi");
                } else if ("lsi1".equals(controller)) {
                    sendCommand("if [ -f /usr/sbin/megasasctl ]; then\ncmd=`/usr/sbin/megasasctl | grep -i degraded | wc -l`\nif [ \"$cmd\" -gt \"0\" ]; then\necho \"raid-#-nok;\";\nelse\necho \"raid-#-ok;\";\nfi\nelse\necho \"raid-#-na;\";\nfi");
                }
            } catch (IOException ex) {
            }
        }
    }

    @Override
    public boolean globalRun(String line) {
        String[] aux = line.split("-#-");
        if ("raid".equals(aux[0].trim())) {
            String raid_r = aux[1].trim();

            if (raid_r.contains("nok") && !raid_alerted) {
                raid_alerted = true;
                Outkept.notifier.notify(Notifier.TWITTER, this.address + " raid degraded!");
                Outkept.notifier.notify(Notifier.MAIL, this.hostname + "(" + this.address + ")" + " raid degraded");
                Outkept.notifier.notify(Notifier.SMS, this.hostname + "(" + this.address + ")" + " raid degraded");
            } else if (!raid_r.contains("nok")) {
                raid_alerted = false;
            }
            return true;
        }
        return false;
    }
}
