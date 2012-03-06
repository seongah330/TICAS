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

package edu.umn.natsrl.sfim.comm;

import edu.umn.natsrl.sfim.SFIMConfig;
import java.util.Vector;

/**
 * Data structure for saving controller information
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class InfoController {
    public InfoCommLink commlink;
    public String name;
    public int drop;
    public String cabinet;
    public String active;
    public Vector<String> IOs;
    public Vector<Integer> PINs;
    public Vector<Integer> Eastings;
    public Vector<Integer> Northings;
    public Vector<String> LaneTypes;
    public float fieldlength = 0;
    
    public InfoController() {
        IOs = new Vector<String>();
        PINs = new Vector<Integer>();
        Eastings = new Vector<Integer>();
        Northings = new Vector<Integer>();
        LaneTypes = new Vector<String>();
    }
    
    public String toString()
    {
        return "CommLink="+commlink.name + ", Ctrl Name=" + this.name + ", Drop = " + drop;
    }

    public boolean hasDetector() {
        for(Integer pin : PINs) {
            if(pin >= SFIMConfig.DETECTOR_PIN_RANGE_START && pin <= SFIMConfig.DETECTOR_PIN_RANGE_END) {
                return true;
            }
        }
        return false;
    }

}
