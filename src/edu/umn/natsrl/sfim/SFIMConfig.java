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

package edu.umn.natsrl.sfim;

import edu.umn.natsrl.util.PropertiesWrapper;

/**
 *
 * @author Chongmyung Park
 */
public class SFIMConfig {

    /************************************************************************
    ** Server
    ************************************************************************/
    
    // commlink
    public static int SERVER_PORT_START = 4000;
    
    // database
    public static String IRIS_SERVER_ADDR = "131.212.105.233";
    public static int TIMESYNC_PORT = 5555;
    public static int REMOTE_OUT_PORT = 5556;
    public static int REMOTE_ERR_PORT = 5557;  
    static int IRIS_CONTROLLER_PORT = 5678;    
    public static String IRIS_DB_NAME = "tms";
    public static String IRIS_DB_USER = "tms";
    public static String IRIS_DB_PASSWD = "tms";    
    public static String LOADED_IRIS_DB_NAME = "";
    public static String LOADED_IRIS_DB_USER = "";
    public static String LOADED_IRIS_DB_PASSWD = "";    
    
    /************************************************************************
    ** MnDot Protocol
    ************************************************************************/
    
    // status code from controller 170
    static public final int STAT_OK = 0;
    
    // below status codes are not used in simulation
    static public final int STAT_BAD_MESSAGE = 1;
    static public final int STAT_BAD_POLL_CHECKSUM = 2;
    static public final int STAT_DOWNLOAD_REQUEST = 3;
    static public final int STAT_WRITE_PROTECT = 4;
    static public final int STAT_MESSAGE_SIZE = 5;
    static public final int STAT_NO_DATA = 6;
    static public final int STAT_NO_RAM = 7;
    static public final int STAT_DOWNLOAD_REQUEST_4 = 8; // 4-bit addressing    
    
    // category of requst from controller 170
    static public final int CAT_SHUT_UP = 0;
    static public final int CAT_LEVEL_1_RESTART = 1;
    static public final int CAT_SYNCHRONIZE_CLOCK = 2;
    static public final int CAT_QUERY_RECORD_COUNT = 3;
    static public final int CAT_SEND_NEXT_RECORD = 4;
    static public final int CAT_DELETE_OLDEST_RECORD = 5;
    static public final int CAT_WRITE_MEMORY = 6;
    static public final int CAT_READ_MEMORY = 7;
    
    /**
     * Packet Offsets
     *  # Memory Request
     *     + Get Request Packet Structure
     *         | Drop(5bit) | Cat(3bit) | Message Length (1byte) | Address (2byte Big-endian) | Payload Length (1byte) | Checksum (1byte) |
     * 
     *     + Set Request Packet Structure
     *         | Drop(5bit) | Cat(3bit) | Message Length (1byte) | Address (2byte Big-endian) | Payload ( ~ bytes) | Checksum (1byte) |
     * 
     *  # Memory Response
     *         | Drop(5bit) | Stat (3bit) | Message Length (1byte) | Payload ( ~ bytes) | Checksum |
     */
    static public final int OFF_DROP_CAT = 0;
    static public final int OFF_LENGTH = 1;    
    static public final int OFF_ADDRESS = 2;
    static public final int OFF_RESPONSE_PAYLOAD = 2;
    static public final int OFF_GET_REQUEST_PAYLOAD_LENGTH = 4;
    static public final int OFF_SET_REQUEST_PAYLOAD = 4;    
    
    /**
     * Memory Offsets
     */
    static public final int OFF_MEM_STATUS = 0;
    static public final int OFF_MEM_REMOTE_RATE = 3;
    static public final int OFF_MEM_METER_1 = 0;
    static public final int OFF_MEM_METER_2 = 6;
    
    // below offsets are not used in simulation
    static public final int OFF_CURRENT_RATE = 1;
    static public final int OFF_GREEN_COUNT_30 = 2;   
    static public final int OFF_POLICE_PANEL = 4;
    static public final int OFF_GREEN_COUNT_5 = 5;
    static public final int OFF_GREEN_METER_1 = 72;
    static public final int OFF_GREEN_METER_2 = 73;    

    // address for rampmeter and detector
    static public final int ADDR_METER_1_TIMING_TABLE = 0x0140;
    static public final int ADDR_METER_2_TIMING_TABLE = 0x0180;   
    static public final int ADDR_OFF_RED_TIME = 0x08;
    static public final int ADDR_OFF_PM_TIMING_TABLE = 0x1B;
    static public final int ADDR_RAMP_METER_DATA = 0x010C;
    static public final int ADDR_DATA_BUFFER_30_SECOND = 0x034B;    
    static public final int ADDR_ALARM_INPUTS = 0x5005;
    static public final int ADDR_PROM_VERSION = 0xFFF6;
    
    
    /**
     * pin range information of device
     */
    public static final int METER_1_PIN = 2;
    public static final int METER_2_PIN = 3;
    public static final int RAMPMETER_PIN_RANGE_START = 2; /// ramp meter can have pin number 2 and 3 in comm_link of IRIS
    public static final int RAMPMETER_PIN_RANGE = 2;
    public static final int RAMPMETER_PIN_RANGE_END = RAMPMETER_PIN_RANGE_START + RAMPMETER_PIN_RANGE;
    public static final int DETECTOR_PIN_RANGE_START = 39; /// detector can have pin number 39 to 62 in comm_link of IRIS
    public static final int DETECTOR_PIN_RANGE = 24;
    public static final int DETECTOR_PIN_RANGE_END = DETECTOR_PIN_RANGE_START + DETECTOR_PIN_RANGE;
    public static final int DMS_PIN_RANGE = 1;
    public static final int DMS_PIN_RANGE_START = 1;
    public static final int DMS_PIN_RANGE_END = 1;    
    
