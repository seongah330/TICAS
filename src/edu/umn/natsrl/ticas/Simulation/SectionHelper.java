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

/*
 * SectionHelper.java
 *
 * Created on Jun 8, 2011, 10:26:49 AM
 */
package edu.umn.natsrl.ticas.Simulation;

import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.infraobjects.DMS;
import edu.umn.natsrl.infra.infraobjects.DMSImpl;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.Exit;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.simobjects.SimDMS;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimMeter.MeterType;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.simobjects.SimStation;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.util.DistanceUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


/**
 *
 * @author Chongmyung Park
 */
public class SectionHelper {
    
    private Section section;
    private ArrayList<State> states = new ArrayList<State>();
    private ArrayList<StationState> stationStates = new ArrayList<StationState>();
    private ArrayList<EntranceState> entranceStates = new ArrayList<EntranceState>();
    private ArrayList<ExitState> exitStates = new ArrayList<ExitState>();
    private ArrayList<DMSImpl> DMSs = new ArrayList<DMSImpl>();
    private SimObjects simObjects = SimObjects.getInstance();
    private ArrayList<SimMeter> meters;
    private ArrayList<SimDetector> detectors;
    private ArrayList<SimDMS> simDMSs;
    private SimInterval SimulationInterval;
    
    public static IDetectorChecker dc = new IDetectorChecker() {

        @Override
        public boolean check(Detector d) {
            if (d.isAbandoned() || d.isHov()) {
                return false;
            }
            return true;
        }
    };
    public SectionHelper(Section section, ArrayList<SimDetector> detectors, ArrayList<SimMeter> meters, ArrayList<SimDMS> simdms,
            SimInterval simInterval) {
        this.section = section;
        this.detectors = detectors; 
        this.meters = meters;
        simDMSs = simdms;
        SimulationInterval = simInterval;
        init();
    }

    /**
     * Returns station state according to station id
     * @param station_id
     * @return station state
     */
    public StationState getStationState(String station_id) {
        for (State s : states) {
            if (s.type.isStation() && station_id.equals(s.rnode.getStationId())) {
                return (StationState) s;
            }
        }
        return null;
    }

    /**
     * Returns all station states
     * @return station state list
     */
    public ArrayList<StationState> getStationStates() {
        return stationStates;
    }

    /**
     * Returns all entrance states
     * @return entrance state list
     */
    public ArrayList<EntranceState> getEntranceStates() {
        return getEntranceStates(false);
    }
    
    public ArrayList<EntranceState> getEntranceStates(boolean hasMeter){
        if(!hasMeter)
            return this.entranceStates;
        ArrayList<EntranceState> ess = new ArrayList<EntranceState>();
        for(EntranceState es : this.entranceStates){
            if(es.hasMeter())
                ess.add(es);
        }
        return ess;
    }
    
    public int getEntranceCount(boolean hasMeter){
        if(!hasMeter)
            return this.entranceStates.size();
        
        int cnt = 0;
        for(EntranceState es : this.entranceStates){
            if(es.hasMeter())
                cnt ++;
        }
        return cnt;
    }
    
    /**
     * Returns average density between 2 station
     * @param upStation upstream station
     * @param downStation downstream station (not need to be next downstream of upStation)
     * @return average density (distance weight)
     */
    public double getAverageDensity(StationState upStation, StationState downStation, SimulationGroup sg)
    {
        return getAverageDensity(upStation, downStation, 0,sg);
    }
    
    public double getAverageDensity(StationState upStation, StationState downStation, int prevStep,SimulationGroup sg)
    {
        StationState cursor = upStation;

        double totalDistance = 0;
        double avgDensity = 0;
        while(true) {
            StationState dStation = cursor.getDownStreamStationState();
            double upDensity = cursor.getIntervalAggregatedDensity(sg,prevStep);
            double downDensity = dStation.getIntervalAggregatedDensity(sg,prevStep);
            double middleDensity = (upDensity + downDensity) / 2;
            double distance = TMO.getDistanceInMile(cursor.rnode, dStation.rnode);
            double distanceFactor = distance / 3;
            totalDistance += distance;           
            avgDensity += (upDensity + middleDensity + downDensity) * distanceFactor;
            
            if(dStation.equals(downStation)) break;
            cursor = dStation;
        }
        return avgDensity / totalDistance;        
    }
    
