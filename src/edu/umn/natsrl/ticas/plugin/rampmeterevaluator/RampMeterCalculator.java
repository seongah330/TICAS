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
package edu.umn.natsrl.ticas.plugin.rampmeterevaluator;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.section.SectionHelper;
import edu.umn.natsrl.infra.section.SectionHelper.EntranceState;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.ticas.SimulationResult;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class RampMeterCalculator {
    ArrayList<Period> periods = new ArrayList<Period>();
    RampMeterEvaluatorMode mode;
    Section section;
    int interval = 30;
    
    SimulationResult sr = null;
    /**
     * New RampMeter Calculator
     * @param s Section
     * @param p Periods
     * @param m Calculator Mode
     */
    public RampMeterCalculator(Section s, ArrayList<Period> p, RampMeterEvaluatorMode m,int Interval){
        periods = p;
        mode = m;
        section = s;
        interval = Interval;
    }
    
    public RampMeterCalculator(Section s, ArrayList<Period> p, RampMeterEvaluatorMode m,int Interval,SimulationResult _sr){
        this(s,p,m,Interval);
        sr = _sr;
    }
    
    /**
     * Process Selected RampMeter
     * @return ArrayList<RampMeterResult> RampMeters Data
     */
    public ArrayList<RampMeterResult> Process(){
        ArrayList<RampMeterResult> results = new ArrayList<RampMeterResult>();
        for(Period p : periods){
            RampMeterResult r = new RampMeterResult(section,p,mode);
            r.setResult(getResult(section,p));
            //reset interval
            r.getPeriod().setInterval(interval);
            results.add(r);
        }
        return results;
    }
    
    public void print(){
        System.out.println(section.getName());
        System.out.println(mode.toString());
        System.out.println("\nStation LIst");
        for(String ids : section.getStationIds())
            System.out.println("Station : "+ids);
        System.out.println("\nPeriod List("+periods.size()+")");
        for(Period ps : periods){
            System.out.println(ps.getPeriodStringWithoutTime());
        }
    }

    /**
     * get Period Result
     * @param sec
     * @param p
     * @return 
     */
    private ArrayList<RampMeterNode> getResult(Section sec, Period p) {
        ArrayList<RampMeterNode> stations = new ArrayList<RampMeterNode>();
        
        SectionHelper sectionHelper = new SectionHelper(section);
        ArrayList<EntranceState> entrances = sectionHelper.getEntranceStates();
        
        /**
         * if not simulation
         */
        if(sr == null){
            section.loadData(p,false);
        }else{
            SimObjects.getInstance().reset();
            sr.setTrafficDataToDetectors();
            section.loadData(p,true,null);
        }
        
        for(EntranceState es : entrances){
            if(es.getRNode() == null)
                continue;
            
            RampMeterNode station = getRampMeterResult(es,p);
            stations.add(station);
        }
        
        return stations;
    }

    /**
     * get RampMeter REsult
     * @param es
     * @param p
     * @return 
     */
    private RampMeterNode getRampMeterResult(EntranceState es, Period p) {
        Entrance entrance = (Entrance)es.getRNode();
        ArrayList<Detector> queue = entrance.getQueue();
        Detector passage = entrance.getPassage();
        Detector merge = entrance.getMerge();
        Detector bypass = entrance.getBypass();
        Detector green = entrance.getGreen();
        
//        System.out.println(es.getRNode().getLabel());
        
        RampMeterNode ramp = new RampMeterNode(es,p,interval);
        ramp.setDefaultData(es.getRampDemandOld(),es.getRampDemandOld(TrafficType.VOLUME),es.getRampFlowNew(),es.getRampVolume(),green);
        ramp.process();
//        ramp.printVolume();
//        System.out.println("p1:"+p1.length+" p2:"+p2.length);
//        
//        for(int i=0;i<p1.length;i++){
//            System.out.println("p1 : " + p1[i]+" p2 : "+p2[i]);
//        }
        
        return ramp;
    }
}
