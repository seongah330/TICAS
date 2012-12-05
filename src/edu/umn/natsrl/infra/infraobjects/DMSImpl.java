/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth) and
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

import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.InfraProperty;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.util.DistanceUtil;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class include DMS devices which have Same Device ID
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class DMSImpl extends InfraObject implements Comparable{
    //private data
    private HashMap<String,DMS> dms = new HashMap<String,DMS>();
    private HashMap<String, DMSImpl> downstreamDMS = new HashMap<String, DMSImpl>();
    private HashMap<String, DMSImpl> upstreamDMS = new HashMap<String, DMSImpl>();
    private HashMap<String, Integer> distanceToDownstreamDMS = new HashMap<String, Integer>();    // unit = feet
    private HashMap<String, Integer> distanceToUpstreamDMS = new HashMap<String, Integer>();    // unit = feet
    
    private HashMap<String, Integer> feetPoint = new HashMap<String, Integer>();
//    private HashMap<String, Station> downstreamStation = new HashMap<String, Station>();
//    private HashMap<String, Station> upstreamStation = new HashMap<String, Station>();
//    private HashMap<String, Integer> distanceToDownstreamStation = new HashMap<String, Integer>();    // unit = feet
//    private HashMap<String, Integer> distanceToUpstreamStation = new HashMap<String, Integer>();    // unit = feet
    
    private int distanceFromFirstStation = 0;
    private boolean isdesc = false;

    Corridor corridor;
    int easting = -1;
    int northing = -1;
    private boolean isVSAStarted = false;
    private int speedlimit = 0;

    public DMSImpl(){
        this.infraType = InfraType.DMSImpl;
    }
    public DMSImpl(String _id, HashMap<String, DMS> _dms){
        this();
        this.id = _id;
        dms = _dms;
        setProperty(InfraProperty.name, _id);
        adjustDMS();
    }

    /**
     * set Distance From First Station to adjust DMS location
     * @param distancetoFStation 
     */
    public void setDistanceFromFirstStation(int distancetoFStation) {
        distanceFromFirstStation = distancetoFStation;
    }

    /**
     * get Distance from first station in Corridor
     * @return 
     */
    public int getDistanceFromFirstStation(){
        return distanceFromFirstStation;
    }

    /**
    * adjust DMS
    * setup DMS information
    */
    private void adjustDMS() {
        if(getDMSList().length <= 0)
            return;

        DMS cdms = getDMSList()[0];

        for(DMS d : getDMSList()){
            d.setDMSImpl(this);
            if(d.getGID() == 1 && d.getEasting() != -1 && d.getNorthing() != -1){
                easting = d.getEasting();
                northing = d.getNorthing();
            }
        }
    }

    /**
     * has same DMS Device with Key
     * @param key
     * @return 
     */
    public boolean hasDMS(String key){
        if(dms.get(key) != null){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int compareTo(Object o) {
        DMSImpl cdata = (DMSImpl)o;
        if(isdesc){ // 4 - 3 - 2 - 1
            if(distanceFromFirstStation == cdata.getDistanceFromFirstStation())
                return 0;
            else if(distanceFromFirstStation < cdata.getDistanceFromFirstStation())
                return 1;
            else 
                return -1;
        }else{ // 1 - 2 - 3 - 4
            if(distanceFromFirstStation == cdata.getDistanceFromFirstStation())
                return 0;
            else if(distanceFromFirstStation > cdata.getDistanceFromFirstStation())
                return 1;
            else 
                return -1;
        }
    }

    /**
     * set Corridor
     * @param co 
     */
    public void setCorridor(Corridor co) {
        this.corridor = co;
        setProperty(InfraProperty.CorridorId, co.getId());
    }
    
    public Corridor getCorridor(){
        return corridor;
    }
    
    public DMS[] getDMSList(){
        Vector<DMS> dlist = new Vector<DMS>();
        for(DMS d : this.dms.values()){
            dlist.add(d);
        }
        return dlist.toArray(new DMS[dlist.size()]);
    }
    
    public int getEasting(){
        return easting;
    }
    public int getNorthing(){
        return northing;
    }
    
    public void setUpstreamDMS(String sectionName, DMSImpl udms){
        this.upstreamDMS.put(sectionName, udms);
//        this.setDistancetoUpstreamDMS(sectionName, calculateDistanceToOtherDMS(udms));
    }
    
    public void setDownstreamDMS(String sectionName, DMSImpl ddms){
        this.downstreamDMS.put(sectionName, ddms);
//        this.setDistancetoDownstreamDMS(sectionName, calculateDistanceToOtherDMS(ddms));
    }
    
    public DMSImpl getUpstreamDMS(String sectionName){
        return this.upstreamDMS.get(sectionName);
    }
    
    public DMSImpl getDownstreamDMS(String sectionName){
        return this.downstreamDMS.get(sectionName);
    }
    
    public void setDistancetoUpstreamDMS(String sectionName, int dis){
        this.distanceToUpstreamDMS.put(sectionName, dis);
    }
    
    public void setDistancetoDownstreamDMS(String sectionName, int dis){
        this.distanceToDownstreamDMS.put(sectionName, dis);
    }
    
    public int getDistancetoUpstreamDMS(String sectionName){
        if(this.distanceToUpstreamDMS.get(sectionName) == null){
            return -1;
        }
        return this.distanceToUpstreamDMS.get(sectionName);
    }
    
    public int getDistancetoDownstreamDMS(String sectionName){
        if(this.distanceToDownstreamDMS.get(sectionName) == null){
            return -1;
        }
        return this.distanceToDownstreamDMS.get(sectionName);
    }

    public int calculateDistanceToOtherDMS(DMSImpl odms) {
        return this.calculateDistance(this.getEasting(), odms.getEasting(), this.getNorthing(), odms.getNorthing());
    }
    
    public void setFeetPoint(String sectionName, int distance){
        this.feetPoint.put(sectionName, distance);
    }
    public int getFeetPoint(String sectionName){
        if(feetPoint.get(sectionName)==null){
            return -1;
        }
        return feetPoint.get(sectionName);
    }
    
    public Double getMilePoint(String sectionName){
        return DistanceUtil.getFeetToMile(getFeetPoint(sectionName));
    }

//    public void setUpstreamStation(String sectionName, Station st){
//        this.upstreamStation.put(sectionName, st);
//        int distance = this.calculateDistance(this.getEasting(), st.getEasting(), this.getNorthing(), st.getNorthing());
//        this.setDistanceToUpstreamStation(sectionName, distance);
//    }
//    public void setDownstreamStation(String sectionName, Station st){
//        this.downstreamStation.put(sectionName, st);
//        int distance = this.calculateDistance(this.getEasting(), st.getEasting(), this.getNorthing(), st.getNorthing());
//        this.setDistanceToDownstreamStation(sectionName, distance);
//    }
//    void setDistanceToUpstreamStation(String sectionName, int distance) {
//        this.distanceToUpstreamStation.put(sectionName, distance);
//    }
//
//    void setDistanceToDownstreamStation(String sectionName, int distance) {
//        this.distanceToDownstreamStation.put(sectionName, distance);
//    }
//    public Station getUpstreamStation(String sectionName){
//        return this.upstreamStation.get(sectionName);
//    }
//    public Station getDownstreamStation(String sectionName){
//        return this.downstreamStation.get(sectionName);
//    }
//    public int getDistancetoUpstreamStation(String sectionName){
//        return this.distanceToUpstreamStation.get(sectionName);
//    }
//    public int getDistancetoDownstreamStation(String sectionName){
//        return distanceToDownstreamStation.get(sectionName);
//    }

    /**
     * @deprecated 
     * not implements
     * @param setSpeed 
     */
    public void setVSA(Integer setSpeed) {
        if(setSpeed == null){
            isVSAStarted = false;
            speedlimit = -1;
        }else{
            isVSAStarted = true;
            speedlimit = setSpeed;
        }
    }

    public boolean isStarted() {
        return isVSAStarted;
    }

    public Integer getSpeedLimit() {
        return speedlimit;
    }
    
}
