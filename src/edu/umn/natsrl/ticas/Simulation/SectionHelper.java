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
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.Exit;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimMeter.MeterType;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.simobjects.SimStation;
import edu.umn.natsrl.infra.types.TrafficType;
import java.util.ArrayList;
import java.util.HashMap;


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
    private SimObjects simObjects = SimObjects.getInstance();
    private ArrayList<SimMeter> meters;
    private ArrayList<SimDetector> detectors;
    
    public static IDetectorChecker dc = new IDetectorChecker() {

        @Override
        public boolean check(Detector d) {
            if (d.isAbandoned() || d.isHov()) {
                return false;
            }
            return true;
        }
    };
    
    public SectionHelper(Section section, ArrayList<SimDetector> detectors, ArrayList<SimMeter> meters) {
        this.section = section;
        this.detectors = detectors; 
        this.meters = meters;
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
    public double getAverageDensity(StationState upStation, StationState downStation)
    {
        return getAverageDensity(upStation, downStation, 0);
    }
    
    public double getAverageDensity(StationState upStation, StationState downStation, int prevStep)
    {
        StationState cursor = upStation;

        double totalDistance = 0;
        double avgDensity = 0;
        while(true) {
            StationState dStation = this.getDownstreamStationState(cursor.idx);
            double upDensity = cursor.getAggregatedDensity(prevStep);
            double downDensity = dStation.getAggregatedDensity(prevStep);
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
            for (SimDetector sd : detectors) {
                if (sd.getDetectorId() == d.getDetectorId()) {
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

        for (RNode rn : section.getRNodesWithExitEntrance()) {
            State prev = null;
            if (!states.isEmpty()) {
                prev = states.get(states.size() - 1);
            }

            if (rn.isStation()) {
                if (!isInMap((Station) rn)) {
                    continue;
                }
                states.add(new StationState((Station) rn));
            }
            if(rn.isEntrance()){
                EntranceState en = new EntranceState((Entrance) rn);
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

        // DEBUG
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            sb.append("[" + String.format("%02d", state.idx) + "] ");
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

    /**
     * State class to organize for metering
     */
    public class State {

        StateType type;
        public String id;
        int idx;
        int easting, northing;
        State upstream;
        State downstream;
        RNode rnode;
        double distanceToDownState = 0;

        public State(String id, RNode rnode) {
            this.id = id;
            this.rnode = rnode;
            if(rnode != null) {
                this.easting = rnode.getEasting();
                this.northing = rnode.getNorthing();
            }
            this.idx = states.size();
        }

        public boolean hasDetector(SimDetector sd) {
            if (sd == null) {
                return false;
            }
            return this.rnode.hasDetector(sd.getId());
        }

        public boolean hasDetector(SimDetector[] sds) {
            if (sds == null) {
                return false;
            }
            for (SimDetector sd : sds) {
                if (this.rnode.hasDetector(sd.getId())) {
                    return true;
                }
            }
            return false;
        }
    }

    
    
    /**
     * State class that represents station
     */
    public class StationState extends State {

        SimStation station;
        //EntranceState associatedEntrance;
        int stationIdx = 0;
        double aggregatedDensity = 0;
        double aggregatedSpeed = 0;
        
        int MOVING_U_AVG_WINDOW = 2;
        int MOVING_K_AVG_WINDOW = 2;
        
        int MAX_SPEED_ALPHA = 10;
        int lastSpeedAggCount = 0;
        double NEARBY_DISTANCE = 2000;  // 2000 feet
                
        public IDetectorChecker dc = SectionHelper.dc;

                
        public StationState(Station s) {            
            super(s.getStationId(), s);
            this.station = simObjects.getStation(s.getStationId());
            type = StateType.STATION;
            stationStates.add(this);
            this.stationIdx = stationStates.size()-1;
        }

        /**
         * Return aggregated density
         * @return 
         */
        public double getAggregatedDensity() {
            return getAggregatedDensity(0);
        }        
        public double getAggregatedDensity(int prevStep){
            return getAverageDensity(prevStep,MOVING_K_AVG_WINDOW);
        }
        /**
         * Returns aggregated density before given prevStep time step
         * @return 
         */
        public double getAverageDensity(int prevStep,int howManySteps) {
            double sum = 0;
            int validCount = 0;
            for(int i=0; i<howManySteps; i++) {
                double k = station.getData(dc, TrafficType.DENSITY, prevStep+i);
                //debug
//                double k = station.getData(dc, TrafficType.DENSITY, prevStep+i);

                if(k > 0) {
                    sum += k;
                    validCount++;
                }
            }
            if(validCount == 0 || sum < 0) return 0;
            
            return sum/validCount;
        }   
        
        public double getAggregatedSpeed() {
            return getAggregatedSpeed(0);
        }        

        public double getAggregatedSpeed(int prevStep) {
            return getAverageSpeed(prevStep, MOVING_U_AVG_WINDOW);
        }

        public double getAverageSpeed(int prevStep, int howManySteps)
        {
            double sum = 0;
            int validCount = 0;
            for(int i=0; i<howManySteps; i++) {
                double u = station.getData(dc, TrafficType.SPEED, prevStep+i);
                if(u > 0) {
                    sum += u;
                    validCount++;
                }
            }
            if(validCount == 0 || sum < 0) return 0;
            return sum/validCount;                        
        }
        
        public double getAverageFlow(int prevStep, int howManySteps){
            double sum = 0;
            int validCount = 0;
            for(int i=0; i<howManySteps; i++) {
                double q = station.getData(dc, TrafficType.AVERAGEFLOW, prevStep+i);
                if(q > 0) {
                    sum += q;
                    validCount++;
                }
            }
            if(validCount == 0 || sum < 0) return 0;
            return sum/validCount;   
        }
        
        
        /**
         * Return aggregated speed
         * @param lastSampleIndex
         * @return 
         */
        public double getAggregatedSpeed2(int lastSampleIndex) {
            double density = getAggregatedDensity();
            double usum, u30s;
            usum = u30s = station.getData(dc, TrafficType.SPEED);
            int divide = 1;
            int period = 1;

            if (density < 10) {
                this.lastSpeedAggCount = lastSampleIndex;
                return getSpeedForLowK();

            } else if (density < 15) {
                period = 6;
            } else if (density < 25) {
                period = 4;
            } else if (density < 40) {
                period = 3;
            } else if (density < 55) {
                period = 4;
            } else {
                period = 6;
            }

            // trend check
            if (density >= 15) {
                double cU = u30s;
                double pU = this.station.getData(dc, TrafficType.SPEED, 1);
                double ppU = this.station.getData(dc, TrafficType.SPEED, 2);

                // if it has trend (incrase or decrease trend)
                if ((cU >= pU && pU >= ppU) || (cU <= pU && pU <= ppU)) {
                    period = 2;
                }
            }

            divide = 1;
            int last = lastSampleIndex;
            for (int i = 1; i < period; i++) {
                if (lastSampleIndex - i < 0 || lastSampleIndex - i < this.lastSpeedAggCount) {
                    break;
                }
                usum += this.station.getData(dc, TrafficType.SPEED, i);
                last = lastSampleIndex - i;
                divide++;
            }

            this.lastSpeedAggCount = last;

            return checkMaxSpeed(usum / divide, station.getStation().getSpeedLimit());
        }

        private double checkMaxSpeed(double u, double speedLimit) {
            int alpha = MAX_SPEED_ALPHA;

            // max speed = speed limit
            if (u > speedLimit) {
                return speedLimit + alpha;
            } else {
                return u;
            }
        }

        private int getSpeedForLowK() {
            int speedLimit = this.rnode.getSpeedLimit();
            if (this.downstream == null) {
                return speedLimit;
            }
            RNode downNode = this.downstream.rnode;
            if (downNode != null && downNode.getSpeedLimit() < speedLimit) {
                return (downNode.getSpeedLimit() + speedLimit) / 2;
            }

            return speedLimit;
        }

        public double getVolume(){
            return this.station.getData(dc,TrafficType.VOLUME);
        }
        public double getTotalVolume(int prevStep, int howManySteps){
            double sum = 0;
            int validCount = 0;
            for(int i=0; i<howManySteps; i++) {
                double v = station.getData(dc, TrafficType.VOLUME, prevStep+i);
                if(v > 0) {
                    sum += v;
                    validCount++;
                }
            }
            return sum;
        }
        public double getAverageLaneFlow(){
            return this.station.getData(dc, TrafficType.AVERAGEFLOW);
        }
        public double getFlow(){
            return this.station.getData(dc,TrafficType.FLOW);
        }
        public double getTotalFlow(int prevStep, int howManySteps){
            double sum = 0;
            int validCount = 0;
            for(int i=0; i<howManySteps; i++) {
                double q = station.getData(dc, TrafficType.FLOW, prevStep+i);
                if(q > 0) {
                    sum += q;
                    validCount++;
                }
            }
            return sum;
        }
        /**
         * @deprecated 
         * @return 
         */
        public double getSpeed() {
            return this.station.getData(dc, TrafficType.SPEED);
        }

        /**
         * @deprecated 
         * @return 
         */
        public double getDensity() {
            return this.station.getData(dc, TrafficType.DENSITY);
//            return this.station.getDataForDebug(dc, TrafficType.DENSITY);
        }
    }
    
    /**
     * State class that represents entrance
     */
    public class EntranceState extends State {
        Entrance entrance;
        SimMeter meter;
        
        ArrayList<Double> cumulativeDemand = new ArrayList<Double>();
        ArrayList<Double> cumulativeMergingVolume = new ArrayList<Double>();
        ArrayList<Double> rateHistory = new ArrayList<Double>();        
        HashMap<Integer, Double> segmentDensityHistory = new HashMap<Integer, Double>(); // <dataCount, K>
        double lastDemand = 0;
        double lastVolumeOfRamp = 0;
        
        public EntranceState(Entrance e) {
            super(e.getId(), e);
            this.entrance = e;
            type = StateType.ENTRANCE;
        }
        
        /**
         * Return output in this time interval
         */        
        public double getPassageVolume() {
            return this.lastVolumeOfRamp;
        }
        
        /**
         * Return output before 'prevStep' time step
         */        
        public double getMergingVolume(int prevStep) {
            int nIdx = cumulativeMergingVolume.size() - prevStep - 1;
            int pIdx = cumulativeMergingVolume.size() - prevStep - 2;
            //System.out.println(cumulativeFlow.size() + ", " + prevStep + ", " + pIdx + ", " + nIdx);
            return cumulativeMergingVolume.get(nIdx) - cumulativeMergingVolume.get(pIdx);
        }        
        
        /**
         * Return demand in this time interval
         */
        public double getQueueVolume() {
            return this.lastDemand;
        }
        
        public double getCumulativeDemand(){
            return this.cumulativeDemand.get(cumulativeDemand.size()-1);
        }
        public double getCumulativePassage(){
            return this.cumulativeMergingVolume.get(cumulativeMergingVolume.size()-1);
        }
        
        public double getRampDensity()
        {
            if(this.cumulativeDemand.isEmpty()) return 0;
            
            int currentIdx = this.cumulativeDemand.size()-1;            
            double It = this.cumulativeDemand.get(currentIdx);
            double Ot = this.cumulativeMergingVolume.get(currentIdx);
            
            // ramp length in mile
            double L = this.meter.getMeter().getStorage() / 5280D;
            
            // if dual type, length should be double
            if(this.meter.getMeterType() == MeterType.DUAL) {
                L *= 2;
            }                    
            
            // ramp density : ( cumulative input - cumulative output ) / ramp length
            //                    vehicles in ramp at current time
            double k = (It - Ot) / L;               
//            if(this.meter.getId().equals("M62E35")){
//             System.out.println(this.meter.getId()+" : k="+k+", ( LT("+It+") - Ot("+Ot+") ) / L("+L+")");   
//            }
//            if(k >= 100) {
//                System.out.println(this.meter.getId() + " -> Kramp = " + k + "(i="+It+", o="+Ot+", i-o="+(It-Ot)+", L="+L+")" );                
//            }
            return k;
        }
        
        /**
         * Calculate demand and output
         */
        public void updateState() {
            if(this.meter == null) return;
            
            
            double p_volume = calculateRampVolume();
            double demand = calculateRampDemand();
            
            double prevCd = 0;
            double prevCq = 0;
            
            if(this.cumulativeDemand.size()>0) prevCd = this.cumulativeDemand.get(this.cumulativeDemand.size()-1);
            if(this.cumulativeMergingVolume.size()>0) prevCq = this.cumulativeMergingVolume.get(this.cumulativeMergingVolume.size()-1);
            
            this.cumulativeDemand.add(prevCd + demand);
            this.cumulativeMergingVolume.add(prevCq + p_volume);
            
            this.lastDemand = demand;                
            this.lastVolumeOfRamp = p_volume;
        }
        
        /**
         * Return ramp demand
         * @return 
         */
        private double calculateRampDemand() {
            
            if(this.meter == null) return 0;
            
            SimDetector[] qDets = this.meter.getQueue();           
            
            double demand = 0;
            double p_flow = calculateRampVolume();

            // queue detector is ok
            if(qDets != null) {
                for(int i=0; i<qDets.length; i++) {
                    double d = (int)simObjects.getDetector(qDets[i].getId()).getData(TrafficType.VOLUME);
                    if(d > 0) demand += d;
                }
                
                return demand;
            }
                        
            return p_flow;                                    
        }
        
        /**
         * Return ramp flow now
         * @return ramp flow
         */
        private double calculateRampVolume() {
            return calculateRampVolume(0);
        }
        
        /**
         * Return ramp flow before given prevStep intervals
         * @param prevStep
         * @return ramp flow
         */
        private double calculateRampVolume(int prevStep) {
            if(this.meter == null) return 0;
            SimDetector pDet = this.meter.getPassage();
            SimDetector mDet = this.meter.getMerge();
            SimDetector bpDet = this.meter.getByPass();            
            
            double p_volume = 0;           

            // passage detector is ok
            if(pDet != null) {
                p_volume = simObjects.getDetector(pDet.getId()).getData(TrafficType.VOLUME, prevStep);
            } else {
                // merge detector is ok
                if(mDet != null) {
                    p_volume = simObjects.getDetector(mDet.getId()).getData(TrafficType.VOLUME, prevStep);                      
                    // bypass detector is ok
                    if(bpDet != null) {
                        p_volume -= simObjects.getDetector(bpDet.getId()).getData(TrafficType.VOLUME, prevStep);
                        if(p_volume < 0) p_volume = 0;
                    }                                      
                }   
            }    

            return p_volume;
        }

        public void saveSegmentDensityHistory(int dataCount, double Kt) {
            this.segmentDensityHistory.put(dataCount, Kt);
        }
        
        public Double getSegmentDensity(int dataCount)
        {
            return segmentDensityHistory.get(dataCount);            
        }
        
        public String getID(){
            if(hasMeter())
                return meter.getId();
            else
                return null;
        }
        
        public String getLabel(){
            if(hasMeter())
                return meter.getMeter().getLabel();
            else return null;
        }
        
        public boolean hasMeter(){
            if(meter != null)
                return true;
            else
                return false;
        }
        
        public double getCurrentRate(){
            if(meter != null)
                return 0;
            else
                return meter.currentRate;
        }
        
        public SimMeter getSimMeter(){
            return this.meter;
        }
    }
    
    /**
     * State class that represents exit
     */
    class ExitState extends State {

        Exit exit;
        
        public ExitState(Exit e) {
            super(e.getId(), e);
            this.exit = e;
            type = StateType.EXIT;
        }     
        
    }
}
