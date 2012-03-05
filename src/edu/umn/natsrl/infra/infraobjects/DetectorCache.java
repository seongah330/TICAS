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

package edu.umn.natsrl.infra.infraobjects;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.types.TrafficType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class DetectorCache {

    private final long CAPACITY = 200 * 1024 * 1024; // 100 MB
    private long memorySize = 0;
    private HashMap<String, Double[]> volume = new HashMap<String, Double[]>();
    private HashMap<String, Double[]> speed = new HashMap<String, Double[]>();
    private HashMap<String, Double[]> flow = new HashMap<String, Double[]>();
    private HashMap<String, Double[]> occupancy = new HashMap<String, Double[]>();
    private HashMap<String, Double[]> density = new HashMap<String, Double[]>();
    private HashMap<String, Double[]> scan = new HashMap<String, Double[]>();
    private HashMap<TrafficType, HashMap<String, Double[]>> dataMap = new HashMap<TrafficType, HashMap<String, Double[]>>();
    
    private HashMap<String, Double[]> cachedData = new HashMap<String, Double[]>();

    public DetectorCache() {
        dataMap.put(TrafficType.VOLUME, volume);
        dataMap.put(TrafficType.SPEED, speed);
        dataMap.put(TrafficType.FLOW, flow);
        dataMap.put(TrafficType.OCCUPANCY, occupancy);
        dataMap.put(TrafficType.DENSITY, density);
        dataMap.put(TrafficType.SCAN, scan);
    }

    public void putData(Detector detector, Period period)
    {
        // if memory size is over capacity
        if (memorySize > CAPACITY) {
            cachedData.clear();
            memorySize = 0;
        }        
        
        putData(detector.getId(), detector.getDataVector(TrafficType.VOLUME), period, TrafficType.VOLUME);
        putData(detector.getId(), detector.getDataVector(TrafficType.SPEED), period, TrafficType.SPEED);
        putData(detector.getId(), detector.getDataVector(TrafficType.OCCUPANCY), period, TrafficType.OCCUPANCY);
        putData(detector.getId(), detector.getDataVector(TrafficType.DENSITY), period, TrafficType.DENSITY);
        putData(detector.getId(), detector.getDataVector(TrafficType.FLOW), period, TrafficType.FLOW);
        putData(detector.getId(), detector.getDataVector(TrafficType.SCAN), period, TrafficType.SCAN);
        
    }
    
    private void putData(String detectorId, Vector<Double> data, Period period, TrafficType trafficType) {
        
        // create keys
        String key = getKey(detectorId, period, trafficType);
        
        // put data
        cachedData.put(key, data.toArray(new Double[data.size()])) ;

        // calculate memory size
        memorySize += data.size() * Double.SIZE * 6;

    }

    public List<Double> getData(String detectorId, Period period, TrafficType trafficType) {
        String key = getKey(detectorId, period, trafficType);
        Double[] data = cachedData.get(key);
        
        if(data == null) return null;
        return (List<Double>)Arrays.asList(data);
    }

    private String getKey(String detectorId, Period period, TrafficType trafficType) {
        return detectorId + "_" + period.getPeriodString() + "_" + period.interval+ "_" + trafficType;
    }

    public boolean hasCache(String detectorId, Period period, TrafficType trafficType) {
        if(period == null) return false;
        return cachedData.containsKey(getKey(detectorId, period, trafficType));
    }

    public void clear() {
        cachedData.clear();
    }

}
