/*
 * (C) Copyright Tretyakov Roman.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package ru.trett.vklient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class Config {

    /**
     * Check existing file for store and create new one if not
     */
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

    /**
     * Set new property
     *
     * @param key   Key for property
     * @param value Property value
     */
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

    /**
     * Get value of the property by key
     *
     * @param key Property key
     * @return String property value
     */
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
