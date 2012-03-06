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

package edu.umn.natsrl.infra.simobjects;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class SimConfig {
    
    /**
     * metering status
     */
    public static final byte METER_STATUS_FLASH = 0;
    public static final byte METER_STATUS_MANUAL = 1;
    public static final byte METER_STATUS_CENTRAL = 2;
    public static final byte METER_STATUS_TOD = 3;

    /**
     * metering rates
     */
    public static final byte METER_RATE_FLASH = 0;
    public static final byte METER_RATE_CENTRAL = 1;
    public static final byte METER_RATE_TOD = 2;
    public static final byte METER_RATE_FORCED_FLASH = 7;    
    
}
