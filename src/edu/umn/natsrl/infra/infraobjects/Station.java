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

import edu.umn.natsrl.infra.types.StationType;
import java.util.HashMap;
import org.w3c.dom.Element;

/**
 *
 * @author Chongmyung Park
 */
public class Station extends RNode {
    
    private int sid;    // station_id as integer
    
    private HashMap<String, Station> downstreamStation = new HashMap<String, Station>();
    private HashMap<String, Station> upstreamStation = new HashMap<String, Station>();
    private HashMap<String, Integer> distanceToDownstreamStation = new HashMap<String, Integer>();    // unit = feet
    private HashMap<String, Integer> distanceToUpstreamStation = new HashMap<String, Integer>();    // unit = feet    
        
    public Station(Element ele) {
        super(ele);
        String station_id = getStationId();
        if(station_id == null || station_id.isEmpty()) return;
        
//        this.sid = Integer.parseInt(station_id.substring(1));  // S43 -> 43
        this.sid = StationType.getStationIDbyType(station_id);
        
        if(this.sid / 100 == 17) this.isWavetronicsStation = true;        
    }

    public void setDownstreamStation(String sectionName, Station s) {
        this.downstreamStation.put(sectionName, s);
        s.setUpstreamStation(sectionName, this);
    }
    
    private void setUpstreamStation(String sectionName, Station s) {
        this.upstreamStation.put(sectionName, s);
    }

    public void setDistanceToDownstreamStation(String sectionName, int distance) {
        this.distanceToDownstreamStation.put(sectionName, distance);
    }    

    public void setDistanceToUpstreamStation(String sectionName, int distance) {
        this.distanceToUpstreamStation.put(sectionName, distance);
    }        
    
    /**
     * Return distance to downstream station in feet
     * @param sectionName
     * @return 
     */
    public int getDistanceToDownstreamStation(String sectionName) {

        Integer v = this.distanceToDownstreamStation.get(sectionName);        
        return v;
    }
    
    /**
     * Return distance to downstream station in feet
     * @param sectionName
     * @return 
     */
    public int getDistanceToUpstreamStation(String sectionName) {

        Integer v = this.distanceToUpstreamStation.get(sectionName);        
        return v;
    }    
    
//    public int getDistanceToDownstreamStation(String sectionName) {
//        Station cursor = this;
//        int distance = 0;
//        while(true) {
//            Station dStation = cursor.getDownStation(sectionName);
//            distance += cursor.getDistanceToDownstreamStation(sectionName);
//            if(dStation.isAvailableStation()) break;
//            cursor = dStation;
//        }
//        return distance;
//    }    
    
    public int getStationIdAsInteger()
    {
        return this.sid;
    }

    public Station getDownStation(String sectionName) {
        return this.downstreamStation.get(sectionName);
    }

    public Station getUpStation(String sectionName) {
        return this.upstreamStation.get(sectionName);
    }

    public String toString()
    {
        return getStationId() + " (" + getLabel() + ")";
    }
    
}
