package com.outkept.notifiers;

import com.outkept.Config;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author pedrodias
 */
public class Notifier {

    public static final int TWITTER = 0;
    public static final int REDIS = 1;
    public static final int MAIL = 2;
    public static final int SMS = 3;
    private TwitterDispatcher twitter;
    private SMSDispatcher sms;
    private MailDispatcher mail;

    public Notifier() {
        if (Config.twitter_enable) {
            twitter = new TwitterDispatcher();
        }
        if (Config.sms_enable) {
            sms = new SMSDispatcher();
        }
        mail = new MailDispatcher();
    }

    public void notify(int type, String message) {
        switch (type) {
            case TWITTER:
                if (Config.twitter_enable) {
                    this.twitter.tweet(new SimpleDateFormat("HH:mm:ss").format(new Date()) + " " + message);
                }
                break;
            case MAIL:
                this.mail.sendMail(message, Config.notification_mail);
                break;
            case SMS:
                if (Config.sms_enable) {
                    this.sms.sendSMS(new SimpleDateFormat("HH:mm:ss").format(new Date()) + " " + message, Config.sms_numbers.split(";"));
                }
                break;
        }
    }
}
