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

package edu.umn.natsrl.ticas.plugin.fixedmetering;

import edu.umn.natsrl.ticas.plugin.metering.*;
import edu.umn.natsrl.util.PropertiesWrapper;

/**
 *
 * @author Chongmyung Park
 */
public class MeteringConfig {
    
    private static PropertiesWrapper prop = new PropertiesWrapper();
    private static String configFile = "fixedmetering.config";
    
    public static int MAX_RATE = 1714;    // 3600/2.1 (red = 0.1s, green + yellow = 2s)
    public static int MAX_RED_TIME = 13;  // max red time = 13 second    
    public static float MIN_RED_TIME = 0.1f;  // minimum red time = 0.1 second    
    public static String CASE_FILE = "";
    public static int RANDOM_SEED = 10;
    public static boolean UseMetering = true;
    public static boolean UseCoordination = false;
    public static int stopDuration = 10;    // 5min
    public static int stopBSTrend = 3;
    public static int stopUpstreamCount = 3;
    static int BottleneckTrendCount = 2;
    
    public static float getMinRate() {
        return 3600 / (MAX_RED_TIME + 2);
    }
    
    public static void saveConfig() {
        prop.put("StopDuration", stopDuration);
        prop.put("StopBSTrend", stopBSTrend);
        prop.put("StopUpstreamCount", stopUpstreamCount);
        prop.put("MAX_RED_TIME", MAX_RED_TIME);
        prop.put("CASE_FILE", CASE_FILE);        
        prop.put("RANDOM_SEED", RANDOM_SEED);   
        prop.put("UseCoordination", UseCoordination);
        prop.save(configFile);
    }
    
    public static void loadConfig() {
        PropertiesWrapper p = PropertiesWrapper.load(configFile);
        if(p != null) {
            prop = p;
            stopDuration = prop.getInteger("StopDuration");
            stopBSTrend = prop.getInteger("StopBSTrend");
            stopUpstreamCount = prop.getInteger("StopUpstreamCount");
            MAX_RED_TIME = prop.getInteger("MAX_RED_TIME");   
            CASE_FILE = prop.get("CASE_FILE");
            RANDOM_SEED = prop.getInteger("RANDOM_SEED");
            UseCoordination = prop.getBoolean("UseCoordination");
        }
    }


            
}
