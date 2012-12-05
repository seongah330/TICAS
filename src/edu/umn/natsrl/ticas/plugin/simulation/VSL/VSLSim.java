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

import edu.umn.natsrl.chart.TICASChart;
import edu.umn.natsrl.chart.TICASChartXY;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.DMSImpl;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.ticas.Simulation.Simulation;
import edu.umn.natsrl.ticas.Simulation.SimulationImpl;
import edu.umn.natsrl.ticas.Simulation.StationState;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.VSLAlgorithm;
import edu.umn.natsrl.vissimcom.VISSIMVersion;
import info.monitorenter.gui.chart.views.ChartPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class VSLSim extends Simulation implements SimulationImpl{
    
    VSLAlgorithm vsl;
    JPanel chartPanel = null;
    private ArrayList<VSLStationState> VSLStationStates = new ArrayList<VSLStationState>();
    
    //for Graph X axis
    HashMap<Integer,String> xmap;
    VSLChartXY chart;
    ChartPanel cpn;
    
    VSLMilePointList ml;
    
    public VSLSim(String caseFile, int seed, Section section, VISSIMVersion v){
        super(caseFile,seed,section,v);
        init();
    }
    
    private void init() {
        /*
         * Set VSLStationState
         * set Up, down VSLStation
         */
        for(int i=0;i<sectionHelper.getStationStates().size();i++){
            StationState cs = sectionHelper.getStationStates().get(i);
            VSLStationState current = new VSLStationState(cs);
            
            if(cs.getUpstreamStationState() != null){
                VSLStationState upstreamVSL = VSLStationStates.get(i-1);
                current.setUpstreamVSLStationState(upstreamVSL);
                upstreamVSL.setDownStreamVSLStation(current);
            }
            
            VSLStationStates.add(current);
            
        }
        initDebug();
        ml = new VSLMilePointList(VSLStationStates,section.getDMS());

    }
    
    @Override
    public void RunningInitialize() {
        super.RunningInitialize();
        vsl = new VSLAlgorithm(VSLStationStates,this.section);
        initChart();
//        Debug();
    }

    @Override
    public void ExecuteBeforeRun() {
        super.ExecuteBeforeRun();
    }

    @Override
    public void ExecuteAfterRun() {
        super.ExecuteAfterRun();
        
        //Update Station
        for(VSLStationState cs : VSLStationStates){
            cs.updateState();
        }
        
        vsl.Process();
    }

    @Override
    public void DebugMassage() {
            //for Station debuging
            for (int i = 0; i < VSLStationStates.size(); i++) {
                VSLStationState s = VSLStationStates.get(i);
                System.out.println(s.getID() + " : T_Q="+String.format("%.1f",s.getFlow())
                        + " A_Q="+String.format("%.1f",s.getAverageFlow(0, this.getDebugIntervalIndex()))
                        + " k=" +String.format("%.1f", s.getAverageDensity(0,getDebugIntervalIndex()))
                        + " u=" + String.format("%.1f", s.getAverageSpeed(0, getDebugIntervalIndex()))
                        + " v=" + s.getTotalVolume(0, getDebugIntervalIndex())
                        + " acc = "+s.calculateAcceleration());
            }
            
            System.err.println("clearlog");
            for(VSLStationState cvs : VSLStationStates){
                System.err.println(cvs.getID()+"("+cvs.getMilePoint()+")" + " : "+"bcount = "+cvs.n_bottleneck+" , bottleneck = " + cvs.bottleneck+", pbottle = "+ cvs.p_bottleneck);
            }
            System.err.println();
            System.err.println();
            System.err.println("DMS Information");
            for(DMSImpl d : section.getDMS()){
                System.err.println(d.getId()+"("+d.getMilePoint(section.getName())+")" + " : "+d.isStarted()+", speedlimit : "+d.getSpeedLimit());
            }
            
            updateChart();
    }

    private void Debug() {
        if(VSLStationStates.isEmpty())
            return;
        
        System.out.println("Node Lists");
        StationState node = VSLStationStates.get(0);
        while(true){
            System.out.print(node.getID() + " - ");
//            if(node.getUpstreamStationState() != null){
//                System.out.print(node.getUpstreamStationState().getID()+"("+node.getStation().getDistanceToUpstreamStation(section.getName())+")");
//            }else
//                System.out.print("null");
            
            if(node.getDownStreamStationState() != null){
                System.out.print(" - "+node.getDownStreamStationState().getID()+"("+node.getStation().getDistanceToDownstreamStation(section.getName())+")");
                System.out.print(" - "+node.getdistanceToDownstreamStationState());
            }else
                System.out.print(" - null");
            
            
            System.out.println();
//            System.out.println(node.getID() + " - "+node.getDownStreamStationState().getID()+"("+node.getStation().getDistanceToDownstreamStation(this.section.getName())+")"
//                    +" : "+node.getDownStreamStationState().getID()+"("+node.getdistanceToDownstreamStationState()+")");
            if(node.getDownStreamStationState() != null){
                node = node.getDownStreamStationState();
            }else{
                break;
            }
        }
        
        System.out.println("VSL Check");
        VSLStationState n = VSLStationStates.get(0);
        while(true){
            System.out.println(n.getID());
            if(n.getDownstreamVSLStationState() != null){
                n = n.getDownstreamVSLStationState();
            }else{
                break;
            }
        }
        
        System.out.println();
        System.out.println("StationState List");
        for(VSLStationState s : this.VSLStationStates){
            System.out.println(s.getID() + " : "+s.getdistanceToDownstreamStationState());
        }
        System.out.println("RealStation List");
        if(VSLStationStates.isEmpty())
            return;
        Station rstation = VSLStationStates.get(0).getStation();
        while(true){
            System.out.print(rstation.getStationId() + " : ");
            Station down = rstation.getDownStation(this.section.getName());
            if(down == null){
                System.out.println("null");
                break;
            }
            else{
                System.out.println(down.getDistanceToUpstreamStation(this.section.getName()));
                rstation = down;
            }
        }
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

    void setChartPanel(JPanel PanelChart) {
        chartPanel = PanelChart;
    }

    private void updateChart() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                chart.AddStationSpeedGraph(ml.getStationStates(), "StationSpeed");
                chart.AddDMSSpeedGraph(ml.getDMSs(), "DMSSpeedLimit");
                cpn.setSize(chartPanel.getSize());
                chartPanel.getParent().validate();
            }
        }, 1);
    }

    private void initChart() {
        xmap = new HashMap<Integer,String>();
        int cnt = 0;
        for(VSLStationState s : VSLStationStates){
            xmap.put(cnt, s.getID());
            cnt++;
        }
        chartPanel.removeAll();
        chart = new VSLChartXY(ml.getMilePointListLayout(),null);
        cpn = new ChartPanel(chart.getChart());
        cpn.setSize(chartPanel.getSize());
        chartPanel.add(cpn);
    }
}
