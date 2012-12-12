package com.outkept;

import com.outkept.utils.Utils;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;

/**
 *
 * @author pedrodias
 */
public class Config {

    public static String password = "";
    public static int timer = 10000;
    public static boolean reactive = true;
    public static int alarm = 2;
    public static int port = 22;
    public static int timeout = 10;
    public static String sms_numbers = "";
    public static boolean sms_enable = false;
    public static boolean twitter_enable = false;
    public static String twitter_consumer_key = "";
    public static String twitter_consumer_secret = "";
    public static String twitter_access_token = "";
    public static String twitter_token_secret = "";
    public static String crawler_user = "";
    public static boolean debug = true;
    public static String redis;
    public static String web_user;
    public static String web_password;
    public static String sms_host;
    public static String sms_user;
    public static String mail_host;
    public static String mail_user;
    public static String mail_password;
    public static String mail_from;
    public static String[] ignored_ips;
    public static String notification_mail;
    public static boolean recheck_sensors = true;

    public static void loadConfig() {
        try {
            JSONObject myjson = new JSONObject(Utils.readFile("config.json"));

            Config.redis = myjson.getString("redis");
            Config.port = Integer.parseInt(myjson.getString("port"));
            Config.web_user = myjson.getString("web_user");
            Config.web_password = myjson.getString("web_password");
            Config.mail_host = myjson.getString("mail_host");
            Config.mail_user = myjson.getString("mail_user");
            Config.mail_password = myjson.getString("mail_password");
            Config.mail_from = myjson.getString("mail_from");
            Config.notification_mail = myjson.getString("notification_mail");
            Config.twitter_enable = Boolean.parseBoolean(myjson.getString("twitter_enable"));
            if (Config.twitter_enable) {
                Config.twitter_consumer_key = myjson.getString("twitter_consumer_key");
                Config.twitter_consumer_secret = myjson.getString("twitter_consumer_secret");
                Config.twitter_access_token = myjson.getString("twitter_access_token");
                Config.twitter_token_secret = myjson.getString("twitter_token_secret");
            }
            Config.sms_enable = Boolean.parseBoolean(myjson.getString("sms_enable"));
            if (Config.sms_enable) {
                Config.sms_numbers = myjson.getString("sms_numbers");
                Config.sms_host = myjson.getString("sms_host");
                Config.sms_user = myjson.getString("sms_user");
            }
            Config.timeout = Integer.parseInt(myjson.getString("timeout"));
            Config.alarm = Integer.parseInt(myjson.getString("alarm"));
            Config.reactive = Boolean.parseBoolean(myjson.getString("reactive"));
            Config.timer = Integer.parseInt(myjson.getString("timer"));
            Config.crawler_user = myjson.getString("crawler_user");
            Config.debug = Boolean.parseBoolean(myjson.getString("debug"));
            Config.recheck_sensors = Boolean.parseBoolean(myjson.getString("recheck_sensors"));

            JSONArray the_json_array = myjson.getJSONArray("ranges");
            for (int i = 0; i < the_json_array.length(); i++) {
                JSONObject jo = the_json_array.getJSONObject(i);
                Outkept.ipsDomain.addRange(jo.getString("range"));
            }

            the_json_array = myjson.getJSONArray("ignored_access_ips");
            Config.ignored_ips = new String[the_json_array.length()];
            for (int i = 0; i < the_json_array.length(); i++) {
                JSONObject jo = the_json_array.getJSONObject(i);
                Config.ignored_ips[i] = jo.getString("ip");
            }
        } catch (Exception ex) {
            System.out.println("Configuration file missing (config.json) or with errors. Next message will give you a hint about the problematic variable.");
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    public static void saveConfig() {
        Jedis connr = Outkept.redis.getResource();

        try {
            connr.hset("config", "web_user", Config.web_user);
            connr.hset("config", "web_password", Config.web_password);
        } catch (JedisException e) {
            System.out.println("Jedis fail.");
            Outkept.redis.returnBrokenResource(connr);
        } finally {
            Outkept.redis.returnResource(connr);
        }
    }
}
