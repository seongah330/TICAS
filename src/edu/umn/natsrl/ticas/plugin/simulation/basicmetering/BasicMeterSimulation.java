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
package edu.umn.natsrl.ticas.plugin.simulation.basicmetering;

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.ticas.Simulation.EntranceState;
import edu.umn.natsrl.ticas.Simulation.Simulation;
import edu.umn.natsrl.ticas.Simulation.SimulationConfig;
import edu.umn.natsrl.ticas.Simulation.SimulationImpl;
import edu.umn.natsrl.ticas.Simulation.StationState;
import edu.umn.natsrl.vissimcom.VISSIMVersion;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class BasicMeterSimulation extends Simulation implements SimulationImpl{
    ArrayList<BasicMeter> metering = new ArrayList();
    public BasicMeterSimulation(String caseFile, int seed, Section section, VISSIMVersion v){
        super(caseFile,seed,section,v,SimulationConfig.RunningInterval);
    }
    
    @Override
    public void RunningInitialize() {
        super.RunningInitialize();
    }

    @Override
    public void ExecuteBeforeRun() {
        super.ExecuteBeforeRun();
    }

    @Override
    public void ExecuteAfterRun() {
        super.ExecuteAfterRun();
        updateMeterState();
    }

    @Override
    public void DebugMassage() {
//        super.DebugMassage();
        super.DisplayStationState();
        DisplayState();
    }
    
    public ArrayList<EntranceState> getEntranceStates(boolean hasmeter){
        return this.sectionHelper.getEntranceStates(hasmeter);
    }
    public ArrayList<StationState> getStationState(){
        return this.sectionHelper.getStationStates();
    }
    public StationState getStationState(String id){
        return this.sectionHelper.getStationState(id);
    }
    public void setBasicMeter(ArrayList<BasicMeter> m){
        metering = m;
    }

    private void DisplayState() {
        System.err.println("clearlog");
        if(metering != null){
            System.err.println("Meter Method : " + metering.get(0).getMethod().toString());
        }
        System.err.println("=====Metering State======================");
        for(BasicMeter bm : metering){
            EntranceState es = bm.getEntranceState();
            double queue = es.getCumulativePassage() - es.getCumulativeDemand();
//            System.err.println(bm.getID() + " : " + "Queue Demand="+es.getQueueVolume()+", Passage="+es.getPassageVolume()+", Queue="+queue+", Rate="+bm.getLastRate());
            System.err.println(bm.getID() + " : " + "Queue Demand="+es.getCumulativeDemand()+", Passage="+es.getCumulativePassage()+
                    ", Queue="+Math.abs(queue)+", Rate="+bm.getLastRate());
        }
        
        System.err.println("=========Meter Method=======");
        for(BasicMeter bm : metering){
            System.err.println(bm.getID() + " : "+bm.getMethodState());
        }
    }

    private void updateMeterState() {
        for(BasicMeter bm : metering){
            bm.updateState();
        }
    }
}
