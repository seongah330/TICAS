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

package edu.umn.natsrl.ticas.Simulation;

import edu.umn.natsrl.util.PropertiesWrapper;
import edu.umn.natsrl.util.ticasConfig;

/**
 *
 * @author Chongmyung Park
 */
public class SimulationConfig extends ticasConfig{
    
    private static String configFile = "simulation.config";
    
    public static String CASE_FILE = "";
    public static int RANDOM_SEED = 10;
    public static boolean UseMetering = true;
    public static int logtimeinterval = 0;
    
    //metering
    public static int MAX_RATE = 1714;    // 3600/2.1 (red = 0.1s, green + yellow = 2s)
    public static int MAX_RED_TIME = 13;  // max red time = 13 second    
    public static float MIN_RED_TIME = 0.1f;  // minimum red time = 0.1 second
    
    public static int getInterval(){
        switch(logtimeinterval){
            case 0 :
                return 30;
            case 1 :
                return 60;
            case 2 :
                return 120;
            case 3 :
                return 180;
            case 4 :
                return 240;
            case 5 :
                return 300;
            case 6 :
                return 600;
            case 7 :
                return 900;
            case 8 :
                return 1200;
            case 9 :
                return 1800;
            case 10 : 
                return 3600;
            default :
                return 30;
                
        }
    }
    
    public static double getCurrentRate(double rate){
        if(rate < getMinRate())
            return getMinRate();
        else if(rate > MAX_RATE)
            return MAX_RATE;
        else
            return rate;
    }
    
    public static double getMinRate() {
        return 3600 / (MAX_RED_TIME + 2);
    }
    
    public static void saveConfig() {
        prop.put("CASE_FILE", CASE_FILE);        
        prop.put("RANDOM_SEED", RANDOM_SEED);   
        saveConfig(configFile);
    }
    
    public static void loadConfig() {
        
        PropertiesWrapper p = loadConfig(configFile);
        if(p != null) {
            prop = p;
            CASE_FILE = prop.get("CASE_FILE");
            RANDOM_SEED = prop.getInteger("RANDOM_SEED");
        }
    }


            
}
