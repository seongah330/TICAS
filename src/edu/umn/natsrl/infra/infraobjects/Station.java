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

import edu.umn.natsrl.infra.InfraProperty;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.infra.types.StationType;
import edu.umn.natsrl.infra.types.TransitionType;
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
    
    private HashMap<String, Integer> stationFeetPoint = new HashMap<String, Integer>();
    
    //DMS Information
    private HashMap<String, DMSImpl> upstreamDMS = new HashMap<String, DMSImpl>();
    private HashMap<String, DMSImpl> downstreamDMS = new HashMap<String, DMSImpl>();
    private HashMap<String, Integer> distanceToDownstreamDMS = new HashMap<String, Integer>();    // unit = feet
    private HashMap<String, Integer> distanceToUpstreamDMS = new HashMap<String, Integer>();    // unit = feet
    StationType sType;
    
    public Station(Element ele) {
        super(ele);
        String station_id = getStationId();
        if(station_id == null || station_id.isEmpty()) return;
        
//        this.sid = Integer.parseInt(station_id.substring(1));  // S43 -> 43
        this.sid = StationType.getStationIDbyType(station_id);
        sType = StationType.getStationType(station_id);
        if(this.sid / 100 == 17) this.isWavetronicsStation = true;
        if(!sType.isOriginStation()) this.isWavetronicsStation = true;
    }

    public Station(String id, String stationId, String label, int lanes, int easting, int northing, InfraType itype) {
        this.id = id;
        this.sid = StationType.getStationIDbyType(stationId);
        sType = StationType.getStationType(stationId);
        this.infraType = itype;
        setProperty(InfraProperty.station_id,stationId);
        setProperty(InfraProperty.label,label);
        setProperty(InfraProperty.lanes,lanes);
        setProperty(InfraProperty.shift,0);
        setProperty(InfraProperty.transition,"None");
        setProperty(InfraProperty.attach_side,"right");
        setProperty(InfraProperty.s_limit,55);
        this.transitionType = TransitionType.None;
        
        this.isAvailable = true;
        this.easting = easting;
        this.northing = northing;
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
        if(this.distanceToDownstreamStation.get(sectionName) == null){
            return -1;
        }
        Integer v = this.distanceToDownstreamStation.get(sectionName);        
        return v;
    }
    
    /**
     * Return distance to downstream station in feet
     * @param sectionName
     * @return 
     */
    public int getDistanceToUpstreamStation(String sectionName) {
        if(this.distanceToUpstreamStation.get(sectionName) == null){
            return -1;
        }
        Integer v = this.distanceToUpstreamStation.get(sectionName);        
        return v;
    }    
    
    /**
     * set upstream DMS near this station
     * @param sectionName
     * @param cdms 
     */
    public void setUpstreamDMS(String sectionName, DMSImpl cdms) {
        //Add upstream DMS
        this.upstreamDMS.put(sectionName, cdms);
//        //set Distance
//        int distance = this.calculateDistance(this.getEasting(), cdms.getEasting(), this.getNorthing(), cdms.getNorthing());
//        this.setDistancetoUpstreamDMS(sectionName, distance);
    }

    /**
     * set Downstream DMS near this station
     * @param sectionName
     * @param cdms 
     */
    public void setDownstreamDMS(String sectionName, DMSImpl cdms) {
        this.downstreamDMS.put(sectionName, cdms);
//        //set Distance
//        int distance = this.calculateDistance(this.getEasting(), cdms.getEasting(), this.getNorthing(), cdms.getNorthing());
//        this.setDistancetoDownstreamDMS(sectionName, distance);
    }
    
    /**
     * get Upstream DMS near this Station
     * @param sectionName
     * @return there is no DMS - null
     */
    public DMSImpl getUpstreamDMS(String sectionName){
        return this.upstreamDMS.get(sectionName);
    }
    
    /**
     * get Downstream DMS near this Station
     * @param sectionName
     * @return there is no DMS - null
     */
    public DMSImpl getDownstreamDMS(String sectionName){
        return this.downstreamDMS.get(sectionName);
    }
    
    /**
     * set Distance to Upstream DMS from this station
     * @param sectionName
     * @param dis 
     */
    public void setDistancetoUpstreamDMS(String sectionName, int dis){
        this.distanceToUpstreamDMS.put(sectionName, dis);
    }
    
    /**
     * set Distance to Downstream DMS from this station
     * @param sectionName
     * @param dis 
     */
    public void setDistancetoDownstreamDMS(String sectionName, int dis){
        this.distanceToDownstreamDMS.put(sectionName, dis);
    }
    
    /**
     * get Distance to Upstream DMS from this station
     * @param sectionName 
     */
    public int getDistancetoUpstreamDMS(String sectionName){
        return this.distanceToUpstreamDMS.get(sectionName);
    }
    
    /**
     * get Distance to Downstream DMS from this station
     * @param sectionName 
     */
    public int getDistancetoDownstreamDMS(String sectionName){
        return this.distanceToDownstreamDMS.get(sectionName);
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

    public void setStationFeetPoint(String sectionName, int CumulativedistanceInFeet) {
        stationFeetPoint.put(sectionName, CumulativedistanceInFeet);
    }
    public int getStationFeetPoint(String sectionName){
        if(stationFeetPoint.get(sectionName) == null){
            return -1;
        }
        
        return stationFeetPoint.get(sectionName);
    }

}
