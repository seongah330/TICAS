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
package edu.umn.natsrl.ticas.Simulation;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.DMS;
import edu.umn.natsrl.infra.infraobjects.DMSImpl;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.simobjects.SimDMS;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class Emulation extends Thread {
    protected Section section;
    protected Period period;
    protected SimObjects simObjects = SimObjects.getInstance();
    protected ArrayList<SimMeter> meters = new ArrayList<SimMeter>();
    protected ArrayList<SimDetector> detectors = new ArrayList<SimDetector>();
    protected ArrayList<SimDMS> simDMSs = new ArrayList<SimDMS>();
    
    protected ArrayList<StationState> stationStates;
    protected ArrayList<EntranceState> entrancestates;
    
    private ISimEndSignal signalListener;            
    private int samples = 0;    
    
    public SectionHelper sectionHelper;
    
    private boolean isDebug_StationInfo = true;
    
    public Emulation(Section _section, Period p){
        this.section = _section;
        period = p;
        initEmulation();
    }
    
    private void initEmulation() {
        detectors = loadSimDetectorFromSection();
        simDMSs = loadSimDMSFromSection();
        sectionHelper = new SectionHelper(section,detectors,meters,simDMSs);
    }
    
    @Override
    public void run(){
        System.out.println("Detector Size : "+detectors.size());
        System.out.println("DMS Size : "+simDMSs.size());
        
        System.out.println("Load Data from IRIS..."+period.getPeriodString());
        section.loadData(period);
        int totalSamples = period.getIntervalPeriod();
        System.out.println("Total Samples : "+totalSamples);
        RunningInitialize();
        while(true){
            ExecuteBeforeRun();
            
            Process(samples);
            
            ExecuteAfterRun();
            DebugMessage();
            
            samples++;
            if(samples >= totalSamples){
                break;
            }
        }
        
        signalListener.signalEnd(0);
    }
    
    public void setSignalListener(ISimEndSignal signalListener) {
        this.signalListener = signalListener;
    }
    
    public int getSample() {
        return samples;
    }

    private ArrayList<SimDetector> loadSimDetectorFromSection() {
        ArrayList<SimDetector> simDets = new ArrayList<SimDetector>();
        int cnt = 0;
        for(RNode s : section.getRNodes()){
            for(Detector det : s.getDetectors()){
                cnt ++;
                simDets.add(simObjects.getDetector(det.getId()));
            }
        }
        System.out.println("Total : "+cnt);
        return simDets;
    }

    private ArrayList<SimDMS> loadSimDMSFromSection() {
        ArrayList<SimDMS> simdms = new ArrayList<SimDMS>();
        
        for(DMSImpl d : section.getDMS()){
            for(DMS dms : d.getDMSList()){
                simdms.add(simObjects.getDms(dms.getId()));
            }
        }
        
        return simdms;
    }

    protected void RunningInitialize() {
        stationStates = sectionHelper.getStationStates();
        entrancestates = sectionHelper.getEntranceStates();
    }

    protected void ExecuteBeforeRun() {
    }

    protected void ExecuteAfterRun() {
        updateStates();
    }

    protected void DebugMessage() {
        DisplayStationState();
    }

    private void Process(int sample) {
        for(RNode r : section.getRNodes()){
            if(r.isStation() && !r.isAvailableStation()){
                continue;
            }
            for(Detector d : r.getDetectors()){
                if(d.getVolume() == null || (d.getVolume() != null && d.getVolume().length-1 < sample)){
                    System.out.println(r.getId()+", "+r.getLabel()+", "+r.getStationId()+", "+r.getInfraType().toString()+", "+d.getId());
                    continue;
                }
                double v = d.getVolume()[sample];
                double q = d.getFlow()[sample];
                double u = d.getSpeed()[sample];
                double k = d.getDensity()[sample];
                double occ = d.getOccupancy()[sample];
                AddDetectorData(d.getDetectorId(),v,q,u,k,occ);
            }
        }
    }
    
    private void AddDetectorData(int detector_id, double v, double q, double u, double k, double occupancy) {
        SimDetector simDetector = simObjects.getDetector(""+detector_id);
        simDetector.addData(v, q, u, k, occupancy);
    }
    
    public void DisplayStationState() {
        if(this.isDebug_StationInfo){
            //for Station debuging
            for (int i = 0; i < stationStates.size(); i++) {
                System.out.println(stationStates.get(i).getID() + " : T_Q="+String.format("%.1f",stationStates.get(i).getFlow())
                        + " A_Q="+String.format("%.1f",stationStates.get(i).getAverageFlow(0, this.getDebugIntervalIndex()))
                        + " k=" +String.format("%.1f", stationStates.get(i).getAverageDensity(0,getDebugIntervalIndex()))
                        + " u=" + String.format("%.1f", stationStates.get(i).getAverageSpeed(0, getDebugIntervalIndex()))
                        + " v=" + stationStates.get(i).getTotalVolume(0, getDebugIntervalIndex()));
            }
        }
    }
    
    public int getDebugIntervalIndex(){
        return this.period.interval / 30;
    }

    private void updateStates() {
        if(entrancestates.isEmpty())
            return;
        else{
            //Update Entrance
            for(EntranceState e : entrancestates){
                e.updateState();
            }
        }
        
        //Update Station
        for(StationState cs : stationStates){
            cs.updateState();
        }
    }
    
    public static interface ISimEndSignal {
        public void signalEnd(int code);
    }
}
