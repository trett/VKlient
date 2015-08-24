/*
 *  *
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

package ru.trett.vklient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by maat on 24.08.15.
 */
public class Config {


    public void checkStore() {
        File configFile = new File("vklient.properties");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setValue(String key, String value) {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream("vklient.properties"));
            config.put(key, value);
            FileOutputStream out = new FileOutputStream("vklient.properties");
            config.store(out, "account settings");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getValue(String key) {
        try {
            Properties config = new Properties();
            config.load(new FileInputStream("vklient.properties"));
            String value = config.getProperty(key);
            return value;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