    /**
     * Is station in VISSIM case file?
     * @param s station
     * @return true if given station in the case
     */
    private boolean isInMap(Station s) {
        Detector[] dets = s.getDetectors(SectionHelper.dc);
        for (Detector d : dets) {
            if(d == null){
                return false;
            }
            for (SimDetector sd : detectors) {
                if (sd != null && sd.getDetectorId() == d.getDetectorId()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Initialize
     *   - Build up section structure
     */
    private void init() {
        Vector<Station> errorStation = new Vector<Station>();
        
        for (RNode rn : section.getRNodesWithExitEntrance()) {
            State prev = null;
            if (!states.isEmpty()) {
                prev = states.get(states.size() - 1);
            }
            
            StateInterval sitv = null;
            if(SimulationInterval != null)
                    sitv = SimulationInterval.getState(rn.getId());
            
            if (rn.isStation() && rn.isAvailableStation()) {
                if (!isInMap((Station) rn)) {
                    errorStation.add((Station)rn);
                    continue;
                }
                
                StationState nss = new StationState((Station) rn, section,simObjects,SimulationInterval);
                states.add(nss);
                stationStates.add(nss);
            }
            if(rn.isEntrance()){
                EntranceState en = new EntranceState((Entrance) rn, simObjects,SimulationInterval);
                states.add(en);
                entranceStates.add(en);
            }
            if(rn.isExit()){
                ExitState ex = new ExitState((Exit) rn);
                states.add(ex);
                exitStates.add(ex);
            }
            
            if (prev != null) {
                State cur = states.get(states.size() - 1);
                prev.downstream = cur;
                cur.upstream = prev;
                prev.distanceToDownState = TMO.getDistanceInFeet(prev.rnode, rn);
            }
        }
        
        setMeters();
        setStation(errorStation);
        setDMS();
        

        // DEBUG
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            sb.append("[" + String.format("%02d", i) + "] ");
            if (state.type.isStation()) {
                StationState ss = (StationState)state;
                sb.append(((Station) state.rnode).getStationId() + " -> ");
            }
             if (state.type.isEntrance()) {
                EntranceState e = (EntranceState) state;
                if(e.meter != null) sb.append("Ent(" + e.meter.getId() + ")");
                else sb.append("Ent(" + e.rnode.getLabel() + ")");
            }
            if (state.type.isExit()) {
                sb.append("Ext(" + state.rnode.getLabel() + ")");
            }
            sb.append(" (distance to downstream = " + state.distanceToDownState + ")\n");
        }
        System.out.println(sb.toString());
    }
    
    private void CheckStationError(Vector<Station> errorStation) {
        /**
         * Check Station Error
         */
        boolean ischeck = false;
        for(Station es : errorStation){
            if(es.getStationId() != null)
                ischeck = true;
        }
        if(!errorStation.isEmpty() && ischeck){
            System.out.println("!!Warning!!");
            System.out.println("Station Information is not correct in CASEFILE");
            System.out.println("Check Detector in CASEFILE below lists");
            for(Station es : errorStation){
                if(es.getStationId() != null)
                    System.out.println("-- Station ID : "+es.getStationId()+", RNode : "+es.getId() );
            }
        }
    }
    
    /**
     * Return entrance state which include given ramp meter
     * @param meter ramp meter to find entrance
     * @return entrance state
     */
    private EntranceState findStateHasMeter(SimMeter meter) {        
        for (int i = 0; i < states.size(); i++) {
//            System.out.println(states.get(i).id);
            State state = states.get(i);
            if (!state.type.isEntrance()) {
                continue;
            }
            EntranceState s = (EntranceState) state;
            if (s.hasDetector(meter.getMerge())
                    || s.hasDetector(meter.getGreen())
                    || s.hasDetector(meter.getPassage())
                    || s.hasDetector(meter.getByPass())
                    || s.hasDetector(meter.getQueue())) {
                return s;
            }
        }
        return null;
    }
    
    /**
     * Return upstream station state
     * @param idx rnode-index of state
     * @return upstream station state
     */
    public StationState getUpstreamStationState(int idx) {
        if (idx <= 0 || idx >= states.size()) {
            return null;
        }
        for (int i = idx - 1; i >= 0; i--) {
            State st = states.get(i);
            if (st.type.isStation()) {
                return (StationState) st;
            }
        }
        return null;
    }
    
    /**
     * Return downstream station state
     * @param idx rnode-index of state
     * @return downstream station state
     */
    public StationState getDownstreamStationState(int idx) {
        if (idx < 0 || idx >= states.size() - 1) {
            return null;
        }
        for (int i = idx + 1; i < states.size(); i++) {
            State st = states.get(i);
            if (st.type.isStation()) {
                return (StationState) st;
            }
        }
        return null;
    }

    private void setMeters() {
        for (SimMeter m : meters) {
            String name = m.getId();
            if (name.contains("_L")) {
                name = name.split("_")[0];
            }
            EntranceState st = findStateHasMeter(m);            
            if (st == null) {
                System.out.println("Cannot find entrance for " + m.getId());
                continue;
            }

            st.meter = m;
        }
    }

    private void setStation(Vector<Station> errorStation) {
        CheckStationError(errorStation);
        
        //Set up the Up and the Down Stream
        for(int i = 0; i < stationStates.size();i++){
            StationState current = stationStates.get(i);
            if(i > 0){
                StationState upstream = stationStates.get(i-1);
                current.setUpstreamStationState(upstream);
            }
            
            if(i < stationStates.size() - 1){
                StationState downstream = stationStates.get(i+1);
                current.setDownStreamStationState(downstream);
            }
        }
    }

    /**
     * I think that Real DMS Name is L35xxx_1 <-L35WS53_1,  L35WS53_2
     */
    private void setDMS() {
        //setDMS
        this.DMSs = (ArrayList<DMSImpl>)section.getDMS();
        
        for(SimDMS d : simDMSs){
            DMSImpl dim = findDMS(d);
            if(dim == null){
//                System.out.println("Cannot find DMS for " + d.getId());
                continue;
            }else{
            }
            
            dim.setSimDMS(d);
        }
        
        //search error
        boolean isvslerrfirst = true;
        if(simDMSs != null && !simDMSs.isEmpty()){
            for(DMSImpl dm : DMSs){
                if(isvslerrfirst){
                    System.out.println("Desired Speed Matching Error for VSL");
                    isvslerrfirst = false;
                }
                
                if(dm.hasAllSimDMS()){
                    System.out.println(dm.getId() + " : Correct All DMS");
                    for(DMS d : dm.getDMSList()){
                        System.out.println("  ---"+d.getId()+" : "+d.hasSimDMS());
                    }
                }else{
                    System.out.println(dm.getId() + " : Incorrect DMS");
                    for(DMS d : dm.getDMSList()){
                        System.out.println("  ---"+d.getId()+" : "+d.hasSimDMS());
                    }
                }
                
            }
        }
    }

    /**
     * @param d
     * @return 
     */
    private DMSImpl findDMS(SimDMS d) {
        for(DMSImpl dm : DMSs){
            if(dm.hasDMS(d.getId())){
                return dm;
            }
        }
        
        return null;
    }
}
