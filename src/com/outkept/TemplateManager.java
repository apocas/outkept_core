package com.outkept;

import com.outkept.sensors.LoadedSensor;
import com.outkept.utils.Utils;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;

/**
 *
 * @author pedrodias
 */
public class TemplateManager {

    public HashMap<String, LoadedSensor> sensors = new HashMap<String, LoadedSensor>();

    public TemplateManager() {
        this.loadSensors();
    }

    public Object[] getTemplates() {
        return sensors.values().toArray();
    }

    private void loadSensors() {
        try {
            JSONObject myjson = new JSONObject(Utils.readFile("sensors.json"));
            JSONArray the_json_array = myjson.getJSONArray("sensors");
            for (int i = 0; i < the_json_array.length(); i++) {
                JSONObject jo = the_json_array.getJSONObject(i);

                String name = jo.getString("name");
                Double alarm = jo.getString("alarm").isEmpty() ? null : jo.getDouble("alarm");
                Double warning = jo.getString("warning").isEmpty() ? null : jo.getDouble("warning");
                boolean export = jo.getBoolean("export");
                String cmd = jo.getString("cmd");
                cmd = "echo -n \";" + name + "-#-\";" + cmd + ";echo ';';";
                String reactive = jo.getString("reactive");
                String verifier = jo.getString("verifier");
                boolean inverted = jo.getBoolean("inverted");
                boolean zero = jo.getBoolean("zero");

                sensors.put(name, new LoadedSensor(name, cmd, reactive, verifier, alarm, warning, export, inverted, zero));
            }
        } catch (Exception ex) {
            Logger.getLogger(TemplateManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public LoadedSensor getSensor(String name) throws CloneNotSupportedException {
        return (LoadedSensor) sensors.get(name).clone();
    }
}
