package com.outkept.notifiers;

import com.outkept.Config;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author pedrodias
 */
public class MailDispatcher extends Thread {

    Queue<Mail> queue = new LinkedList<Mail>();
    public boolean running = false;

    public MailDispatcher() {
    }

    public void sendMail(String txt, String mail) {
        queue.add(new Mail(txt, mail));
        if (!running) {
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        running = true;
        while (!queue.isEmpty()) {
            Mail msg = (Mail) queue.remove();
            this.sendMailIn(msg.msg, msg.mail);
        }
        running = false;
    }

    private void sendMailIn(String msg, String to) {
        String host = Config.mail_host;
        final String user = Config.mail_user;
        final String password = Config.mail_password;
        String from = Config.mail_from;

        try {
            Properties properties = System.getProperties();
            properties.setProperty(Config.mail_host, host);
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = javax.mail.Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, password);
                        }
                    });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Outkept reporting");
            message.setText(msg);
            Transport.send(message);

        } catch (Exception ex) {
            Logger.getLogger(MailDispatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
