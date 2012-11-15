package com.outkept.feeds;

import com.outkept.utils.Utils;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;

/**
 *
 * @author pedrodias
 */
public class FeedLoader {

    public FeedLoader() {
    }

    public void load() {
        try {
            JSONObject myjson = new JSONObject(Utils.readFile("feeds.json"));
            JSONArray the_json_array = myjson.getJSONArray("rssfeeds");
            for (int i = 0; i < the_json_array.length(); i++) {
                JSONObject jo = the_json_array.getJSONObject(i);

                RSSFeed rs = new RSSFeed(jo.getString("name"), jo.getString("feed"), jo.getString("field"), jo.getBoolean("verify"), jo.getInt("interval"));
                rs.start();
            }
        } catch (Exception ex) {
            Logger.getLogger(FeedLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
