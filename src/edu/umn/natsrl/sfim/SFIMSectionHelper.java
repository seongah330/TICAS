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

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.simobjects.SimStation;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.vissimctrl.VISSIMHelper;
import java.util.ArrayList;

/**
 *
 * @author Chongmyung Park
 */
public class SFIMSectionHelper {
    
    private Section section;
    private ArrayList<EntranceState> entranceStates = new ArrayList<EntranceState>();
    private ArrayList<StationState> stationStates = new ArrayList<StationState>();
    private SimObjects simObjects = SimObjects.getInstance();
    private ArrayList<SimMeter> meters = new ArrayList<SimMeter>();
    private ArrayList<SimDetector> detectors = new ArrayList<SimDetector>();
    private double PASSAGE_DEMAND_FACTOR = 1.15;
    
    public SFIMSectionHelper(Section section, String casefile) {
        this.section = section;
        
        try {
            ArrayList<String> sgs = VISSIMHelper.loadSignalGroupsFromCasefile(casefile);
            for(String signal : sgs) {
                String name = signal;
                boolean isDual = false;
                if(name.contains("_L")) {
                    name = name.split("_")[0];
                    isDual = true;
                }
                if(name.contains("_R")) continue;            

                SimMeter sd = simObjects.getMeter(name);
                if(isDual) sd.setMeterType(SimMeter.MeterType.DUAL);
                else sd.setMeterType(SimMeter.MeterType.SINGLE);
                meters.add(sd);
            }   
            ArrayList<String> dets = VISSIMHelper.loadDetectorsFromCasefile(casefile);
            for(String det : dets) {
                detectors.add(simObjects.getDetector("D"+det));
            }            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        SimMeter meter = simObjects.getMeter("M35WN27");
        meter.getMeter().setPassage("D120");
        meter.getMeter().setQueue("D3495");
        
        init();
    }

    /**
     * Returns all entrance states
     * @return entrance state list
     */
    public ArrayList<EntranceState> getEntranceStates() {
        return entranceStates;
    }    
           
    public ArrayList<StationState> getStationStates() {
        return stationStates;
    }    
           
        
    
    /**
     * Initialize
     *   - Build up section structure
     */
    private void init() {

        for (RNode rn : section.getRNodesWithExitEntrance()) {
            if(rn.isAvailableStation()) {
                new StationState((Station) rn);
            }
            else if(rn.isEntrance()) this.entranceStates.add(new EntranceState((Entrance) rn));
        }

        for (SimMeter m : meters) {
            String name = m.getId();
            if (name.contains("_L")) {
                name = name.split("_")[0];
            }
            EntranceState es = findStateHasMeter(m);            
            if (es == null) {
                continue;
            }
            es.meter = m;
        }
    }

    

    /**
     * Return entrance state which include given ramp meter
     * @param meter ramp meter to find entrance
     * @return entrance state
     */
    private EntranceState findStateHasMeter(SimMeter meter) {
        for (int i = 0; i < entranceStates.size(); i++) {
            EntranceState s = entranceStates.get(i);
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
     * State class that represents entrance
     */
    class EntranceState {
        RNode rnode;        
        SimMeter meter;       
        double lastDemand = 0;
        double lastFlowOfRamp = 0;
        
        public EntranceState(Entrance e) {
            this.rnode = e;
        }
        
        /**
         * Return output in this time interval
         */        
        public double getFlow() {
            return this.lastFlowOfRamp;
        }
        
        /**
         * Return demand in this time interval
         */
        public double getDemand() {
            return this.lastDemand;
        }
        
        /**
         * Calculate demand and output
         */
        public void updateState() {
            if(this.meter == null) return;
                        
            double p_flow = calculateRampFlow();
            double demand = calculateRampDemand();
            
            this.lastDemand = demand;                
            this.lastFlowOfRamp = p_flow;      
        }
        
        /**
         * Return ramp demand
         * @return 
         */
        private double calculateRampDemand() {
            
            if(this.meter == null) return 0;
            
            SimDetector[] qDets = this.meter.getQueue();           
            
            double demand = 0;
            double p_flow = calculateRampFlow();
            
            // queue detector is ok
            if(qDets != null) {
                for(int i=0; i<qDets.length; i++) {
                    double d = (int)simObjects.getDetector(qDets[i].getId()).getData(TrafficType.FLOW);
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
        private double calculateRampFlow() {
            return calculateRampFlow(0);
        }
        
        /**
         * Return ramp flow before given period intervals
         * @param period
         * @return ramp flow
         */
        private double calculateRampFlow(int period) {
            if(this.meter == null) return 0;
            SimDetector pDet = this.meter.getPassage();
            SimDetector mDet = this.meter.getMerge();
            SimDetector bpDet = this.meter.getByPass();            
            
            double p_flow = 0;           

            // passage detector is ok
            if(pDet != null) {
                p_flow = simObjects.getDetector(pDet.getId()).getData(TrafficType.FLOW, period);
            } else {
                // merge detector is ok
                if(mDet != null) {
                    p_flow = simObjects.getDetector(mDet.getId()).getData(TrafficType.FLOW, period);                      
                    // bypass detector is ok
                    if(bpDet != null) {
                        p_flow -= simObjects.getDetector(bpDet.getId()).getData(TrafficType.FLOW, period);
                        if(p_flow < 0) p_flow = 0;
                    }                                      
                }   
            }    
            
            return p_flow;
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
    public class StationState  {
        int stationIdx = 0;                
        SimStation station;
        
        public StationState(Station s) {
            this.station = simObjects.getStation(s.getStationId());
            stationStates.add(this);
            this.stationIdx = stationStates.size()-1;
        }
    }       
    
}
