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
package edu.umn.natsrl.ticas.plugin.simulation.VSL;

import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.VSLStationState;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.DMSImpl;
import edu.umn.natsrl.ticas.Simulation.Emulation;
import edu.umn.natsrl.ticas.Simulation.EmulationImpl;
import edu.umn.natsrl.ticas.Simulation.StationState;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.VSLAlgorithm;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.VSLStationStateNew;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.VSLStationStateOld;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.VSLVersion;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class VSLEmulation extends Emulation implements EmulationImpl{
    
    private ArrayList<VSLStationState> VSLStationStates = new ArrayList<VSLStationState>();
    VSLMilePointList ml;
    VSLResults vslresult;
    
    VSLAlgorithm vsl;
    
    VSLVersion vslversion;
    public VSLEmulation(Section s, Period p, VSLVersion vv){
        super(s,p);
        vslversion = vv;
        init();
    }
    
    private void init() {
        /*
         * Set VSLStationState
         * set Up, down VSLStation
         */
        for(int i=0;i<sectionHelper.getStationStates().size();i++){
            StationState cs = sectionHelper.getStationStates().get(i);
            
            VSLStationState current = null;
            if(vslversion.isNewVersion()){
                current = new VSLStationStateNew(cs);
            }else if(vslversion.isOldVersion()){
                current = new VSLStationStateOld(cs);
            }
            
            if(cs.getUpstreamStationState() != null){
                VSLStationState upstreamVSL = VSLStationStates.get(i-1);
                current.setUpstreamVSLStationState(upstreamVSL);
                upstreamVSL.setDownStreamVSLStation(current);
            }
            
            VSLStationStates.add(current);
            
        }
        initDebug();
        ml = new VSLMilePointList(VSLStationStates,section.getDMS());
        vslresult = new VSLResults(section,ml,vslversion);
    }
    
    @Override
    public void RunningInitialize(){
        super.RunningInitialize();
        System.out.println("Run");
        vsl = new VSLAlgorithm(VSLStationStates,section,vslversion);
    }

    @Override
    public void ExecuteBeforeRun() {
        super.ExecuteBeforeRun();
    }

    @Override
    public void ExecuteAfterRun() {
        super.ExecuteAfterRun();
        for(VSLStationState cs : VSLStationStates){
            cs.updateState();
        }
        
        vsl.Process();
        updateResults();
    }

    @Override
    public void DebugMessage() {
    }

    public VSLResults getVSLResults() {
        return vslresult;
    }
    
    private void initDebug() {
        System.out.println();
        System.out.println("DMS-Station Distance compare");
        for(VSLStationState s : VSLStationStates){
            String sname = section.getName();
//            DMSImpl ddms = s.getStation().getDownstreamDMS(this.section.getName());
//            int ddistance = s.getStation().getDistancetoDownstreamDMS(sname);
//            DMSImpl udms = s.getStation().getUpstreamDMS(this.section.getName());
//            int udistance = s.getStation().getDistancetoUpstreamDMS(sname);
//            System.out.println("--"+s.getID());
//            System.out.println("---U:"+udms.getId()+"("+udistance+")"+"-DownstreamStation : "+udms.getDownstreamStation(sname)+"("+udms.getDistancetoDownstreamStation(sname));
//            System.out.println("---D:"+ddms.getId()+"("+ddistance+")"+"-UpstreamStation : "+ddms.getUpstreamStation(sname)+"("+ddms.getDistancetoUpstreamStation(sname));
            System.out.print("--"+s.getID() + " : ");
            if(s.getdistanceToUpstreamStationState() != -1){
                System.out.print(s.getdistanceToUpstreamStationState());
            }else{
                System.out.print("0");
            }
            System.out.print(" - ");
            System.out.println(s.getStation().getStationFeetPoint(this.section.getName()));
        }
    }    

    private void updateResults() {
        //add Station info
        for (int i = 0; i < VSLStationStates.size(); i++) {
            VSLStationState s = VSLStationStates.get(i);
            VSLResultStation rs = vslresult.getStations().get(s.getMilePoint());
            if(rs != null){
                rs.addData(s);
            }
        }
        
        //add DMS info
        for(DMSImpl d : section.getDMS()){
            VSLResultDMS rd = vslresult.getDMSs().get(d.getMilePoint(section.getName()));
            if(rd != null){
                rd.addData(d);
            }
        }
    }
}