    /**
     * Constants
     */
    public static final int FEET_PER_MILE = 5280;
    public static final int MAX_SCANS = 1800;
    public static final boolean PRINT_PACKET = false;
    public static final String newline = System.getProperty("line.separator");
    
    /**
     * Parameters
     */
    public static String caseFile = "";
    public static int randomSeed = 10;
    public static long simulationTime = -1;
    public static int simulationDuration = 3;   // in hour
    //soobin jeon modify
    public static int DEFAULT_TIME_UNIT = 30;   // 30 sec (it should be multiple of 30)
    public static int SC_NUM_FOR_METERING = 100;  // signal controller starting number for metering
    public static int VISSIM_INITIAL_RUNTIME = 1800;  // it should be multiple of 30
    public static boolean USE_METERING = true; // simulate metering?
    public static boolean USE_VSA = true;
    public static Boolean DEBUG_IRIS_STATION = true;
    public static Boolean DEBUG_IRIS_METER = true;
    
    /**
     * Configuration Properties
     */
    private static PropertiesWrapper prop = new PropertiesWrapper();
        
    /**
     * Save Configurations
     */
    public static void saveConfig() {
        prop.put("SERVER_PORT_START", SERVER_PORT_START);
        prop.put("IRIS_SERVER_ADDR", IRIS_SERVER_ADDR);
        prop.put("IRIS_TIMESYNC_PORT", TIMESYNC_PORT);
        prop.put("REMOTE_OUT_PORT", REMOTE_OUT_PORT);
        prop.put("REMOTE_ERR_PORT", REMOTE_ERR_PORT);         
        prop.put("IRIS_CONTROLLER_PORT", IRIS_CONTROLLER_PORT);
        prop.put("IRIS_DB_NAME", LOADED_IRIS_DB_NAME);
        prop.put("IRIS_DB_USER", LOADED_IRIS_DB_USER);
        prop.put("IRIS_DB_PASSWD", LOADED_IRIS_DB_PASSWD);
        prop.put("caseFile", caseFile);        
        prop.put("randomSeed", randomSeed);        
        prop.put("simulationTime", simulationTime);
        prop.put("simulationDuration", simulationDuration);
        prop.save("sfim.config", "SFIM Configuration");
        
    }

    /**
     * Load Configurations
     */
    public static void loadConfig() {
        PropertiesWrapper p = PropertiesWrapper.load("sfim.config");
        if(p != null) {
            prop = p;
            SERVER_PORT_START = prop.getInteger("SERVER_PORT_START");
            IRIS_SERVER_ADDR = prop.get("IRIS_SERVER_ADDR");
            TIMESYNC_PORT = prop.getInteger("IRIS_TIMESYNC_PORT");
            REMOTE_OUT_PORT = prop.getInteger("REMOTE_OUT_PORT");
            REMOTE_ERR_PORT = prop.getInteger("REMOTE_ERR_PORT");         
            IRIS_CONTROLLER_PORT = prop.getInteger("IRIS_CONTROLLER_PORT");
            LOADED_IRIS_DB_NAME = prop.get("IRIS_DB_NAME");
            LOADED_IRIS_DB_USER = prop.get("IRIS_DB_USER");
            LOADED_IRIS_DB_PASSWD = prop.get("IRIS_DB_PASSWD");
            if(LOADED_IRIS_DB_NAME != null && !LOADED_IRIS_DB_NAME.isEmpty()) IRIS_DB_NAME = LOADED_IRIS_DB_NAME;
            if(LOADED_IRIS_DB_USER != null &&!LOADED_IRIS_DB_USER.isEmpty()) IRIS_DB_USER = LOADED_IRIS_DB_USER;
            if(LOADED_IRIS_DB_PASSWD != null &&!LOADED_IRIS_DB_PASSWD.isEmpty()) IRIS_DB_PASSWD = LOADED_IRIS_DB_PASSWD;
            caseFile = prop.get("caseFile");
            randomSeed = prop.getInteger("randomSeed");
            simulationTime = prop.getLong("simulationTime");
            simulationDuration = prop.getInteger("simulationDuration");
        }
    }
    
}
