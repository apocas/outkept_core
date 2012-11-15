package com.outkept.notifiers;

import com.outkept.Config;
import java.util.LinkedList;
import java.util.Queue;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author pedrodias
 */
public class TwitterDispatcher extends Thread {

    public Twitter twitter = null;
    Queue<String> queue = new LinkedList<String>();
    public boolean running = false;

    public TwitterDispatcher() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true).setOAuthConsumerKey(Config.twitter_consumer_key).setOAuthConsumerSecret(Config.twitter_consumer_secret).setOAuthAccessToken(Config.twitter_access_token).setOAuthAccessTokenSecret(Config.twitter_token_secret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }

    public void tweet(String txt) {
        queue.add(txt);
        if (!running) {
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        running = true;
        while (!queue.isEmpty()) {
            try {
                Status status = twitter.updateStatus((String) queue.remove());
            } catch (TwitterException ex) {
            }
        }
        running = false;
    }
}
