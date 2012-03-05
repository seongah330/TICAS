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
package edu.umn.natsrl.ticas.plugin.metering;

import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.Exit;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.simobjects.SimConfig;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimMeter.MeterType;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.simobjects.SimStation;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.util.Logger;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author Chongmyung Park
 */
public class MeteringSectionHelper {
    
    private Section section;
    private ArrayList<State> states = new ArrayList<State>();
    private ArrayList<StationState> stationStates = new ArrayList<StationState>();
    private ArrayList<EntranceState> entranceStates = new ArrayList<EntranceState>();
    private ArrayList<ExitState> exitsStates = new ArrayList<ExitState>();
    private SimObjects simObjects = SimObjects.getInstance();
    private ArrayList<SimMeter> meters;
    private ArrayList<SimDetector> detectors;
    
    public float MAX_RATE = MeteringConfig.MAX_RATE;    // 3600/2.1 (red = 0.1s, green + yellow = 2s)
    public float MIN_RATE = MeteringConfig.getMinRate();    // 3600/2.1 (red = 0.1s, green + yellow = 2s)
    public int MAX_RAMP_DENSITY = MeteringConfig.MAX_RAMP_DENSITY;
    public double PASSAGE_DEMAND_FACTOR = MeteringConfig.PASSAGE_DEMAND_FACTOR;
    public double MIN_RED_TIME = MeteringConfig.MIN_RED_TIME;  // minimum red time = 0.1 second
    
    
    public MeteringSectionHelper(Section section, ArrayList<SimMeter> meters, ArrayList<SimDetector> detectors) {
        this.section = section;
        this.meters = meters;
        this.detectors = detectors; 
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
        return entranceStates;
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
        Detector[] dets = s.getDetectors(Metering.dc);
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
            if (rn.isEntrance()) {
                states.add(new EntranceState((Entrance) rn));
            }
            if (rn.isExit()) {
                states.add(new ExitState((Exit) rn));
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
        
        // FIXME: automate this routine!!
        EntranceState toRemove = null;
        for(StationState s : this.stationStates) {
            s.setAssociatedEntrances();
            /*
            if(s.id.equals("S43")) {
                for(EntranceState es : s.getAssociatedEntrances()) {
                    if(es.meter.getId().equals("M35WN27")) {
                        toRemove = es;
                        break;
                    }
                }
                s.associatedEntrances.remove(toRemove);
            }
            if(s.id.equals("S34")) {
                s.associatedEntrances.add(toRemove);
            }
             */
        }

        
        
        // DEBUG
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            sb.append("[" + String.format("%02d", state.idx) + "] ");
            if (state.type.isStation()) {
                StationState ss = (StationState)state;
                sb.append(((Station) state.rnode).getStationId() + " -> ");
                for(EntranceState es : ss.getAssociatedEntrances()) {
                    if(es != null && es.meter != null) sb.append(es.meter.getId() + ", ");
                }
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
     * State class to organize for metering
     */
    class State {

        StateType type;
        String id;
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
     * State class that represents entrance
     */
    class EntranceState extends State {
        Entrance entrance;
        SimMeter meter;
        StationState associatedStation;
        DIR associatedStationDir;
        
        ArrayList<Double> cumulativeDemand = new ArrayList<Double>();
        ArrayList<Double> cumulativeMergingVolume = new ArrayList<Double>();
        ArrayList<Double> rateHistory = new ArrayList<Double>();        
        HashMap<Integer, Double> segmentDensityHistory = new HashMap<Integer, Double>(); // <dataCount, K>
        double lastDemand = 0;
        double lastVolumeOfRamp = 0;
        double lastRate = 0;
        boolean isMetering = false;
        boolean isRateUpdated = false;
        boolean isActiveMetering = false;
        private double minimumRate = 0;
        int maxWaitTimeIndex = MeteringConfig.MAX_WAIT_TIME_INDEX;
        private int noBottleneckCount;
        private StationState bottleneck;
        public boolean hasBeenStoped = false;

        public EntranceState(Entrance e) {
            super(e.getId(), e);
            this.entrance = e;
            type = StateType.ENTRANCE;
            entranceStates.add(this);
            String thislabel = e.getLabel();
            
            // check if it is freeway-to-freeway entrance
            for(Corridor c : TMO.getInstance().getInfra().getCorridors()) {
                if(c.getId().contains(thislabel)) {
                    maxWaitTimeIndex = MeteringConfig.MAX_WAIT_TIME_INDEX_F2F;
                    break;
                }
            }
        }
        
        /**
         * Return output in this time interval
         */        
        public double getMergingVolume() {
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
        public double getDemandVolume() {
            return this.lastDemand;
        }
        
        /**
         * Return rate in previous time step
         */
        public double getRate() {
            
            // initial rate = average(last 3 flows) or MAX_RATE
            if(this.lastRate == 0 ) {
//                this.lastRate = MAX_RATE;
                this.lastRate = this.lastVolumeOfRamp * 120;
                double flowSum = 0;
                int cnt = 0;
                
                for(int i=0; i<3; i++) {
                    flowSum += calculateRampVolume(i) * 120;
                    cnt++;
                }
                
                if(cnt > 0) this.lastRate = flowSum / cnt;
                else this.lastRate = MAX_RATE;  // no flow
            }
            
            return this.lastRate;
        }
        
        public double getRate(int prevStep) {
            int index = rateHistory.size() - prevStep - 1;
            return rateHistory.get(index);
        }  
        
        public int countRateHistory()
        {
            return rateHistory.size();
        }               
        
        /**
         * Return minimum rates to guarantee 
         *     - max waiting time
         *     - max density 
         * @return minimum rates
         */
        public double getMinimumRate() {
            return this.minimumRate;
        }
        
        /**
         * Calculate max wait time
         */
        private void calculateMaxWaitTime() {
            // line graph between two points : y = ((y2 - y1) / (x2 - x1)) * (x - x1) + y2;
            // if Kramp < 100
            //   then, MaxWaitTime = 4 min
            // else 
            //   MaxWaitTime = ((1min - 4min) / (200 - 100)) * (Kramp - 100) + 4min;
            
            // parameters
            int MaxTime = 4;
            int MinTime = 1;
            double MaxDensity = 200;
            double DensityThreshold = 100;
            
            // cumulative input and output vehicles at ramp
            int currentIdx = this.cumulativeDemand.size()-1;
            if(currentIdx < maxWaitTimeIndex) return;

            // ramp density : ( cumulative input - cumulative output ) / ramp length
            //                    vehicles in ramp at current time
            double Kramp = this.getRampDensity();
                        
            if(Kramp < DensityThreshold) {
                this.maxWaitTimeIndex = (MaxTime * 2) - 1;
                //System.out.println(this.meter.getId() + " -> MaxWaitTimeIndex(1) : " + maxWaitTimeIndex);                
                return;
            }
            
            // find point at line graph
            double waitTimePoint = (MinTime - MaxTime) / (MaxDensity - DensityThreshold) * (Kramp - DensityThreshold) + MaxTime;            
            System.out.println(this.meter.getId() + " -> WaitTimePoint (0) : " + waitTimePoint);
            
            // round up by 0.5
            waitTimePoint = Math.ceil(waitTimePoint * 2) / 2;
            System.out.println(this.meter.getId() + " -> WaitTimePoint (1) : " + waitTimePoint);
            
            // check max value
            waitTimePoint = Math.min(waitTimePoint, MaxTime);
            waitTimePoint = Math.max(MinTime, waitTimePoint);
            System.out.println(this.meter.getId() + " -> WaitTimePoint (2) : " + waitTimePoint);
            
            // convert to time index
            this.maxWaitTimeIndex = (int)(waitTimePoint * 2) - 1;                
            System.out.println(this.meter.getId() + " -> MaxWaitTimeIndex(2) : " + maxWaitTimeIndex);
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
            if(k >= 100) {
                System.out.println(this.meter.getId() + " -> Kramp = " + k + "(i="+It+", o="+Ot+", i-o="+(It-Ot)+", L="+L+")" );                
            }
            return k;
        }
        
        /**
         * Calculate minimum rate
         */
        private void calculateMinimumRate() {

            //calculateMaxWaitTime();
            
            int currentIdx = this.cumulativeDemand.size()-1;
            if(currentIdx < maxWaitTimeIndex) {
                minimumRate = this.getMergingVolume() * 120;
                return;
            }
                                   
            // cumulative demand 4 min ago
            double Cd_4mAgo = this.cumulativeDemand.get(currentIdx - maxWaitTimeIndex);
            
            // current cumulative demand
            double Cd_current = this.cumulativeDemand.get(currentIdx);
            
            // current cumulative passage flow
            double Cf_current = this.cumulativeMergingVolume.get(currentIdx);
            
            // minimum rates to guarantee 4 min waitting time
            minimumRate = ( Cd_4mAgo - Cf_current ) * 120;
            
            // DEBUG
            //if(minimumRate > 0) {
                System.out.println(
                        "    - "+ this.meter.getId()+" Minimum Rate = " + String.format("%.2f", minimumRate) + 
                        ", Ci = "+String.format("%.2f", Cd_current) +                        
                        ", Ci4 = " + String.format("%.2f", Cd_4mAgo) + 
                        ", Co = "+ String.format("%.2f", Cf_current) +
                        ", Input Volume = "+ String.format("%.2f", this.getDemandVolume()) +
                        ", Output Volume = "+ String.format("%.2f", this.getMergingVolume())

                );                 
            //}
            
//            if(minimumRate > 0) {
//                // expected ramp density with calculated min rates
//                double rampExpectedDensity = getExpectedRampDensity(minimumRate);
//
//                // DEBUG
//                System.out.println(
//                        "    - "+ this.meter.getId()+" Minimum Rate = " + String.format("%.2f", minimumRate) + 
//                        ", ExpectedDensity = " + String.format("%.2f", rampExpectedDensity) + 
//                        ", Ci = "+String.format("%.2f", Cd_current) +                        
//                        ", Ci4 = " + String.format("%.2f", Cd_4mAgo) + 
//                        ", Co = "+ String.format("%.2f", Cf_current) +
//                        ", Input Volume = "+ String.format("%.2f", this.getDemandVolume()) +
//                        ", Output Volume = "+ String.format("%.2f", this.getMergingVolume())
//
//                );            
//
//
//                // if expacted ramp density > max ramp density using minRates
//                if(rampExpectedDensity > MAX_RAMP_DENSITY) {                
//                    
//                    minimumRate = getMinimumRateForMaxRampDensity(); 
//                }
//            }
//                        
            if(minimumRate <= 0) {
                minimumRate = MeteringConfig.getMinRate();
            }                        
        }
        
        /**
         * Return expected ramp density at next time step
         * @return 
         */
        private double getExpectedRampDensity(double minRates) {
            
            // * equation : Ke = ( Cd(t+1) - Cf(t+1) ) / L
            //      - Ke : expected ramp density at next time step
            //      - Cd(t+1) : cumulative demand volume at next time step
            //      - Cf(t+1) : cumulative passage volume at next time step
            //      - L : ramp length in mile (if meter type is dual, L = L*2)
            
            // * Cd(t+1) = Cd(t) + current demand
            // * Cf(t+1) = Cf(t) + minimum rates
            
            double Cd_next = this.cumulativeDemand.get(this.cumulativeDemand.size()-1) + this.getDemandVolume();
            double Cf_next = this.cumulativeMergingVolume.get(this.cumulativeMergingVolume.size()-1) + (minRates/120);
            
            // expected vehs for next 30s
            double expected_vehs = Cd_next - Cf_next; 
            
            double rampLength = this.meter.getMeter().getStorage();
            if(this.meter.getMeterType() == MeterType.DUAL) {
                rampLength *= 2;
            }   
            
            
            return expected_vehs / ( rampLength / InfraConstants.FEET_PER_MILE );
        }
        
        /**
         * Return minimum rate for next time satisfying max ramp density
         * @return 
         */
        private double getMinimumRateForMaxRampDensity() {            
            
            // * recalculate min rates when expected density > MAX_RAMP_DENSITY 
            //        at next time step with min rates
            
            // * equation : minRate = Cd(t+1) - Veh@MaxDensity - Cf(t)
            //    - Cd(t+1) = cumulative demand at next time step
            //    - Veh@MaxDensity = number of vehicles at max density
            //    - Cf(t) = cumulative passage flow at current time step
            
            // * assume :  demand(t) = demand(t+1)
            
            double rampLength = this.meter.getMeter().getStorage();
            if(this.meter.getMeterType() == MeterType.DUAL) {
                rampLength *= 2;
            }
            
            double It = this.cumulativeDemand.get(this.cumulativeDemand.size()-1);
            double Ot = this.cumulativeMergingVolume.get(this.cumulativeMergingVolume.size()-1);           
            
            // max number of vehicles in max density as flow
            double maxVehs = ( MAX_RAMP_DENSITY * (rampLength/InfraConstants.FEET_PER_MILE) );

            double Inext =  It + this.getDemandVolume();
            double Cf_next = Inext - maxVehs;   // veh / mile
            //double minRates = ( Cf_next - Ot ) * 120;
            double minRates = ( Inext - Ot - (MAX_RAMP_DENSITY * (rampLength/InfraConstants.FEET_PER_MILE) ) ) * 120;
            
            // DEBUG
            System.out.println(
                    "        -> Minimum Rate for Max density = " + String.format("%.2f", minRates) + 
                    ", maxVeh="+String.format("%.2f", maxVehs)+ 
                    ", Cd(t+1)="+String.format("%.2f", Inext*120)+ 
                    ", Cq(t+1)="+String.format("%.2f", Cf_next*120)+
                    ", L="+String.format("%.2f", rampLength/InfraConstants.FEET_PER_MILE)
            );
            
            return minRates;
        }
        
        /**
         * Calculate demand and output
         */
        public void updateState() {
            if(this.meter == null) return;
            
            this.isRateUpdated = false;
            
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
            
            this.calculateMinimumRate();
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
                        
            return p_flow * PASSAGE_DEMAND_FACTOR;                                    
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
        

        /**
         * Set metering rate
         * @param Rnext 
         */
        public void setRate(double Rnext) {
            if(this.meter == null) return;
            this.lastRate = Rnext;
            float redTime = calculateRedTime(Rnext);
            redTime = Math.round(redTime * 10) / 10f;
            this.isRateUpdated = true;
            this.meter.setRate((byte)1);
            this.meter.setRedTime(redTime);
        }
        
        public void startMetering() {
            this.isMetering = true;
        }
        
        public void stopMetering() {
            this.isMetering = false;
            this.lastDemand = 0;
            this.lastVolumeOfRamp = 0;
            this.lastRate = 0;            
            this.rateHistory.clear();
            this.noBottleneckCount = 0;
            this.hasBeenStoped = true;
            this.meter.setRate(SimConfig.METER_RATE_FLASH);
        }
        
        /**
         * Return red time that converted from rate
         * @param rate
         * @return red time in seconds
         */
        private float calculateRedTime(double rate) {
            float cycle = 3600 / (float)rate;
            return Math.max(cycle - this.meter.GREEN_YELLOW_TIME, MeteringConfig.MIN_RED_TIME);
        }

        public void saveRateHistory(double Rnext) {
           this.rateHistory.add(Rnext);
        }

        public void saveSegmentDensityHistory(int dataCount, double Kt) {
            this.segmentDensityHistory.put(dataCount, Kt);
        }
        
        public Double getSegmentDensity(int dataCount)
        {
            return segmentDensityHistory.get(dataCount);            
        }

        public void addNoBottleneckCount() {
            this.noBottleneckCount++;
        }
        
        public void resetNoBottleneckCount() {
            this.noBottleneckCount = 0;
        }
        
        public int getNoBottleneckCount() {
            return this.noBottleneckCount;        
        }

        public void setBottleneck(StationState bottleneck) {
            this.bottleneck = bottleneck;
        }

        public StationState getBottleneck() {
            return this.bottleneck;
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
            exitsStates.add(this);
        }     
        
    }
    
    /**
     * State class that represents station
     */
    class StationState extends State {

        SimStation station;
        //EntranceState associatedEntrance;
        DIR associatedDir;
        int stationIdx = 0;
        double aggregatedDensity = 0;
        double aggregatedSpeed = 0;
        
        int MOVING_U_AVG_WINDOW = 2;
        int MOVING_K_AVG_WINDOW = 2;
        
        int MAX_SPEED_ALPHA = 10;
        int lastSpeedAggCount = 0;
        int noBsCount = 0;
        boolean isBottleneck = false;
        boolean isPrevBottleneck = false;
        boolean isConsecutiveBS = false;
        double NEARBY_DISTANCE = 2000;  // 2000 feet
                
        public IDetectorChecker dc = Metering.dc;
        ArrayList<EntranceState> associatedEntrances = new ArrayList<EntranceState>();
        double Kc = MeteringConfig.Kc;
        boolean isPrimaryBottleneck = false;
        double coordinateKc = -1;
        int trendIndicator;
        int coordinateLimit = 1;
        private boolean isPrevPrimaryBottleneck = false;

                
        public StationState(Station s) {            
            super(s.getStationId(), s);
            this.station = simObjects.getStation(s.getStationId());
            type = StateType.STATION;
            stationStates.add(this);
            this.stationIdx = stationStates.size()-1;
        }

        /**
         * Return acceleration from current station to down station
         * @param lastSampleIndex
         * @return 
         */
        public double getAcceleration() {
            double u2 = this.getAggregatedSpeed();
            StationState downStationState = getDownstreamStationState(idx);
            if (downStationState == null) {
                return 0;
            }
            double u1 = downStationState.getAggregatedSpeed();
            return (u1 * u1 - u2 * u2) / (2 * TMO.getDistanceInMile(this.rnode, downStationState.rnode));
        }
        
        /**
         * Return aggregated density
         * @return 
         */
        public double getAggregatedDensity() {
            return getAggregatedDensity(0);
        }        
        
        /**
         * Returns aggregated density before given prevStep time step
         * @return 
         */
        public double getAggregatedDensity(int prevStep) {
            double sum = 0;
            int validCount = 0;
            for(int i=0; i<MOVING_K_AVG_WINDOW; i++) {
                double k = station.getData(dc, TrafficType.DENSITY, prevStep+i);
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
            return getMovingAverageSpeed(prevStep, MOVING_U_AVG_WINDOW);
        }

        public double getMovingAverageSpeed(int prevStep, int howManySteps)
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

        /**
         * Associated Meter to Station
         *   - Iterate from upstream to downstream
         *   - Upstream meter will be associated to the station 
         *     when distance(Upstream Meter, Station) less than 500 feet 
         *          or Meter is not associated to any station
         *   - Downstream meter will be associated to the station
         *     when distance(Downstream Meter, Station) less than 1 mile         
         */
        private void setAssociatedEntrances() {
           
            ArrayList<EntranceState> upstreamEntrances = this.getUpstreamEntrances();
            ArrayList<EntranceState> downstreamEntrances = this.getDownstreamEntrances();
            
            StationState us = null, ds = null;
            if(this.stationIdx > 0) us = stationStates.get(this.stationIdx-1);
            if(this.stationIdx < stationStates.size()-1) ds = stationStates.get(this.stationIdx+1);
                        
            if(us != null) {
                for(EntranceState es : upstreamEntrances) {                    
                    int d = this.getDistanceToUpstreamEntrance(es);                    
                    int ud = us.getDistanceToDownstreamEntrance(es) ;
                    
                    // very close(?) or not allocated with upstream station                    
                    //if( ( d < 800 && ud > 500) || es.associatedStation == null) {
                    if( ( d < 500 && d < ud) || es.associatedStation == null) {
                        if(es.associatedStation != null) {
                            es.associatedStation.associatedEntrances.remove(es);
                        }                        
                        associatedEntrances.add(es);
                        es.associatedStation = this;
                    }
                    
                    
                }
            }
            
            if(ds != null) {
                for(EntranceState es : downstreamEntrances) {
                    int d = this.getDistanceToDownstreamEntrance(es);
                    if(d < 5280) {
                        associatedEntrances.add(es);
                        es.associatedStation = this;
                    }
                }
            }
        }
        
        public ArrayList<EntranceState> getAssociatedEntrances() {
            return associatedEntrances;
        }
        
        public int getDistanceToUpstreamEntrance(EntranceState es) {
            if(this.idx <= 0) return -1;
            int distance = 0;
            State cursor = this;
            State s = null;
            
            boolean found = false;
            for(int i=this.idx-1; i>=0; i--) {
                s = states.get(i);
            
                distance += TMO.getDistanceInFeet(cursor.rnode, s.rnode);
                if(s.equals(es)) {
                    found = true;
                    break;
                }
                cursor = s;
            }
            if(found) return distance;
            else return -1;
        }
        
        public int getDistanceToDownstreamEntrance(EntranceState es) {
            if(this.idx >= states.size()-1) return -1;
            int distance = 0;
            State cursor = this;
            State s = null;
            
            boolean found = false;
            for(int i=this.idx+1; i<states.size(); i++) {
                s = states.get(i);
                distance += TMO.getDistanceInFeet(cursor.rnode, s.rnode);
                if(s.equals(es)) {
                    found = true;
                    break;
                }
                cursor = s;
            }
            if(found) return distance;
            else return -1;
        }        

        /**
         * Return upstream entrances up to next upstream station
         * @return upstream entrance list
         */
        public ArrayList<EntranceState> getUpstreamEntrances() {
            ArrayList<EntranceState> list = new ArrayList<EntranceState>();
            if(this.idx <= 0) return list;
                 
            for(int i=this.idx-1; i>=0; i--) {
                State s = states.get(i);
                if(s.type.isStation()) break;
                if(s.type.isEntrance() && ((EntranceState)s).meter != null) list.add((EntranceState)s);
            }        
            return list;
        }    
        
        /**
         * Return downstream entrances up to next downstream station
         * @return downstream entrance list
         */        
        public ArrayList<EntranceState> getDownstreamEntrances() {
            ArrayList<EntranceState> list = new ArrayList<EntranceState>();
            if(this.idx >= states.size()-1) return list;

            for(int i=this.idx+1; i<states.size(); i++) {
                State s = states.get(i);
                if(s.type.isStation()) break;
                if(s.type.isEntrance()&& ((EntranceState)s).meter != null) list.add((EntranceState)s);
            }        
            return list;
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
        }
        
        public void afterMetering() {
            // initialize variables to control coordinate
            if(!this.isPrimaryBottleneck) {
                this.trendIndicator = 0;                
                this.coordinateLimit = 1;
            }
            this.coordinateKc = -1;
            this.isPrevPrimaryBottleneck = this.isPrimaryBottleneck;
            
            this.isPrevBottleneck = this.isBottleneck;
            this.isBottleneck = false;
            this.isPrimaryBottleneck = false;
            this.isConsecutiveBS = false;
            
        }

        public void updatePrimaryState() {
            if(!this.isPrevPrimaryBottleneck) {
                this.trendIndicator = 0;
            } else if(this.getAggregatedDensity() > this.getAggregatedDensity(1)) {
                this.trendIndicator++;
            } else if(this.getAggregatedDensity() < this.getAggregatedDensity(1)) {
                this.trendIndicator--;
            }
            this.trendIndicator = Math.min(5, this.trendIndicator);
            this.trendIndicator = Math.max(-5, this.trendIndicator);
        }
                
    }
}
