/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth, US) and
 * Software and System Laboratory @ KNU (Kangwon National University, Korea)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.umn.natsrl.ticas.plugin.srte;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 */
public class SRTEConfig {
    
    private static SRTEConfig instance = new SRTEConfig();
    private Properties config = new Properties();
    private boolean loaded = false;
    
    public static int DATA_READ_EXTENSION_BEFORE_SNOW = 2;
    public static int DATA_READ_EXTENSION_AFTER_SNOW = 6;
    
    private SRTEConfig() {   }

    public static SRTEConfig getInstance() {
        return instance;
    }

    public int getInt(String name) { return Integer.parseInt(config.getProperty(name).toString()); }
    public double getDouble(String name) { return Double.parseDouble(config.getProperty(name).toString()); }
    public String getString(String name) { return config.getProperty(name).toString(); }
    public void set(String name, String value) { config.setProperty(name, value); }
    public void set(String name, int value) { config.setProperty(name, String.format("%d", value)); }
    public void set(String name, double value) { config.setProperty(name, String.format("%f", value)); }
    public void save() {
        try {
            config.store(new FileOutputStream("srte.config"), "SRTE Configuration");
        } catch (Exception ex) {}
    }

    public void load() {
        try {
            config.load(new FileInputStream("srte.config"));
            loaded = true;
        } catch (Exception ex) {}
    }

    public boolean isLoaded() {
        return loaded;
    }
}
