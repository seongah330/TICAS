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

package edu.umn.natsrl.infra;

import java.io.File;

/**
 *
 * @author Chongmyung Park
 */
public class InfraConstants {

    /** reading traffic data */
    public static String TRAFFIC_DATA_URL= "http://data.dot.state.mn.us:8080/trafdat";
    public static String TRAFFIC_CONFIG_URL = "http://131.212.105.237/tms_config.xml.gz";
//    public static String TRAFFIC_CONFIG_URL = "http://data.dot.state.mn.us/dds/tms_config.xml.gz";
//    public static String TRAFFIC_CONFIG_URL = "http://data.dot.state.mn.us/iris_xml/metro_config.xml.gz";
    
    public static final String CACHE_DIR = "caches";
    public static final String SECTION_DIR = "section";
    
    public static final String CACHE_TRAFDATA_DIR = InfraConstants.CACHE_DIR+File.separator+"trafdat";
    public static final String CACHE_DETDATA_DIR = InfraConstants.CACHE_DIR+File.separator+"detdat";
    public static final String CACHE_DETFAIL_DIR = InfraConstants.CACHE_DIR+File.separator+"detfail";
    public static final String CACHE_MAP_DIR = InfraConstants.CACHE_DIR+File.separator+"map";
    
    public static final int SAMPLES_PER_HOUR = 120;
    public static final int MAX_SCANS = 1800;
    public static final int MISSING_DATA = -1;
    public static final int FEET_PER_MILE = 5280;
    public static final int SAMPLES_PER_DAY = 2880;
    /** Maximum "realistic" volume for a 30-second sample */
    public static final int MAX_VOLUME = 37;
    /** Maximum occupancy value (100%) */
    public static final int MAX_OCCUPANCY = 100;
    /** Valid density threshold for speed calculation */
    public static final double DENSITY_THRESHOLD = 1.2;

    /** Maximum (valid) speed (miles per hour) */
    public static final double MAX_SPEED = 100.0;

    /** metering status */
    public static final int FLASH = 0;
    public static final int MANUAL = 1;
    public static final int CENTRAL = 2;
    public static final int TOD = 3;

    /** metering rate */
    public static final int FLASH_RATE = 0;
    public static final int CENTRAL_RATE = 1;
    public static final int TOD_RATE = 2;
    public static final int FORCED_FLASH = 7;

    /** meter status (for simulation) */
    public static final int RAMP_GREEN = 3;
    public static final int RAMP_RED = 1; //this is for vissim attributes
    public static final float SEPARATED_CORRIDOR_DISTANCE_THRESHOLD = 20;
    
}
